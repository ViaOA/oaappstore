// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.view.oa;

import java.util.logging.Logger;

import com.viaoa.hub.Hub;

public class OAModelJfcUtil {
    private static Logger LOG = Logger.getLogger(OAModelJfcUtil.class.getName());
    private static int cnt;
    
    public static void register(OAModelJfcInterface mji) {
        Hub h = mji.getHub();
        LOG.fine((++cnt)+") OAModelJfcUtil.register: creating object UI for "+h);
    }
    public static void setParent(OAModelJfcInterface mjiThis, OAModelJfcInterface mjiParent) {
        LOG.fine((cnt)+") OAModelJfcUtil.register: has a parent "+mjiThis.getHub());
    }
    
    // when a Jfc is used inside of a search, input editor, etc.
    public static void registerOther(OAModelJfcInterface mji) {
        Hub h = mji.getHub();
        LOG.fine((++cnt)+") creating other UI for "+h);
    }
    
    
}
