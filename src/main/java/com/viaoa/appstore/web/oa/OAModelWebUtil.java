// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.web.oa;

import java.util.logging.Logger;

import com.viaoa.hub.Hub;

public class OAModelWebUtil {
    private static Logger LOG = Logger.getLogger(OAModelWebUtil.class.getName());
    private static int cnt;
    
    public static void register(OAModelWebInterface mji) {
        Hub h = mji.getHub();
        LOG.fine((++cnt)+") OAModelWebUtil.register: creating object UI for "+h);
    }
    public static void setParent(OAModelWebInterface mjiThis, OAModelWebInterface mjiParent) {
        LOG.fine((cnt)+") OAModelWebUtil.register: has a parent "+mjiThis.getHub());
    }
    
    // when a Jfc is used inside of a search, input editor, etc.
    public static void registerOther(OAModelWebInterface mji) {
        Hub h = mji.getHub();
        LOG.fine((++cnt)+") creating other UI for "+h);
    }
    
    
}
