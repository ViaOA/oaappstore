// Copied from OATemplate project by OABuilder 11/19/19 12:32 PM
package com.viaoa.appstore.remote;

import com.viaoa.appstore.model.oa.*;
import com.viaoa.remote.multiplexer.annotation.OARemoteInterface;


/**
 * Client side remote interface that is 
 * used by server to call client Apps and load jar files from other applications.
 * 
 * @see Server/ClientJarStoreController  
 */
@OARemoteInterface
public interface RemoteClientJarStoreInterface {
    public final static String BindName = "JarStore";

    // see if this client has the release for a serverApp
    public int getRelease(ServerApplication serverApp) throws Exception;
    public void getJarFile(ApplicationVersion appVer) throws Exception;
}
