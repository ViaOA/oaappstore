// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.control.webserver;

import java.awt.Desktop;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.appstore.control.*;
import com.viaoa.appstore.control.server.JettyController;
import com.viaoa.appstore.delegate.*;
import com.viaoa.appstore.model.oa.cs.*;
import com.viaoa.appstore.resource.*;
import com.viaoa.sync.OASync;
import com.viaoa.util.*;

/**
 *  Main controller for starting a webserver that connects to the core server.
 */
public class WebserverController {
	private static Logger LOG = Logger.getLogger(WebserverController.class.getName());
	
    private LogController controlLog;
    private RemoteClientController controlRemote;
    private JettyController controlJetty;
    
    public WebserverController() {
        Resource.setLocale(null);
    }

    public boolean start() throws Exception {
        long ms = System.currentTimeMillis();
        getLogController();

        String server = Resource.getValue(Resource.APP_Server);
        int serverPort = Resource.getInt(Resource.APP_ServerPort, 1099);
        String serverDisplayName = Resource.getValue(Resource.APP_ServerDisplayName);
        LOG.config(String.format("Core Server: %s, port: %d, display name: %s", 
                server, serverPort, serverDisplayName));
        String jettyServer = Resource.getValue(Resource.APP_JettyServer);
        int jettyPort = Resource.getInt(Resource.APP_JettyPort, 80);
        int jettySSLPort = Resource.getInt(Resource.APP_JettySSLPort, 443);
        int jettyWSPort = Resource.getInt(Resource.APP_JettyWSPort, 0);
        String jettyDirectory = Resource.getValue(Resource.APP_JettyDirectory);
        String website = Resource.getValue(Resource.APP_Website);
        LOG.config(String.format("Jetty Server: %s, port: %d, sslport: %d, root directory: %s, website: %s", 
                jettyServer, jettyPort, jettySSLPort, jettyDirectory, website));
        
        LOG.config("STEP #1/2 BEGIN: connecting to core server");
        
        controlRemote = new RemoteClientController() {
            @Override
            protected void onDisconnect(Exception e) {
                LOG.log(Level.WARNING, "Exception with core server connection, will exit now", e);
                System.exit(3);
            }
        };
        try {
            controlRemote.start(server, serverPort);
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "STEP #1/2 FAILURE: Could not connect to core server, will exit", e);
            System.exit(1);
        }
        
        LOG.fine("Loading data from core server");
        ServerRoot serverRoot = RemoteDelegate.getRemoteApp().getServerRoot();
        int connectionId = OASync.getConnectionId();
        ClientRoot clientRoot = RemoteDelegate.getRemoteApp().getClientRoot(connectionId);

        LOG.fine("Initializing model");
        ModelDelegate.initialize(serverRoot, clientRoot);
        LOG.config("STEP #1/2 DONE: connected to core server");

        
        LOG.config("STEP #2/2 BEGIN: Starting Jetty Webserver");
        controlJetty = new JettyController();

        try {
            controlJetty.init(jettyPort, jettySSLPort, null);
            controlJetty.start();
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "STEP #2/2 FAILURE: Could not start webserver, will exit", e);
            System.exit(1);
        }
        

        LOG.config("STEP #2/2 DONE: Started Jetty Webserver");
        
        LOG.config(String.format("Webserver has been started, total time %,dms", (System.currentTimeMillis()-ms)));
        
        Desktop d = Desktop.getDesktop();
        if (d != null && OAString.isNotEmpty(website)) {
            String s = "http";
            int port = jettySSLPort;
            if (port > 0) s += "s"; 
            else port = jettyPort;
            s += "://"+website;
            if (port != 80 && port != 443) s += ":" + port;
            String s2 = Resource.getValue(Resource.APP_WelcomePage);
            // if (OAString.isNotEmpty(s2)) s += "/" + s2;
            LOG.config("opening browser to "+s);
            d.browse(new URI(s));
        }
        
        return true;
    }

    protected LogController getLogController() {
        if (controlLog != null) return controlLog;
        controlLog = new LogController(false);
        LogDelegate.setLogController(controlLog);
        
        // Have Logger remove all log files
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                int i1 = OAConv.toInt(Resource.getValue(Resource.APP_LogRegularDays, "14"));
                int i2 = OAConv.toInt(Resource.getValue(Resource.APP_LogErrorDays, "30"));
                LOG.config("removing regular log files > " + i1 + " days.");
                LOG.config("removing error log files > " + i2 + " days.");
                getLogController().removeOldLogFiles(i1, i2);
            }
        }, "LogController.removeOldLogFiles");
        t.start();
        return controlLog;
    }
	
    
    public static void main(String[] args) throws Exception {
        // Resource.setRootDir(".");
        Resource.loadArguments(args);
        Resource.setRunType(Resource.RUNTYPE_Webserver);
        WebserverController wsc = new WebserverController();
        wsc.start();
    }
}
