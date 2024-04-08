// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.control.server;

import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import com.viaoa.appstore.model.oa.AppUserLogin;
import com.viaoa.appstore.model.oa.cs.ClientRoot;
import com.viaoa.sync.model.ClientInfo;
import com.viaoa.util.OAString;

/**
 * Manages Client connections to the server.
 * @author vvia
 */
public class ConnectionController {

    private ConcurrentHashMap<Integer, ClientInfoEx> hmClientInfoEx = new ConcurrentHashMap<Integer, ClientInfoEx>();
    
    class ClientInfoEx {
        int connectionId;
        Socket socket;
        ClientInfo clientInfo;
        ClientRoot clientRoot;
        AppUserLogin userLogin;
    }
    
    public void add(int connectionId, Socket socket, ClientRoot clientRoot) {
        ClientInfoEx cix = hmClientInfoEx.get(connectionId);
        if (cix != null) return;
        cix = new ClientInfoEx();
        cix.connectionId = connectionId;
        cix.socket = socket;
        cix.clientRoot = clientRoot;
        cix.userLogin = null;
        hmClientInfoEx.put(connectionId, cix);        
    }
    public void remove(int connectionId) {
        hmClientInfoEx.remove(connectionId);        
    }

    public void update(ClientInfo ci) {
        if (ci == null) return;
        ClientInfoEx cix = hmClientInfoEx.get(ci.getConnectionId());
        if (cix == null) return;
        cix.clientInfo = ci;
        
        AppUserLogin userLogin = getAppUserLogin(ci.getConnectionId());
        if (userLogin == null) return;

        if (!OAString.isEmpty(ci.getHostName()))userLogin.setHostName(ci.getHostName());
        if (!OAString.isEmpty(ci.getIpAddress()))userLogin.setIpAddress(ci.getIpAddress());
        if (!OAString.isEmpty(ci.getUserName()))userLogin.setComputerName(ci.getUserName());
        if (!OAString.isEmpty(ci.getLocation())) userLogin.setLocation(ci.getLocation());
        userLogin.setTotalMemory(ci.getTotalMemory());
        userLogin.setFreeMemory(ci.getFreeMemory());
    }


    public void setUserLogin(int connectionId, AppUserLogin userLogin) {
        ClientInfoEx cix = hmClientInfoEx.get(connectionId);
        if (cix == null) return;
        cix.userLogin = userLogin;
    }
    
    public AppUserLogin getAppUserLogin(int connectionId) {
        ClientInfoEx cix = hmClientInfoEx.get(connectionId);
        if (cix == null) return null;
        return cix.userLogin;
    }
    
    public Socket getSocket(int connectionId) {
        ClientInfoEx cix = hmClientInfoEx.get(connectionId);
        if (cix == null) return null;
        return cix.socket;
    }
    public ClientInfo getClientInfo(int connectionId) {
        ClientInfoEx cix = hmClientInfoEx.get(connectionId);
        if (cix == null) return null;
        return cix.clientInfo;
    }
    
    public ClientRoot getClientRoot(int connectionId) {
        ClientInfoEx cix = hmClientInfoEx.get(connectionId);
        if (cix == null) return null;
        return cix.clientRoot;
    }
}
