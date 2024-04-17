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
        
        URL url = new URL(urlDownload + "/version.ini");
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
        
        ApplicationVersion ap = new ApplicationVersion();
        ap.setVersion(version);
        ap.setRelease(release);
        applicationType.getApplicationVersions().add(ap);
    }
}
