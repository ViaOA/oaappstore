// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.control.webserver;

import java.util.logging.Logger;
import java.util.concurrent.atomic.AtomicInteger;

import com.viaoa.appstore.datasource.DataSource;
import com.viaoa.appstore.model.oa.*;
import com.viaoa.appstore.resource.Resource;
import com.viaoa.datasource.clientserver.OADataSourceClient;
import com.viaoa.datasource.jdbc.db.Column;
import com.viaoa.datasource.jdbc.db.Database;
import com.viaoa.datasource.jdbc.db.Table;
import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAThreadLocalDelegate;
import com.viaoa.sync.OASyncClient;
import com.viaoa.util.OAFilter;
import com.viaoa.sync.OASync;

/**
	Connection to RemoteServer, that allows distributed method calls. 
	@see ServerDelegate#
*/
public abstract class RemoteClientController {

	private static final Logger LOG = Logger.getLogger(RemoteClientController.class.getName());
	
    private String serverName;
	private int port;
    private OASyncClient syncClient;

    public OASyncClient getSyncClient() {
        return syncClient;
    }
    
    public void start(String serverName, int port) throws Exception {
    	this.serverName = serverName;
    	this.port = port;
        LOG.config("creating DataSourceClient");

        // Create OADataSource to be used by OASyncClient
        final OADataSourceClient dsClient = new OADataSourceClient() {
            DataSource ds;

            public boolean willCreatePropertyValue(OAObject object, String propertyName) {
                return ("id".equalsIgnoreCase(propertyName));
            }
            @Override
            public boolean isAvailable() {
                return true;
            }
            @Override
            public boolean isClassSupported(Class clazz, OAFilter filter) {
                Database db = getDataSource().getDatabase();
                Table table = db.getTable(clazz);
                return table != null;
            }
            @Override
            public int getMaxLength(Class clazz, String propertyName) {
                Database db = getDataSource().getDatabase();
                Table table = db.getTable(clazz);
                if (table == null) return -1;
                Column col = table.getColumn(null, propertyName);
                if (col == null) return -1;
                return col.maxLength;
            }

            DataSource getDataSource() {
                if (ds != null) return ds;
                ds = new DataSource();
                return ds;
            }
        };

        LOG.config("connecting to RemoteServer "+serverName+", on port="+port);
        Package p = AppUser.class.getPackage();
    	syncClient = new OASyncClient(p, serverName, port) {
            private AtomicInteger aiDetailCnt = new AtomicInteger();
            private AtomicInteger aiCursorCnt = new AtomicInteger();
            
            @Override
            protected void onSocketClose(boolean bError) {
                super.onSocketClose(bError);
                onDisconnect(null);
            }
            @Override
            protected void onSocketException(Exception e) {
                super.onSocketException(e);
                onDisconnect(e);
            }
            @Override
            public OADataSourceClient getOADataSourceClient() {
                return dsClient;
            }
    	};
        LOG.config("Starting Client ...");
        syncClient.start();
        
        int x = Resource.getInt(Resource.APP_AppUpdateInterval);
        if (x > 0) {
            syncClient.startUpdateThread(x);
        }
        LOG.config("Client started, updateInterval set to "+x+" seconds");
    }
    
    public boolean isConnected() {
    	return (OASync.isConnected());
    }
    
	public void close() {
        try {
            if (syncClient != null) syncClient.stop();          
        }
        catch (Exception e) {
        }
	}


	protected abstract void onDisconnect(Exception e);
}
