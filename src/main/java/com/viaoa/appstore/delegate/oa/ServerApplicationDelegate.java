package com.viaoa.appstore.delegate.oa;

import java.util.logging.Logger;
import com.viaoa.appstore.model.oa.*;

public class ServerApplicationDelegate {
    private static Logger LOG = Logger.getLogger(ServerApplicationDelegate.class.getName());
    
    public static boolean run(final ServerApplication serverApplication) throws Exception {
        if (serverApplication == null) return false;
        LOG.fine("Starting ServerApplication id="+serverApplication.getId());
        return RunningAppDelegate.run(serverApplication);
    }
}
