// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.control.server;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.logging.*;

import com.viaoa.hub.*;
import com.viaoa.object.*;
import com.viaoa.remote.*;
import com.viaoa.sync.*;
import com.viaoa.util.*;

import com.viaoa.appstore.delegate.*;
import com.viaoa.appstore.model.oa.*;
import com.viaoa.appstore.model.oa.trigger.*;

/**
 *  Created by Server to manage objects. 
 *
 *  Note: it's important to use:
    <code>
    try {
        b = OARemoteThreadDelegate.sendMessages(true);
        ... custom code here ...
    }
    finally {
        OARemoteThreadDelegate.sendMessages(b);
    }
    </code>
    or, OAThreadLocalDelegate.setSendMessages(..)
      
 *  so that changes are sent to other clients, even if thread is cs.OAClientThread
 */
public class ObjectController {
	private static Logger LOG = Logger.getLogger(ObjectController.class.getName());

    private ThreadPoolExecutor executorService;

    // thread to update orderStatus
    private AtomicInteger aiUpdateThread = new AtomicInteger();
    
	public ObjectController() {
	}

    public void start() {
        if (!OASync.isServer()) return;
        LOG.fine("starting");
        // getConnectionInfoController().start();
        
        autoCreate();
        startCustom();
        
        LOG.fine("completed start");
    }

    protected void autoCreate() {
        /*$$Start: ObjectController.autoCreate $$*/
        /*$$End: ObjectController.autoCreate $$*/
    }
    
    protected void startCustom() {
        startCustom2();
        /*$$Start: ObjectController.startCustom $$*/
        /*$$End: ObjectController.startCustom $$*/
    }
    
    protected void startCustom2() {
        // make sure that there is always a user with "admin=true"
        ModelDelegate.getAppUsers().addHubListener(new HubListenerAdapter() {
            @Override
            public void afterPropertyChange(HubEvent e) {
                if (!"admin".equalsIgnoreCase(e.getPropertyName())) return;
                AppUser admin = null;
                for (AppUser user : ModelDelegate.getAppUsers()) {
                    if (user.getAdmin()) return;
                    if ("admin".equalsIgnoreCase(user.getLoginId())) admin = user;
                }
                boolean b = OASync.sendMessages(true);
                if (admin == null) {
                    admin = new AppUser();
                    admin.setLoginId("admin");
                    admin.setPassword("admin");
                    ModelDelegate.getAppUsers().add(admin);
                }
                admin.setAdmin(true);
                OASync.sendMessages(b);
            }
        });

        
        // cleanup AppUserLogin to only show last day
        for (AppUser user : ModelDelegate.getAppUsers()) {
            for (AppUserLogin login : user.getAppUserLogins()) {
                if (login.getDisconnected() == null) login.setDisconnected(new OADateTime());
                OADateTime created = login.getCreated();
                if (created != null) {
                    OADateTime d1 = created.addDays(1);
                    OADateTime d2 = new OADateTime();
                    if (d1.after(d2)) {
                        if (login.getConnectionId() != 0) continue;
                        // dont need extra connection=0 (server) logins
                        d1 = created.addMinutes(3);
                        if (d1.after(d2)) continue;
                    }
                }
                if (ModelDelegate.getAppServer().getAppUserLogin() != login) {                
                    login.delete();
                }
            }
        }
    }
    
    public void stop() {
        LOG.fine("stopping");
        // getConnectionInfoController().stop();
        LOG.fine("completed stop");
    }

    /* example:
    public ConnectionInfoController getConnectionInfoController() {
        if (controlConnectionInfo == null) {
            controlConnectionInfo = new ConnectionInfoController() {
                @Override
                public void run(Runnable runable) {
                    getExecutorService().equals(runable);                    
                }
            };
        }
        return controlConnectionInfo;
    }
    */
	

    // thread pool to handle tasks that can run in the background.
    public ExecutorService getExecutorService() {
        if (executorService != null) return executorService;

        ThreadFactory tf = new ThreadFactory() {
            AtomicInteger ai = new AtomicInteger();
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("ObjectController.thread"+ai.getAndIncrement());
                t.setDaemon(true);
                t.setPriority(Thread.NORM_PRIORITY);
                return t;
            }
        };
        
        // min/max must be equal, since new threads are only created when queue is full
        executorService = new ThreadPoolExecutor(10, 10, 60L, TimeUnit.SECONDS, 
                new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE), tf) 
        {
            @Override
            public Future<?> submit(Runnable task) {
                LOG.fine("running task in thread="+Thread.currentThread().getName());
                return super.submit(task);
            }
        };
        executorService.allowCoreThreadTimeOut(true);
        
        return executorService;
    }

//    Executors.newCachedThreadPool()
    
    public static void main(String[] args) throws Exception {
        ObjectController oc = new ObjectController();
        oc.getExecutorService();
        int xx = 4;
        xx++;
    }
}


