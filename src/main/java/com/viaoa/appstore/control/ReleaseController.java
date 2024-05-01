package com.viaoa.appstore.control;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

import com.viaoa.appstore.resource.Resource;
import com.viaoa.util.OAProperties;
import com.viaoa.util.OAStr;

/**
 * Check for new update for OAAppStore from GitHub oaappstore-run project.
 * <p>
 * This is used for the Windows installer (*.msi) using jpackage.
 * It puts files under "app" directory, and updates "OAAppStore.cfg" with new jar files.
 *  
 * @author vince
 */
public class ReleaseController {
    private static Logger LOG = Logger.getLogger(ReleaseController.class.getName());
    private static final String urlDownload = "https://github.com/ViaOA/oaappstore-run/raw/master";
    private OAProperties gitProps;
    
    public boolean hasUpdate() throws Exception {
        final String currentRelease = Resource.getValue(Resource.APP_Release);
        getGitProperties();

        final String gitRelease = gitProps.getProperty("Release");
        final String gitVersion = gitProps.getProperty("Version");
        LOG.fine(String.format("currentRelease=%s, gitRelease=%s, gitVersion=%s", currentRelease, gitRelease, gitVersion));

        boolean b = OAStr.isEqual(currentRelease, gitRelease);
        return !b;
    }    

    public OAProperties getGitProperties() throws Exception {
        if (gitProps != null) return gitProps;

        URL url = new URL(urlDownload + "/appstore/com/viaoa/oaappstore/version.ini");
        URLConnection conn = url.openConnection();
        gitProps = new OAProperties(conn.getInputStream());
        
        return gitProps;
    }
    
    public void getUpdate() throws Exception {
        URL url;
        URLConnection conn;
        String s;
        File file;
        DataInputStream dis;
        
        LOG.fine("getting new update files, urlDownload=" + urlDownload);
        getGitProperties();
        final byte[] bs = new byte[8196];

        s = "app\\jarstore\\com\\viaoa\\oaappstore";
        file = new File(s);
        if (!file.exists()) {
            LOG.fine("creating directory for application files: " + s);
            file.mkdirs();
        }

        // load jar files
        for (int i = 1;; i++) {
            final String fn = gitProps.getProperty("jar" + i);
            if (fn == null || fn.trim().length() == 0) {
                if (i > 10) {
                    break;
                }
                continue;
            }

            s = fn.replace('/', '\\');
            s = "app\\jarstore\\" + s;
        
            file = new File(s);
            if (file.exists()) continue;
            LOG.fine("saving jar to=" + s);
            
            s = urlDownload + "/jarstore/" + fn;
            LOG.fine("getting jar from URL=" + s);
            url = new URL(s);
            conn = url.openConnection();

            dis = new DataInputStream(new BufferedInputStream(conn.getInputStream()));
        
            file.createNewFile();
            OutputStream fos = new FileOutputStream(file);
        
            for ( ;; ) {
                int x = dis.read(bs);
                if (x < 0) {
                    break;
                }
                fos.write(bs, 0, x);
            }
            fos.close();
            LOG.fine("saved " + fn);
        }
        
        // load other files
        s = "app\\appstore\\com\\viaoa\\oaappstore";
        file = new File(s);
        if (!file.exists()) {
            LOG.fine("creating directory for jar files: " + s);
            file.mkdirs();
        }
        
        for (int i = 1;; i++) {
            final String fn = gitProps.getProperty("file" + i);
            if (fn == null || fn.trim().length() == 0) {
                if (i > 10) {
                    break;
                }
                continue;
            }

            s = urlDownload + "/appstore/" + fn;
            LOG.fine("getting file from URL="+s);
            url = new URL(s);
            conn = url.openConnection();
            dis = new DataInputStream(new BufferedInputStream(conn.getInputStream()));
            
            s = fn.replace('/', '\\');
            s = "app\\appstore\\" + s;
            LOG.fine("saving file to=" + s);
            file = new File(s);
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            
            for (;;) {
                int x = dis.read(bs);
                if (x < 0) {
                    break;
                }
                fos.write(bs, 0, x);
            }
            fos.close();
            LOG.fine("saved " + fn);
        }
        LOG.fine("finished successfully");
    }
    
    public void updateConfig() throws Exception {
        getGitProperties();
        String txt = "";
        txt += "[Application]\n";
        
        txt += "app.classpath=";
        // was: txt += "app.classpath=$APPDIR\\oaappstore.jar\n";
        
        for (int i = 1;; i++) {
            String fn = gitProps.getProperty("jar" + i);
            if (fn == null || fn.trim().length() == 0) {
                if (i > 10) {
                    break;
                }
                continue;
            }
            fn = fn.replace('/', '\\');

            if (i > 1) txt += ";";
            
            String s = "app\\jarstore\\" + fn;
            txt += s;
        }        
        txt += "\n";
        
        txt += "app.mainclass=com.viaoa.appstore.control.StartupController\n";

        txt += "\n";
        txt += "[JavaOptions]\n";
        txt += "java-options=-Xmx2g\n";
        txt += "\n";
        
        txt += "[ArgOptions]\n";
        txt += "arguments=single\n";
        // Important note:  $APPDIR does not work for ArgOptions/arguments
        txt += "arguments=RootDirectory=app\\appstore\\com\\viaoa\\oaappstore\n";

        LOG.fine("updating app\\OAAppStore.cfg");
        LOG.fine("new text="+txt);
        
        File file = new File("app\\OAAppStore.cfg");
        OutputStream os = new FileOutputStream(file);
        os.write(txt.getBytes());
        os.close();
    }
    
    public static void main(String[] args) throws Exception {
        ReleaseController asc = new ReleaseController();
        boolean b = asc.hasUpdate();
        if (b) {
            asc.getUpdate();
            asc.updateConfig();
        }
    }
}
