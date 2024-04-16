package com.viaoa.appstore.delegate.oa;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import com.viaoa.appstore.model.oa.*;
import com.viaoa.appstore.resource.Resource;
import com.viaoa.concurrent.OAThread;
import com.viaoa.util.*;

public class SingleAppDelegate {

    private static Logger LOG = Logger.getLogger(SingleAppDelegate.class.getName());

    private final static ReentrantLock lock = new ReentrantLock(); 

    // map Process to RunningApplication
    private final static ConcurrentHashMap<RunningApp, Process> hmRunningAppProcess = new ConcurrentHashMap<>();
    
    
    public static void setConsole(SingleApp singleApp, String msg) {
        if (singleApp == null) return;
        singleApp.setConsole(msg);
        LOG.fine(msg);
    }
    
    public static boolean run(final SingleApp singleApp) throws Exception {
        if (singleApp == null) return false;
        
        final ApplicationType applicationType = singleApp.getApplicationType();
        if (applicationType == null) return false;

        final ApplicationVersion applicationVersion = singleApp.getApplicationVersion();
        if (applicationVersion == null) return false;
        int release = applicationVersion.getRelease();
        
        
        String dirName = String.format("appstore/%s/%d", applicationType.getDirectoryName(), release);
        dirName = OAFile.convertFileName(dirName);

        String jarFileName = dirName + "/" + applicationType.getJarFileName();
        jarFileName = OAFile.convertFileName(jarFileName);
        setConsole(singleApp, "jar file name="+ jarFileName);
        
        
        
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

        String rootDir = String.format("app/datastore/%s/single/id_%d", applicationType.getDirectoryName(), singleApp.getId());
        File file = new File(rootDir);
        if (!file.exists()) file.mkdirs();
        
        
        String s;
        String txt = "";
        txt += "[Application]\n";
        
        txt += String.format("app.classpath=$APPDIR\\%s\n", OAStr.convert(jarFileName, "/", "\\"));
        txt += String.format("app.mainclass=%s\n", applicationType.getMainClass());
        
        txt += "\n";
        txt += "[JavaOptions]\n";
        s = applicationType.getJvmOptions();
        if (OAString.isEmpty(s)) s = "-Xmx2g";
        txt += String.format("java-options=%s\n", s);
        
        
        txt += "\n";
        txt += "[ArgOptions]\n";
        txt += "arguments=single\n";
        // Important note:  $APPDIR does not work for ArgOptions/arguments
        txt += String.format("arguments=RootDirectory=%s\n", rootDir);

        /*qqqqqqqq
        int port = serverApp.getClientPort();
        txt += String.format("arguments=ServerPort=%d\n", port);
        */
        
        // note: make sure that client.ini file is in the install
        
        LOG.fine("saving OAAppStore.cfg, txt="+txt);
        
        file = new File("app\\OAAppStore.cfg");
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

            
            RunningApp runApp = new RunningApp();
            singleApp.setRunningApp(runApp);
            
            hmRunningAppProcess.put(runApp, process);
            
            OAThread.sleepSeconds(5);
            if (applicationVersion.getVerified() == null) {
                applicationVersion.setVerified(new OADateTime());
            }
        }
        finally {
            LOG.fine("restoring OAAPPStore.cfg, file="+file+", txt="+hold);
            OAFile.writeTextFile(file, hold);
        }
        
        return true;
    }
}
