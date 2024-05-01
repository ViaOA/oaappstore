package com.viaoa.appstore.control.client;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.appstore.delegate.ModelDelegate;
import com.viaoa.appstore.model.oa.*;
import com.viaoa.appstore.model.oa.propertypath.AppUserLoginPP;
import com.viaoa.concurrent.OAThread;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubListenerAdapter;
import com.viaoa.util.OADateTime;
import com.viaoa.util.OAFile;
import com.viaoa.util.OAString;

//qqqqqqqqqqqqqqqqq this should be removed ??


/**
 * This is used by the ServerApplication.runClient command, 
 * to be able to run a client that connects to a Server Application.
 * This will use the OAAppStore installer, and have the
 * jar file that matches version on ServerApplication,
 * and make temp changes to OAAppStore.cfg to then run the client app.
 */
public abstract class ClientProcessController {
    private static Logger LOG = Logger.getLogger(ClientProcessController.class.getName());
    
    private final ReentrantLock lock = new ReentrantLock(); 

    // map Process to RunningApplication
    private final ConcurrentHashMap<RunningApp, Process> hmRunningAppProcess = new ConcurrentHashMap<>();

    // used to remove RunningApplication when process is not running.
    private Thread thread;
    
    
    public ClientProcessController() {
        setup();
    }
    
    protected void setup() {
        Hub<RunningApp> hubRunApp = ModelDelegate.getLocalAppUserLoginHub().getDetailHub(AppUserLoginPP.appUser().clientApps().runningApp().pp);
/*qqqqqq        
        hubRunApp.addHubListener(new HubListenerAdapter<RunningApp>() {
            // check for RunningApp.stopRequest = !null
            @Override
            public void afterPropertyChange(HubEvent<RunningApp> e) {
                if (e == null) return;
                RunningApp runApp = e.getObject();
                if (RunningApp.P_StopRequest.equalsIgnoreCase(e.getPropertyName())) {
                    stopProcess(runApp);
                }
                else if (RunningApp.P_ServerApplication.equalsIgnoreCase(e.getPropertyName())) {
                    startProcess(runApp);
                }
            }
            @Override
            public void afterAdd(HubEvent<RunningApp> e) {
                if (e == null) return;
                RunningApp runApp = e.getObject();
                if (runApp == null) return;
                startProcess(runApp);
            }
        });
**/        
    }

    protected void startProcess(RunningApp runApp) {
        if (runApp == null) return;
/*qqqqqqq        
        if (runApp.getStopRequest() != null) return;

        AppUserLogin appUserLogin = ModelDelegate.getLocalAppUserLogin();
        if (appUserLogin == null) return;
        
//qqqqq        runApp.setAppUserLogin(appUserLogin);

        ClientApp clientApp = appUserLogin.getAppUser().getClientApps().find(ClientApp.P_RunningApp, runApp);
        if (clientApp == null) return;
        
        ServerApplication serverApp = clientApp.getServerApplication();
        if (serverApp == null) return;
        
        runApp(runApp);
        runThread();
*/        
    }    
    
    protected void runThread() {
/*qqqqqqqqqqq        
        if (thread != null) return;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (;;) {
                    AppUserLogin login = ModelDelegate.getLocalAppUserLogin();
                    for (RunningApp runApp : login.getRunningApps()) {
                        Process process = hmRunningAppProcess.get(runApp);
                        if (process != null) {
                            //qqqqqqqq was:
                            // runApp.setCpuSeconds(process.info().totalCpuDuration().stream().count());
                        }
                        if (process == null || !process.isAlive()) {
                            if (runApp.getStopped() == null) runApp.setStopped(new OADateTime());
                            
                            hmRunningAppProcess.remove(runApp);
                            OADateTime dtNow = new OADateTime();
                            OADateTime dt = runApp.getCreated();
                            dt = dt.addMinutes(60);
                            if (dt.before(dtNow)) {
                                runApp.delete();
                            }
                        }
                    }
                    OAThread.sleepSeconds(15);
                }
            }
        }, "ProcessController");
        thread.start();
*/        
    }
    
    protected void stopProcess(RunningApp runApp) {
/*qqqqqqqq        
        if (runApp == null) return;

        AppUserLogin appUserLogin = ModelDelegate.getLocalAppUserLogin();
        if (appUserLogin == null) return;

        if (runApp.getAppUserLogin() != appUserLogin) return;
        
        Process process = hmRunningAppProcess.get(runApp);
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
            hmRunningAppProcess.remove(runApp);
            runApp.setStopped(new OADateTime());
        }
*/        
    }
 
    public void runApp(RunningApp runApp) {
/*qqqqqq        
        try {
            lock.lock();
            _runApp(runApp);
        }
        catch (Exception e) {
            runApp.setError(e.getMessage());
            LOG.log(Level.WARNING, "Error ", e);
        }
        finally {
            lock.unlock();
        }
*/        
    }
    protected void _runApp(final RunningApp runApp) throws Exception {
        

        AppUserLogin appUserLogin = ModelDelegate.getLocalAppUserLogin();
        if (appUserLogin == null) return;

        final AppUser appUser = appUserLogin.getAppUser();
        if (appUser == null) return;
        
        final ClientApp clientApp = appUserLogin.getAppUser().getClientApps().find(ClientApp.P_RunningApp, runApp);
        if (clientApp == null) return;
        
        final ServerApplication serverApp = clientApp.getServerApplication();
        if (serverApp == null) return;
        
        final ApplicationVersion appVer = serverApp.getApplicationVersion();
        
        final ApplicationType appType = serverApp.getApplicationType();
        if (appType == null) return;
        

        // make sure that the jar file is loaded on this computer
        onLoadApplicationVersion(serverApp);
        

        // see: C:\Users\vvia\AppData\Local\OAAppStore\app\OAAppStore.cfg
        /*
            [Application]
            app.classpath=$APPDIR\template\template.jar
            app.mainclass=com.template.control.StartupController
            
            [JavaOptions]
            java-options=-Djpackage.app-version=1.0.0
            java-options=-Xmx2g
            
            [ArgOptions]
            arguments=rootDirectory=app/template
            arguments=single
        */
        
        String s;
        String txt = "";
        txt += "[Application]\n";

        
        
//qqq        txt += String.format("app.classpath=$APPDIR\\%s\\release\\%d\\%s.jar\n", appType.getDirectoryName(), appVer.getRelease(), appVer.getServerFileName());
        txt += String.format("app.mainclass=%s\n", appType.getMainClass());
        
        txt += "\n";
        txt += "[JavaOptions]\n";
        s = appType.getJvmOptions();
        if (OAString.isEmpty(s)) s = "-Xmx2g";
        txt += String.format("java-options=%s\n", s);
        
        
        
        txt += "\n";
        txt += "[ArgOptions]\n";
        txt += "arguments=client\n";
        // Important note:  $APPDIR does not work for ArgOptions/arguments
//qqqq        txt += String.format("arguments=RootDirectory=app/%s/runtime/client\n",appType.getDirectoryName());

        int port = serverApp.getClientPort();
        txt += String.format("arguments=ServerPort=%d\n", port);
        
        // note: make sure that client.ini file is in the install
        
        LOG.fine("saving OAAppStore.cfg, txt="+txt);
        
        File file = new File("app\\OAAppStore.cfg");
        final String hold = OAFile.readTextFile(file, 800);
        OAFile.writeTextFile(file, txt);
        
        try {
            s = "OAAppStore.exe";
            ProcessBuilder builder = new ProcessBuilder(s);

            // builder.redirectErrorStream(true);
            Process process = builder.start();
            
            /*qqqqq was: (might be in newer jdk)
            ProcessHandle.Info info = process.info();
            Optional<Duration> optional = info.totalCpuDuration();
            Duration duration = optional.get();
            if (duration != null) runApp.setCpuSeconds(duration.getSeconds());
            runApp.setPid(process.pid());
            */
            
            hmRunningAppProcess.put(runApp, process);
            
            OAThread.sleepSeconds(15);
            if (appVer.getVerified() == null) {
                appVer.setVerified(new OADateTime());
            }
        }
        finally {
            LOG.fine("restoring OAAPPStore.cfg, file="+file+", txt="+hold);
            OAFile.writeTextFile(file, hold);
        }
    }

    // make sure that the correct version of the jar files are loaded.  If not, then load them now.
    protected abstract void onLoadApplicationVersion(ServerApplication serverApp) throws Exception;
}

