// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.control.client;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import com.viaoa.appstore.remote.RemoteClientJarStoreInterface;
import com.viaoa.appstore.datasource.DataSource;
import com.viaoa.appstore.model.oa.*;
import com.viaoa.appstore.resource.Resource;
import com.viaoa.datasource.clientserver.OADataSourceClient;
import com.viaoa.datasource.jdbc.db.Column;
import com.viaoa.datasource.jdbc.db.Database;
import com.viaoa.datasource.jdbc.db.Table;
import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.object.OASiblingHelper;
import com.viaoa.object.OAThreadLocalDelegate;
import com.viaoa.sync.OASyncClient;
import com.viaoa.util.OAFile;
import com.viaoa.util.OAFilter;
import com.viaoa.util.OAString;
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
    private final ArrayList<OASiblingHelper> alSiblingHelperAWTThreadCache = new ArrayList<>();
    // Custom
    // private final RemoteClientJarStoreInterface remoteClientJarStore;

    // Custom
    /**
     * @param remoteClientJarStore will be sent to server when start is called.
     */
/*qqqqqqq    
    public RemoteClientController(RemoteClientJarStoreInterface remoteClientJarStore) {
        this.remoteClientJarStore = remoteClientJarStore;
    }
*/    
    
    public OASyncClient getSyncClient() {
        return syncClient;
    }
    
    public void start(String serverName, int port) throws Exception {
    	this.serverName = serverName;
    	this.port = port;
        LOG.config("creating RemoteClientController");

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
            public Object getDetail(final OAObject masterObject, final String propertyName) {
                int cnt = aiDetailCnt.incrementAndGet();
                //LOG.finer("OAClient.getDetail, masterObject=" + masterObject + ", propertyName=" + propertyName);

                boolean bUseSameThread = (getFrame() == null || !SwingUtilities.isEventDispatchThread());
                ArrayList<OASiblingHelper> alSiblingHelper = OAThreadLocalDelegate.getSiblingHelpers();

                if (!bUseSameThread && (alSiblingHelper != null)) {
                    for (OASiblingHelper sh : alSiblingHelper) {
                        if (bUseSameThread = sh.getUseSameThread()) break;
                    }
                }
                if (bUseSameThread) {
                    return super.getDetail(masterObject, propertyName);  // mostly called by background threads  
                }
                
                final int cntCursor = aiCursorCnt.incrementAndGet();
                getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                if (alSiblingHelper == null || alSiblingHelper.size() == 0) {
                    alSiblingHelper = alSiblingHelperAWTThreadCache;
                }
                else {
                    OASiblingHelper sh = alSiblingHelper.get(0);
                    if (!alSiblingHelperAWTThreadCache.contains(sh)) {
                        alSiblingHelperAWTThreadCache.add(0, sh);
                        if (alSiblingHelperAWTThreadCache.size() > 3) alSiblingHelperAWTThreadCache.remove(3);
                    }
                }

                final ArrayList<OASiblingHelper> alSiblingHelperX = alSiblingHelper;
                final boolean bForMerger = OAThreadLocalDelegate.isHubMergerChanging();
                
                SwingWorker<Object, Void> sw = new SwingWorker<Object, Void>() {
                    @Override
                    protected Object doInBackground() throws Exception {
                        Object obj;
                        try {
                            if (alSiblingHelperX != null) {
                                for (OASiblingHelper sh : alSiblingHelperX) {
                                    OAThreadLocalDelegate.addSiblingHelper(sh);
                                }
                            }
                            if (bForMerger) OAThreadLocalDelegate.setHubMergerChanging(true);
                            obj = getDetail(masterObject, propertyName);
                        }
                        finally {
                            if (bForMerger) OAThreadLocalDelegate.setHubMergerChanging(false);
                            if (alSiblingHelperX != null) {
                                for (OASiblingHelper sh : alSiblingHelperX) {
                                    OAThreadLocalDelegate.removeSiblingHelper(sh);
                                }
                            }
                        }
                        
                        ActionListener al = new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if (cntCursor == aiCursorCnt.get()) {
                                    getFrame().setCursor(null);
                                }
                            }
                        };
                        Timer timer = new Timer(150, al);  
                        timer.setRepeats(false);
                        timer.start();
                        return obj;
                    }
                };
                sw.execute();

                Object obj;
                for (;;) {
                    try {
                        obj = sw.get();  // wait for response
                        break;
                    }
                    catch (Exception e) { 
                        throw new RuntimeException("Error while getting Detail, Object="+masterObject.getClass().getSimpleName()+", property="+propertyName, e);
                    }
                }
                return obj;
            }
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

    /**
     * Get new uber jar from server and update the config file.
     */
    protected void onUpdateSoftwareForWindows(String serverVersion, int serverRelease) throws Exception {
        String s = "Server version "+serverVersion+" (release: "+serverRelease+"), current release="+Resource.getValue(Resource.APP_Release);
        LOG.fine(s);
        if (serverRelease < 201911182) {
            s = "Server version "+serverVersion+" (release: "+serverRelease+") does not support automated updates";
            s += ",\nserver will need to be updated before this program can run";
            throw new Exception(s);
        }

        final String rootDir = Resource.getRootDirectory();

        // see: RemoteServerController.updateClientSoftware
        final Socket socket = getSyncClient().getRemoteMultiplexerClient().getMultiplexerClient().createSocket("getJarFile");
        final ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        final byte[] bs = new byte[8196];

        ois.readInt();  // this api version
        ois.readInt();  // release
        ois.readUTF(); // version
        final String fileName = ois.readUTF();
        long fileLength = ois.readLong();
        
        socket.getOutputStream().write(1);  // continue

        File file = new File(OAFile.convertFileName(rootDir + "/" + fileName));
        if (file.exists()) file.delete();
        file.createNewFile();
        
        OutputStream fos = new FileOutputStream(file);
        for (;;) {
            int x = ois.readInt();
            if (x <= 0) break;
            ois.readFully(bs, 0, x);
            fos.write(bs, 0, x);
        }
        fos.close();
        
        socket.getOutputStream().write(1); // done
        socket.getOutputStream().flush();
        socket.close();
        
        // the jpackage creates a *.cfg file that needs to be changed for the new jar file
        File f = new File(rootDir);
        File[] files = f.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".cfg");
            }
        });
        if (files == null || files.length == 0) throw new Exception("cant find *.cfg file to set app.mainjar="+fileName);
        LOG.fine("setting "+files[0].getName()+" app.mainjar="+fileName);
        
        BufferedReader reader = new BufferedReader(new FileReader(files[0]));
        StringBuffer sb = new StringBuffer(2048);
        for (;;) {
            String line = reader.readLine();
            if (line == null) break;
            
            if (line.toLowerCase().indexOf("app.mainjar") >= 0) {
                // app.mainjar=$ROOTDIR\app\oabuilder.jar
                line = "app.mainjar=$ROOTDIR\\"+rootDir+"\\"+fileName;
            }
            else if (line.toLowerCase().indexOf("app.classpath") >= 0) {
                // app.classpath=$ROOTDIR\app\oabuilder.jar
                line = "app.classpath=$ROOTDIR\\"+rootDir+"\\"+fileName;
            }
            else if (line.toLowerCase().indexOf("app.version") >= 0) {
                line = "app.version="+serverVersion;
            }
            
            sb.append(line);
            sb.append(OAString.NL);
        }
        reader.close();
        OAFile.writeTextFile(files[0], sb.toString());
    }

	protected abstract void onDisconnect(Exception e);
    protected abstract JFrame getFrame();
}
