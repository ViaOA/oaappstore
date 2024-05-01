package com.viaoa.appstore.delegate.oa;

import java.util.logging.Logger;

import com.viaoa.appstore.model.oa.ClientApp;

public class ClientAppDelegate {
    private static Logger LOG = Logger.getLogger(ClientAppDelegate.class.getName());
    
    public static boolean run(final ClientApp clientApp) throws Exception {
        if (clientApp == null) return false;
        LOG.fine("Starting clientApp id="+clientApp.getId());
        return RunningAppDelegate.run(clientApp);
    }
}
