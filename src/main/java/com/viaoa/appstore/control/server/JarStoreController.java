package com.viaoa.appstore.control.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.viaoa.appstore.model.oa.*;
import com.viaoa.appstore.model.oa.propertypath.AppUserLoginPP;
import com.viaoa.appstore.model.oa.propertypath.EnvironmentPP;
import com.viaoa.appstore.delegate.ModelDelegate;
import com.viaoa.appstore.remote.RemoteClientJarStoreInterface;
import com.viaoa.hub.*;
import com.viaoa.object.OAFinder;
import com.viaoa.util.OADateTime;
import com.viaoa.util.OAString;

/**
 * Server side controller that manages JarStore
 * @author vvia
 */
public class JarStoreController {
    private static Logger LOG = Logger.getLogger(JarStoreController.class.getName());
    
    private final ConcurrentHashMap<Integer, RemoteClientJarStoreInterface> hmClientJarStore = new ConcurrentHashMap<>();  
    private final ConcurrentHashMap<ApplicationVersion, OADateTime> hmApplicationVersion = new ConcurrentHashMap<>();
    
    private volatile boolean bStop;
    

    public JarStoreController() {
        setupConsole();
        LOG.fine("JarStoreController created");
    }
    
    
    public void start() {
        LOG.fine("JarStoreController started");
        Hub<AppUserLogin> hubAppUserLogin = ModelDelegate.getConnectedAppUserLogins();
        
        HubListenerAdapter<AppUserLogin> hl = new HubListenerAdapter<AppUserLogin>() {
            @Override
            public void afterPropertyChange(HubEvent<AppUserLogin> e) {
                if ("xxx".equalsIgnoreCase(e.getPropertyName())) {
                    onNewAppUserLogin(e.getObject());
                }
                else if (AppUserLogin.P_ConnectionId.equalsIgnoreCase(e.getPropertyName())) {
                    onNewAppUserLogin(e.getObject());
                }
            }

            @Override
            public void afterAdd(HubEvent<AppUserLogin> e) {
                onNewAppUserLogin(e.getObject());
            }
            @Override
            public void onNewList(HubEvent<AppUserLogin> e) {
                for (AppUserLogin login : hubAppUserLogin) {
                    onNewAppUserLogin(login);
                }
            }
        };
        hubAppUserLogin.addHubListener(hl, "xxx", AppUserLoginPP.appUser().calcCheckVersions());
        hl.onNewList(null);
    }

    public void stop() {
        this.bStop = true;
    }
    
    protected void setupConsole() {
        Handler handler = new Handler() {
            public void publish(LogRecord record) {
                if (!isLoggable(record)) return;
                
                OADateTime dt = new OADateTime();
                String s2 = dt.toString("MM/dd HH:mm:ss");
                
                String s = s2 + " " + record.getMessage();
                Throwable t = record.getThrown();
                if (t != null) s += ", exception: "+t.toString();
                AppRuntime ar = ModelDelegate.getAppRuntime();
                if (ar != null) {
                    if (s.length() > 250) s = s.substring(0, 250);
                    ar.setConsole(s);
                }
            }
            public void close() throws SecurityException {
            }
            public void flush() {
            }
        };
        handler.setLevel(Level.FINE);
        
        LOG.setLevel(Level.FINE);
        LOG.addHandler(handler);
        
        String s = ServerJarStoreController.class.getName();        
        Logger log = Logger.getLogger(s);
        log.setLevel(Level.FINE);
        log.addHandler(handler);
        
        LOG.fine("console logging started for JarStore controller");
    }
    
    public String getDisplay(Application app) {
        if (app == null) return null;
        String display = "";
        Server server = app.getServer();
        if (server != null) display = OAString.concat(display, server.getDisplayName());
        display = OAString.concat(display, app.getFullName());
        
        return display;
    }
    
    
    
    protected void onNewAppUserLogin(final AppUserLogin appUserLogin) {
        if (appUserLogin == null) return;

        int connectionId = appUserLogin.getConnectionId();
        if (connectionId == 0) return;
        LOG.fine("appUserLogin.id="+appUserLogin.getId()+", connectionId="+connectionId);
        
        final RemoteClientJarStoreInterface rcs = hmClientJarStore.get(connectionId);
        if (rcs == null) {
            LOG.fine("NOTE: RemoteClientJarStoreInterface==null for connectionId="+connectionId);
            return;
        }
        
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    if (!bStop) {
                        runAppUserLoginThread(appUserLogin, rcs);
                    }
                }
                catch (Exception e) {
                    String s = "JarStoreController exception running AppUserLogin thread, connectionId="+connectionId;
                    LOG.log(Level.WARNING, s, e);
                }
                finally {
                }
            }
        }, "JarStoreController."+appUserLogin.getConnectionId());
        t.setDaemon(false);
        t.start();
    }
    protected void runAppUserLoginThread(final AppUserLogin appUserLogin, final RemoteClientJarStoreInterface rcs) throws Exception {
        AppUser user = appUserLogin.getAppUser();
        LOG.fine("checking releases for user="+user.getDisplayName());
        
        for (ApplicationType appType : user.getApplicationTypes()) {
            OAFinder<Environment, Application> finder = new OAFinder<Environment, Application>(ModelDelegate.getEnvironments(), EnvironmentPP.servers().applications().pp) {
                @Override
                protected void onFound(Application app) {
                    updateRelease(appUserLogin, app, rcs);
                }
            };
            finder.addEqualFilter(Application.P_ApplicationType, appType);
            finder.find();
        }
    }

    private final Object lockUpdateRelease = new Object();
    protected void updateRelease(final AppUserLogin appUserLogin, final Application app, final RemoteClientJarStoreInterface rcs) {
        if (bStop) return;
        if (app == null || rcs == null) return;
        LOG.fine("checking release for app="+app.getFullName());

        if (!app.getCanDownload()) {
            LOG.fine("WARNING: cant download, reason="+app.getCantDownloadMessage()+", app.name="+app.getFullName());
        }
        synchronized (lockUpdateRelease) {
            if (app.getCheckingVersion() != null) return;
            app.setCheckingVersion(new OADateTime());
        }
        try {
            _updateRelease(appUserLogin, app, rcs);
        }
        finally {
            synchronized (lockUpdateRelease) {
                app.setCheckingVersion(null);
            }
        }
    }    
    
    protected void _updateRelease(final AppUserLogin appUserLogin, final Application app, final RemoteClientJarStoreInterface rcs) {
        if (bStop) return;
        ApplicationVersion appVersion = null;
        
        int release = 0;
        try {
            release = rcs.getRelease(app);
        }
        catch (Exception e) {
            LOG.log(Level.FINE, "WARNING: updateRelease exception, app.name="+app.getFullName(), e);
            return;
        }

        LOG.fine("app="+app.getFullName()+", current installed release="+release);
        
        ApplicationType appType = app.getApplicationType();
        if (appType == null) return;
        if (OAString.isEmpty(appType.getDirectoryName())) return;

        appVersion = appType.getApplicationVersions().find(ApplicationVersion.P_Release, release);
        if (appVersion != null && appVersion.getCompleted() != null) {
            app.setApplicationVersion(appVersion);
            return;
        }
        if (appVersion != null && hmApplicationVersion.get(appVersion) != null) return;
        
        LOG.fine("will have client get jar file, release="+release+", app="+app.getFullName());
        
        if (appVersion == null) {
            appVersion = new ApplicationVersion();
            appVersion.getAppUserLogins().add(appUserLogin);
            appVersion.setRelease(release);
            app.getApplicationType().getApplicationVersions().add(appVersion);
        }
        appVersion.setStarted(new OADateTime());
        app.setApplicationVersion(appVersion);

        try {
            hmApplicationVersion.put(appVersion, new OADateTime());  // lock
            rcs.getJarFileForServer(app);
            appVersion.setCompleted(new OADateTime());
            LOG.fine("loaded on server, release="+release+", app="+app.getFullName());
            app.save();
        }
        catch (Exception e) {
            LOG.log(Level.FINE, "WARNING: updateRelease exception, app="+app.getFullName(), e);
        }
        finally {
            hmApplicationVersion.remove(appVersion);
        }
    }
    
    
    
    protected void onNewClientJarStore(RemoteClientJarStoreInterface clientJarStore, int connectionId) {
        LOG.fine("connectionId="+connectionId);
        
        hmClientJarStore.put(connectionId, clientJarStore);
        LOG.fine("hmClientJarStore.size="+hmClientJarStore.size());
    }

    protected boolean removeClientJarStoreInterface(int connectionId) {
        boolean b = hmClientJarStore.remove(connectionId) != null;
        LOG.fine("removed ClientJarStore remote object, connectionId="+connectionId+", hmClientJarStore.size="+hmClientJarStore.size()+", bExisted="+b);
        return b;
    }
    
}

