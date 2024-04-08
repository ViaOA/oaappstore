// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.delegate;

import com.viaoa.appstore.model.*;
import com.viaoa.appstore.util.DataGenerator;
import com.viaoa.datasource.objectcache.OADataSourceObjectCache;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubListenerAdapter;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCallbackDelegate;
import com.viaoa.object.OAThreadLocalDelegate;
import com.viaoa.util.*;

public class TestDelegate {

    private OADataSourceObjectCache ds;
    private DataGenerator dg;

    public void createSampleData(String xx) {
        if (dg == null) {
            ds = new OADataSourceObjectCache();
            ds.setAssignIdOnCreate(true);
            dg = new DataGenerator();
            try {
                OAObjectCallbackDelegate.demoAllowAllToPass(true);
                dg.createSamples();
            }
            finally {
                OAObjectCallbackDelegate.demoAllowAllToPass(false);
            }
        }
    }
}



