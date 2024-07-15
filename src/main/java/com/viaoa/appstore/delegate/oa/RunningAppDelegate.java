package com.viaoa.appstore.delegate.oa;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import com.viaoa.appstore.model.oa.ApplicationType;
import com.viaoa.appstore.model.oa.ApplicationVersion;
import com.viaoa.appstore.model.oa.ClientApp;
import com.viaoa.appstore.model.oa.RunningApp;
import com.viaoa.appstore.model.oa.ServerApplication;
import com.viaoa.appstore.model.oa.SingleApp;
import com.viaoa.appstore.model.oa.VersionFile;
import com.viaoa.appstore.resource.Resource;
import com.viaoa.concurrent.OAThread;
import com.viaoa.util.OADateTime;
import com.viaoa.util.OAFile;
import com.viaoa.util.OAStr;

public class RunningAppDelegate {
    private static Logger LOG = Logger.getLogger(RunningAppDelegate.class.getName());
    private final static ReentrantLock lock = new ReentrantLock(); 

    // map Process to RunningApplication
    private final static ConcurrentHashMap<RunningApp, Process> hmRunningAppProcess = new ConcurrentHashMap<>();
    
    public static void setConsole(RunningApp runningApp, String msg) {
        if (runningApp == null) return;
        runningApp.setConsole(msg);
        LOG.fine(msg);
    }
    
    public static boolean stopProcess(final RunningApp runningApp) throws Exception {
        if (runningApp == null) return false;
        Process p = hmRunningAppProcess.get(runningApp);
        if (p == null) return false;
        // stopReadOutput(runningApp);
        p.destroy();
        // p.destroyForcibly();

        hmRunningAppProcess.remove(runningApp);
        runningApp.setStopped(new OADateTime());
        return true;
    }
    
    public static boolean run(final SingleApp singleApp) throws Exception {
        if (singleApp == null) return false;
        final ApplicationVersion applicationVersion = singleApp.getApplicationVersion();
        if (applicationVersion == null) return false;
        final ApplicationType applicationType = applicationVersion.getApplicationType();
        if (applicationType == null) return false;
        
        String txt = applicationVersion.getConfigFileText();
        txt = OAStr.convert(txt, "$RUNTYPE", "single");
        txt = OAStr.convert(txt, "$ID", ""+singleApp.getId());

        final RunningApp runningApp = new RunningApp();
        runningApp.setConfigText(txt);
        singleApp.setRunningApp(runningApp);
        
        run(runningApp, applicationVersion, "single", ""+singleApp.getId());
        return true;
    }

    public static boolean run(final ServerApplication serverApplication) throws Exception {
        if (serverApplication == null) return false;
        final ApplicationVersion applicationVersion = serverApplication.getApplicationVersion();
        if (applicationVersion == null) return false;
        final ApplicationType applicationType = applicationVersion.getApplicationType();
        if (applicationType == null) return false;
        
        
        String txt = applicationVersion.getConfigFileText();
        txt = OAStr.convert(txt, "$RUNTYPE", "server");
        txt = OAStr.convert(txt, "$ID", ""+serverApplication.getId());
        
        
        int x = serverApplication.getClientPort();
        if (x == 0) x = applicationType.getClientPort();
        if (x > 0) txt += String.format("arguments=ServerPort=%d\n", x);
        
        x = serverApplication.getHttpPort();
        if (x == 0) x = applicationType.getHttpPort();
        if (x > 0) txt += String.format("arguments=JettyPort=%d\n", x);

        x = serverApplication.getHttpsPort();
        if (x == 0) x = applicationType.getHttpsPort();
        if (x > 0) txt += String.format("arguments=JettySSLPort=%d\n", x);
        
        final RunningApp runningApp = new RunningApp();
        runningApp.setConfigText(txt);
        serverApplication.setRunningApp(runningApp);
        
        run(runningApp, applicationVersion, "server", ""+serverApplication.getId());
        return true;
    }

    public static boolean run(final ClientApp clientApp) throws Exception {
        if (clientApp == null) return false;
        
        ServerApplication serverApplication = clientApp.getServerApplication();
        if (serverApplication == null) return false;
        
        final ApplicationVersion applicationVersion = serverApplication.getApplicationVersion();
        if (applicationVersion == null) return false;
        final ApplicationType applicationType = applicationVersion.getApplicationType();
        if (applicationType == null) return false;
        
        
        String txt = applicationVersion.getConfigFileText();
        txt = OAStr.convert(txt, "$RUNTYPE", "client");
        txt = OAStr.convert(txt, "$ID", ""+clientApp.getId());
        
        int x = serverApplication.getCalcClientPort();
        if (x > 0) txt += String.format("arguments=ServerPort=%d\n", x);
        
        txt += String.format("arguments=" + Resource.INI_StoreLogin + "=true\n");
        
        if (clientApp.getAutoLogin()) {
            txt += String.format("arguments=" + Resource.INI_AutoLogin + "=true\n");
            // txt += String.format("arguments=" + Resource.INI_AutoLogout + "=true\n");
        }
        
        final RunningApp runningApp = new RunningApp();
        runningApp.setConfigText(txt);
        clientApp.setRunningApp(runningApp);
        
        run(runningApp, applicationVersion, "client", ""+clientApp.getId());
        return true;
    }
    
    protected static boolean run(final RunningApp runningApp, final ApplicationVersion applicationVersion, final String runType, final String id) throws Exception {
        boolean b = false;
        try {
            lock.lock();
            b = _run(runningApp, applicationVersion, runType, id);
        }
        finally {
            lock.unlock();
        }
        return b;
    }
    
    
    protected static boolean _run(final RunningApp runningApp, final ApplicationVersion applicationVersion, final String runType, final String id) throws Exception {
        if (runningApp == null) return false;
        if (applicationVersion == null) return false;
        if (OAStr.isEmpty(runType)) return false;
        
        final ApplicationType applicationType = applicationVersion.getApplicationType();
        
        String txt = runningApp.getConfigText();

        setConsole(runningApp, "updating app\\OAAppStore.cfg, new text=");
        for (String s : txt.split("\\r?\\n")) {
            setConsole(runningApp, s);
        }

        // make sure that it has the files, but dont overwrite if it already exists
        for (VersionFile vf : applicationVersion.getVersionFiles()) {
            if (vf.getCalcIsJarFile()) continue;
            String fn = vf.getCalcFilePath();
            if (OAStr.isEmpty(fn)) continue;
            File file = new File(fn);
            if (!file.exists()) continue;
            
            if (vf.getType() == VersionFile.TYPE_IniFile) {
                if (!fn.toLowerCase().endsWith(runType.toLowerCase()+".ini")) {
                    if (fn.toLowerCase().endsWith("single.ini")) continue;
                    if (fn.toLowerCase().endsWith("client.ini")) continue;
                    if (fn.toLowerCase().endsWith("server.ini")) continue;
                    if (fn.toLowerCase().endsWith("version.ini")) continue;
                }
            }
            
            String fn2 = "app/appstore/" + applicationType.getAppDirectory() + "/" + runType.toLowerCase() + "/" + id + "/" + file.getName();
            fn2 = OAFile.convertFileName(fn2);
            File file2 = new File(fn2);
            if (file2.exists()) continue;
            
            OAFile.copy(file, file2);
        }
        
        
        File file = new File("app\\OAAppStore.cfg");
        final String holdConfig = OAFile.readTextFile(file, 800);
        
        try {
            OAFile.writeTextFile(file, txt);
            
            setConsole(runningApp, "running application");
            ProcessBuilder builder = new ProcessBuilder("OAAppStore.exe");
            
            // builder.redirectErrorStream(true);
            final Process process = builder.start();
            runningApp.setPid(process.pid());

            CompletableFuture<Process> cf = process.onExit();
            cf.thenAccept(px -> {
                hmRunningAppProcess.remove(runningApp);
                runningApp.setStopped(new OADateTime());
                
                //qqqqq
                ProcessHandle.Info info = process.info();
                Optional<Duration> optional = info.totalCpuDuration();
                Duration duration = optional.get();
                // if (duration != null) runApp.setCpuSeconds(duration.getSeconds());
            });
            
            hmRunningAppProcess.put(runningApp, process);
            
            final OADateTime dtEndConsole = (new OADateTime()).addSeconds(30);
            
            Runnable r = new Runnable() {
                public void run() {
                    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    for (;;) {
                        if (Thread.interrupted()) break;
                        
                        // removed, else it will it will end up blocking
                        // if ((new OADateTime()).after(dtEndConsole)) break;
                        
                        String txt = null;
                        try {
                            txt = br.readLine();
                        }
                        catch (Exception e) {
                        }
                        if (OAStr.isEmpty(txt)) {
                            if (!process.isAlive()) break;
                        }
                        else runningApp.setConsole(txt);
                    }
                    setConsole(runningApp, "outputstream thread ending");
                }
            };
            
            Thread t = Thread.ofVirtual().name("outputstream").start(r);            
            //final Thread t = new Thread(r, "ProcessOutput1");
            //t.start();
            
            Runnable r2 = new Runnable() {
                public void run() {
                    BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    for (;;) {
                        if (Thread.interrupted()) break;

                        // removed, else it will it will end up blocking
                        // if ((new OADateTime()).after(dtEndConsole)) break;
                        
                        String txt = null;
                        try {
                            txt = br.readLine();
                        }
                        catch (Exception e) {
                        }
                        if (OAStr.isEmpty(txt)) {
                            if (!process.isAlive()) break;
                        }
                        else runningApp.setConsole(txt);
                    }
                    setConsole(runningApp, "errorstream thread ending");
                }
            };
            /*
            final Thread t2 = new Thread(r2, "ProcessOutput2");
            t2.start();
            */
            Thread t2 = Thread.ofVirtual().name("errorstream").start(r2);            
            
            
            // give process enough time to start and use OAAppStore.cfg before it's overwritten with original
            OAThread.sleep(2500); 
            
            if (applicationVersion.getVerified() == null) {
                applicationVersion.setVerified(new OADateTime());
            }
            
            boolean b = process.isAlive();
            String msg = String.format("Process started, isAlive=%s", process.isAlive());
            setConsole(runningApp, msg);
            setConsole(runningApp, "Process Info: "+process.info());
        }
        finally {
            OAFile.writeTextFile(file, holdConfig);
        }
        return true;    
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
//qqqqqqqqqqqqqqqqq was:  can be removed qqqqqqqqqqqq    
    
    public static boolean runX(final RunningApp runningApp, final ApplicationVersion applicationVersion, final String runType, final String id) throws Exception {
        boolean b = false;
        try {
            lock.lock();
            b = _runX(runningApp, applicationVersion, runType, id);
        }
        finally {
            lock.unlock();
        }
        return b;
    }     
    
    protected static boolean _runX(final RunningApp runningApp, final ApplicationVersion applicationVersion, final String runType, final String id) throws Exception {
        if (runningApp == null) return false;
        if (applicationVersion == null) return false;

        final ApplicationType applicationType = applicationVersion.getApplicationType();
        if (applicationType == null) return false;
        
        
        String txt = applicationVersion.getConfigFileText();
        
        
        if (applicationType.getSingleTypeOnly()) {
            // run in it's directory
            txt = OAStr.convert(txt, "/$RUNTYPE/$ID", "");
        }
        else {
            // need to run in subdirectory, based on runtype and id
            txt = OAStr.convert(txt, "$RUNTYPE", runType);
            txt = OAStr.convert(txt, "$ID", id);
        }
        runningApp.setConfigText(txt);

        setConsole(runningApp, "updating app\\OAAppStore.cfg, new text=");
        for (String s : txt.split("\\r?\\n")) {
            setConsole(runningApp, s);
        }

        // start the App in it's own dir
        // make sure that it has the ini file
        File file = new File("app/appstore/" + applicationType.getAppDirectory() + "/" + runType.toLowerCase() + "/" + id + "/" + runType.toLowerCase() + ".ini");

        if (!file.exists()) {
            File file2 = new File("app/appstore/" + applicationType.getAppDirectory() + "/" + runType.toLowerCase() + ".ini");
            OAFile.copy(file2, file);
        }
        
        
        file = new File("app\\OAAppStore.cfg");
        final String hold = OAFile.readTextFile(file, 800);
        
        try {
            OAFile.writeTextFile(file, txt);
            
            setConsole(runningApp, "running application");
            ProcessBuilder builder = new ProcessBuilder("OAAppStore.exe");
            
            // builder.redirectErrorStream(true);
            final Process process = builder.start();
            runningApp.setPid(process.pid());

            CompletableFuture<Process> cf = process.onExit();
            cf.thenAccept(px -> {
                hmRunningAppProcess.remove(runningApp);
                runningApp.setStopped(new OADateTime());
                
                //qqqqq
                ProcessHandle.Info info = process.info();
                Optional<Duration> optional = info.totalCpuDuration();
                Duration duration = optional.get();
                // if (duration != null) runApp.setCpuSeconds(duration.getSeconds());
            });
            
            hmRunningAppProcess.put(runningApp, process);
            
            Runnable r = new Runnable() {
                public void run() {
                    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    for (;;) {
                        if (Thread.interrupted()) break;
                        String txt = null;
                        try {
                            txt = br.readLine();
                        }
                        catch (Exception e) {
                        }
                        if (OAStr.isEmpty(txt)) {
                            if (!process.isAlive()) break;
                        }
                        else runningApp.setConsole(txt);
                    }
                    setConsole(runningApp, "outputstream1 thread ending");
                }
            };
            
            Thread t = Thread.ofVirtual().name("outputstread1").start(r);            
            //final Thread t = new Thread(r, "ProcessOutput1");
            //t.start();
            
            Runnable r2 = new Runnable() {
                public void run() {
                    BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    for (;;) {
                        if (Thread.interrupted()) break;
                        String txt = null;
                        try {
                            txt = br.readLine();
                        }
                        catch (Exception e) {
                        }
                        if (OAStr.isEmpty(txt)) {
                            if (!process.isAlive()) break;
                        }
                        else runningApp.setConsole(txt);
                    }
                    setConsole(runningApp, "outputstream2 thread ending");
                }
            };
            /*
            final Thread t2 = new Thread(r2, "ProcessOutput2");
            t2.start();
            */
            Thread t2 = Thread.ofVirtual().name("outputstread2").start(r2);            
            
            
            // give process enough time to start and use OAAppStore.cfg before it's overwritten with original
            OAThread.sleep(2500); 
            
            if (applicationVersion.getVerified() == null) {
                applicationVersion.setVerified(new OADateTime());
            }
            
            try {
                t.interrupt();
                t2.interrupt();
            }
            catch (Exception e) {
            }
            
            boolean b = process.isAlive();
            String msg = String.format("Process started, isAlive=%s", process.isAlive());
            setConsole(runningApp, msg);
            setConsole(runningApp, "Process Info: "+process.info());
        }
        finally {
            OAFile.writeTextFile(file, hold);
        }
        return false;    
    }

    
/*qqqqqq    
    
    public static boolean stopReadOutput(final RunningApp runningApp) throws Exception {
        if (runningApp == null) return false;
        final Process p = hmRunningAppProcess.get(runningApp);
        if (p == null) return false;
        Thread t = hmRunningAppThread.get(runningApp);
        if (t == null) return false;
        hmRunningAppThread.remove(runningApp);
        t.interrupt();
        return true;
    }
    
    public static boolean startReadOutput(final RunningApp runningApp) throws Exception {
        if (runningApp == null) return false;
        final Process p = hmRunningAppProcess.get(runningApp);
        if (p == null) return false;
        
        Runnable r = new Runnable() {
            public void run() {
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                for (;;) {
                    try {
                        String txt = br.readLine();
                        runningApp.setConsole(txt);
                    }
                    catch (Exception e) {
                    }
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
        
        return true;
    }
*/    
    
    
    
}
