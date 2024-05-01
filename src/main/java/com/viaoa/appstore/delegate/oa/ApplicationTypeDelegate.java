package com.viaoa.appstore.delegate.oa;

import java.net.*;

import com.viaoa.appstore.model.oa.*;
import com.viaoa.appstore.resource.Resource;
import com.viaoa.util.*;

public class ApplicationTypeDelegate {

    public static void checkForNewVersion(final ApplicationType applicationType) throws Exception {
        if (applicationType == null) return;
        
        String urlDownload = applicationType.getDownloadUrl();
        
        if (OAStr.isEmpty(urlDownload)) return;
        
        applicationType.setConsole(String.format("checking url %s for version", urlDownload));

        //  "https://github.com/ViaOA/oaappstore-run/raw/master/executable-jar";
        
        String s = applicationType.getAppDirectory();
        s = OAStr.convert(s, "\\", "/");
        
        URL url = new URL(urlDownload + "/appstore/" + s +  "/version.ini");
        URLConnection conn = url.openConnection();

        OAProperties gitProps = new OAProperties(conn.getInputStream());
        
        final int release = OAConv.toInt(gitProps.getProperty("Release"));
        final String version = gitProps.getProperty("Version");

        applicationType.setConsole(String.format("version.ini read, version=%s, release=%d", version, release));
        if (release == 0) return;
        
        for (ApplicationVersion ap : applicationType.getApplicationVersions()) {
            if (ap.getRelease() == release) {
                applicationType.setConsole("release already found, id="+ap.getId());
                return;
            }
        }
        
        ApplicationVersion av = new ApplicationVersion();
        av.setVersion(version);
        av.setRelease(release);

        // jars 
        for (int i = 1;; i++) {
            String fn = gitProps.getProperty("jar" + i);
            if (OAStr.isEmpty(fn)) {
                if (i >= 20) break;
                continue;
            }
            VersionFile vf = new VersionFile();
            vf.setFilePath(fn);
            if (OAStr.isNotEmpty(applicationType.getJarFileName()) && OAStr.indexOf(fn, applicationType.getJarFileName(), 0, true) >= 0) vf.setType(VersionFile.TYPE_AppJar);
            else if (OAStr.indexOf(fn, "oa-", 0, true) >= 0) vf.setType(VersionFile.TYPE_OAJar);
            else if (OAStr.indexOf(fn, "dependency-uber", 0, true) >= 0) vf.setType(VersionFile.TYPE_DependencyUber);
            else vf.setType(VersionFile.TYPE_OtherJar);
            av.getVersionFiles().add(vf);
        }

        // files
        for (int i = 1;; i++) {
            String fn = gitProps.getProperty("file" + i);
            if (OAStr.isEmpty(fn)) {
                if (i >= 20) break;
                continue;
            }
            VersionFile vf = new VersionFile();
            vf.setFilePath(fn);
            if (fn.toLowerCase().endsWith(".ini")) vf.setType(VersionFile.TYPE_IniFile);
            else vf.setType(VersionFile.TYPE_Other);
            av.getVersionFiles().add(vf);
        }        
        
        applicationType.getApplicationVersions().add(av);
    }
}
