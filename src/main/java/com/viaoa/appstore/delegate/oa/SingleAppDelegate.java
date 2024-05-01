package com.viaoa.appstore.delegate.oa;

import java.util.logging.Logger;

import com.viaoa.appstore.model.oa.*;

public class SingleAppDelegate {
    private static Logger LOG = Logger.getLogger(SingleAppDelegate.class.getName());
    
    public static boolean run(final SingleApp singleApp) throws Exception {
        if (singleApp == null) return false;
        LOG.fine("Starting singleApp id="+singleApp.getId());
        return RunningAppDelegate.run(singleApp);
    }
}
