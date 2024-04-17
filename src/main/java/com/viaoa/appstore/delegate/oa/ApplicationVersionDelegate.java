package com.viaoa.appstore.delegate.oa;

import java.io.*;
import java.net.*;
import java.util.logging.Logger;

import com.viaoa.appstore.model.oa.*;
import com.viaoa.appstore.resource.Resource;
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
        
        ApplicationType applicationType = applicationVersion.getApplicationType();
        if (applicationType == null) return false;
        
        final String urlDownload = applicationType.getDownloadUrl();
        
        if (OAStr.isEmpty(urlDownload)) {
            setConsole(applicationVersion, "URL download is not set");
            return false;
        }
        
        applicationType.setConsole(String.format("checking download URL=%s", urlDownload));

        //  "https://github.com/ViaOA/oaappstore-run/raw/master/executable-jar";

        URL url;
        URLConnection conn;
        DataInputStream dis;
        int tot;
        final byte[] bs = new byte[8196];

        url = new URL(urlDownload + "/version.ini");
        conn = url.openConnection();
        OAProperties gitProps = new OAProperties(conn.getInputStream());
        
        final int release = OAConv.toInt(gitProps.getProperty("Release"));
        final String version = gitProps.getProperty("Version");

        setConsole(applicationVersion, String.format("version.ini read, version=%s, release=%d", version, release));
        
        if (applicationVersion.getRelease() != release) {
            setConsole(applicationVersion, String.format("release for ApplicationVersion release=%s, does not match version.ini release=%d", applicationVersion.getRelease(), release));
            return false;
        }

        url = new URL(urlDownload + "/" + applicationType.getJarFileName());
        conn = url.openConnection();

        dis = new DataInputStream(new BufferedInputStream(conn.getInputStream()));
        
        String dirName = String.format("appstore/%s/%d",  applicationType.getDirectoryName(), release);
        dirName = OAFile.convertFileName(dirName);
        File dir = new File(dirName);
        dir.mkdirs();

        String fileName = dirName + "/" + applicationType.getJarFileName();
        fileName = OAFile.convertFileName(fileName);
        File file = new File(fileName); 
        
        setConsole(applicationVersion, "file name will be "+ fileName);
        file.createNewFile();
        OutputStream fos = new FileOutputStream(file);
        
        tot = 0;
        for (int i = 0;; i++) {
            int x = dis.read(bs);
            if (x < 0) {
                break;
            }
            tot += x;
            fos.write(bs, 0, x);
        }
        setConsole(applicationVersion, "loaded file " + fileName + ", from " + url.toString() + ", size=" + tot);
        fos.close();
        
        // load other files
        for (int i = 1;; i++) {
            String fn = gitProps.getProperty("getfile" + i);
            setConsole(applicationVersion, "Checking version.ini for name=getfile" + i + ", value = " + fn);
            if (OAString.isEmpty(fn)) {
                if (i > 10) {
                    break;
                }
                continue;
            }

            file = new File(OAFile.convertFileName(dirName + "/" + fn));
            setConsole(applicationVersion, "gettting file " + file);

            file.createNewFile();
            fos = new FileOutputStream(file);

            url = new URL(urlDownload + "/" + fn);
            conn = url.openConnection();
            dis = new DataInputStream(new BufferedInputStream(conn.getInputStream()));

            tot = 0;
            for (;;) {
                int x = dis.read(bs);
                if (x < 0) {
                    break;
                }
                tot += x;
                fos.write(bs, 0, x);
            }
            setConsole(applicationVersion, "loaded file " + fn + ", from " + url.toString() + ", size=" + tot);
        }
        setConsole(applicationVersion, "update done for release=" + release + ", see directory=" + dirName);

        applicationVersion.setCompleted(new OADateTime());
        return true;
    }

}
