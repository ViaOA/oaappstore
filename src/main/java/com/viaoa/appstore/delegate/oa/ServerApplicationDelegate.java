package com.viaoa.appstore.delegate.oa;

import java.util.logging.Logger;

import com.viaoa.appstore.delegate.ModelDelegate;
import com.viaoa.appstore.model.oa.*;

public class ServerApplicationDelegate {

    private static Logger LOG = Logger.getLogger(ServerApplicationDelegate.class.getName());

    public static void runServer(ServerApplication sa) {
        if (sa == null) return;
        
        RunningApp runApp = new RunningApp();
        sa.setRunningApp(runApp);
    }
    
    
    /**
     * Create a new RunningApplication
     * @see ClientProcessController.ProcessController that has a listener that launches a process.
     */
    public static void runClient(ServerApplication sa) {
        if (sa == null) return;
        
        ClientApp ca = new ClientApp();
        ca.setServerApplication(sa);

        RunningApp runApp = new RunningApp();
        ca.setRunningApp(runApp);
        
        AppUserLogin appUserLogin = ModelDelegate.getLocalAppUserLogin();
        if (appUserLogin != null) {
            ca.setAppUser(appUserLogin.getAppUser());
        }
    }


    public static void run(ServerApplication serverApplication) {
        // TODO Auto-generated method stub
        
    }
}
