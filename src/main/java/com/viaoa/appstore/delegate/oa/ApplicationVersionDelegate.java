package com.viaoa.appstore.delegate.oa;

import java.io.*;
import java.net.*;
import java.util.logging.Logger;

import com.viaoa.appstore.model.oa.*;
import com.viaoa.util.*;

public class ApplicationVersionDelegate {
    private static Logger LOG = Logger.getLogger(ApplicationVersionDelegate.class.getName());

    public static void setConsole(ApplicationVersion applicationVersion, String msg) {
        if (applicationVersion == null) return;
        ApplicationType applicationType = applicationVersion.getApplicationType();
        if (applicationType == null) return;
        applicationType.setConsole(msg);
        LOG.fine(msg);
    }

    public static boolean download(ApplicationVersion applicationVersion) throws Exception {
        if (applicationVersion == null) return false;

        for (VersionFile vf : applicationVersion.getVersionFiles()) {
            String fn = vf.getCalcFilePath();
            File file = new File(fn);
            if (vf.getCalcIsJarFile()) {
                if (vf.getType() != VersionFile.TYPE_AppJar) {
                    if (file.exists()) continue;
                }
            }
            String downloadUrl = vf.getCalcDownloadUrl();
            setConsole(applicationVersion, String.format("saving %s to file %s", downloadUrl, fn));
            
            URL url = new URL(downloadUrl);
            URLConnection conn = url.openConnection();
            DataInputStream dis = new DataInputStream(new BufferedInputStream(conn.getInputStream()));
        
            OAFile.mkdirsForFile(file);
            file.createNewFile();
            OutputStream fos = new FileOutputStream(file);
            byte[] bs = new byte[8196];
            
            for ( ;; ) {
                int x = dis.read(bs);
                if (x < 0) {
                    break;
                }
                fos.write(bs, 0, x);
            }
            fos.close();
            setConsole(applicationVersion, "saved " + fn);
        }
        
        // get release
        for (VersionFile vf : applicationVersion.getVersionFiles()) {
            if (vf.getType() != VersionFile.TYPE_IniFile) continue;
            if (!"version.ini".equalsIgnoreCase(vf.getFilePath())) continue;
            
            OAProperties gitProps = new OAProperties(vf.getCalcFilePath());

            String version = gitProps.getProperty("Version");
            if (OAStr.isEmpty(version)) continue;
            if (!version.equals(applicationVersion.getVersion())) continue;
            
            int release = OAConv.toInt(gitProps.getProperty("Release"));
            if (release > 0) applicationVersion.setRelease(release);
        }
        
        setConsole(applicationVersion, "finished successfully");
        
        applicationVersion.setCompleted(new OADateTime());
        return true;
    }

    public static String getConfigFileText(ApplicationVersion applicationVersion) {
        if (applicationVersion == null) return null;
        
        final ApplicationType applicationType = applicationVersion.getApplicationType();
        if (applicationType == null) return null;
    
        String txt = "";
        txt += "[Application]\n";
        txt += "app.classpath=";
    
        int cnt = 0;
        for (VersionFile vf : applicationVersion.getVersionFiles()) {
            if (!vf.getCalcIsJarFile()) continue;
            String fn = vf.getCalcFilePath();
            if (cnt++ > 0) txt += ";";
            String s =  fn;
            txt += s;
        }
        txt += "\n";
        
        txt += "app.mainclass="+applicationType.getMainClass()+"\n";
        txt += "\n";
        txt += "[JavaOptions]\n";
        
        String s = applicationType.getJvmOptions();
        if (OAStr.isEmpty(s)) s = "-Xmx2g"; 
        txt += "java-options="+s+"\n";
        txt += "\n";
        
        txt += "[ArgOptions]\n";
        txt += "# replace with  single, client, server\n";
        txt += "arguments=$RUNTYPE\n";
        
        s = applicationType.getAppDirectory();
        s = OAStr.convert(s, "\\", "/");
        
        txt += "arguments=RootDirectory=app/appstore/"+s+"/$RUNTYPE/$ID\n";
        txt += "arguments=checkForNewRelease=false\n";
        
        return txt;
    }
    
}
