// Copied from OATemplate project by OABuilder 02/13/19 10:11 AM
package com.viaoa.appstore.remote;

import java.util.ArrayList;

import com.viaoa.appstore.remote.RemoteClientJarStoreInterface;
import com.viaoa.appstore.model.oa.AppUserLogin;
import com.viaoa.appstore.model.oa.cs.ClientRoot;
import com.viaoa.appstore.model.oa.cs.ServerRoot;
import com.viaoa.remote.multiplexer.annotation.OARemoteInterface;
import com.viaoa.util.OAProperties;

@OARemoteInterface
public interface RemoteAppInterface {

    public final static String BindName = "RemoteApp";

    public void saveData();
    
    public AppUserLogin getUserLogin(int clientId, String userId, String password, String location, String userComputerName);
    public ServerRoot getServerRoot();
    public ClientRoot getClientRoot(int clientId);

    public Object testBandwidth(Object data);
    public long getServerTime();
    public boolean disconnectDatabase();

    public String getRelease();
    
    public OAProperties getServerProperties();
    public String getResourceValue(String name);
    
    public boolean writeToClientLogFile(int clientId, ArrayList al);

    public boolean isRunningAsDemo();

    // Custom
    public void setClientJarStore(RemoteClientJarStoreInterface clientJarStore, int connectionId);
    
    // remote clients
    /*$$Start: RemoteAppInterface.remoteClient $$*/
    /*$$End: RemoteAppInterface.remoteClient $$*/
}
