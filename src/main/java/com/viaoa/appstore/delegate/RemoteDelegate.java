// Copied from OATemplate project by OABuilder 02/13/19 10:11 AM
package com.viaoa.appstore.delegate;

import java.util.logging.*;
import com.viaoa.appstore.control.server.ConnectionController;
import com.viaoa.appstore.model.oa.AppUserLogin;
import com.viaoa.appstore.remote.*;
import com.viaoa.sync.*;

/**
  This class is used for interactions between workstations (clients) and server.
 */
public class RemoteDelegate {
	private static Logger LOG = Logger.getLogger(RemoteDelegate.class.getName());

    private static RemoteAppInterface remoteApp; 
	private static RemoteFileInterface remoteFile;
    private static RemoteSpellCheckInterface remoteSpellCheck;

    private static ConnectionController controlConnection;
    
    // Remote Clients
    /*$$Start: RemoteDelegate.remoteClient1 $$*/
    /*$$End: RemoteDelegate.remoteClient1 $$*/

    public static void setRemoteApp(RemoteAppInterface rai) {
        remoteApp = rai;
    }
    public static RemoteAppInterface getRemoteApp() {
        if (remoteApp != null) return remoteApp;
        
        OASyncClient sc = OASyncDelegate.getSyncClient();
        try {
            remoteApp = (RemoteAppInterface) sc.lookup(RemoteAppInterface.BindName);
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "exception getting remote object", e);
        }
        return remoteApp;
    }

    // set by ServerController
    public static void setRemoteSpellCheck(RemoteSpellCheckInterface remoteSpellCheck) {
        RemoteDelegate.remoteSpellCheck = remoteSpellCheck;
    }
    public static RemoteSpellCheckInterface getRemoteSpellCheck() {
        if (remoteSpellCheck != null) return remoteSpellCheck;
        OASyncClient sc = OASyncDelegate.getSyncClient();
        try {
            remoteSpellCheck = (RemoteSpellCheckInterface) sc.lookup(RemoteSpellCheckInterface.BindName);
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "exception getting remote object", e);
        }
        return remoteSpellCheck;
    }
	
    // set by ServerController
    public static void setRemoteFile(RemoteFileInterface remoteFile) {
        RemoteDelegate.remoteFile = remoteFile;
    }
    public static RemoteFileInterface getRemoteFile() {
        if (remoteFile != null) return remoteFile;
        OASyncClient sc = OASyncDelegate.getSyncClient();
        try {
            remoteFile = (RemoteFileInterface) sc.lookup(RemoteFileInterface.BindName);
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "exception getting remote object", e);
        }
        return remoteFile;
    }

    /**
     * used on the server, and set by serverController startup
     */
    public static void setConnectionController(ConnectionController cc) {
        controlConnection = cc;
    }
    public static ConnectionController getConnectionController() {
        return controlConnection ;
    }
    /**
     * Used on the server, to get the user that made the current thread's remote method call.
     */
    public static AppUserLogin getRemoteRequestAppUserLogin() {
        if (controlConnection == null) return null;
        int cid = OASync.getRequestConnectionId();
        if (cid < 0) cid = 0;
        
        AppUserLogin userLogin = controlConnection.getAppUserLogin(cid);
        return userLogin;
    }
    
    
    // Remote Clients ========= set by ServerController
    /*$$Start: RemoteDelegate.remoteClient2 $$*/
    /*$$End: RemoteDelegate.remoteClient2 $$*/
    
}
