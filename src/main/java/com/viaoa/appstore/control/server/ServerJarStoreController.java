package com.viaoa.appstore.control.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.appstore.model.oa.ApplicationType;
import com.viaoa.appstore.model.oa.ApplicationVersion;
import com.viaoa.comm.multiplexer.OAMultiplexerServer;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAFile;

/**
 * Server side controller for getting jar files from clients connected to other server,
 * so that the installer can auto-update new releases on other clients.
 * @author vvia
 */
public class ServerJarStoreController {
    private static Logger LOG = Logger.getLogger(ServerJarStoreController.class.getName());
    
    private static final String JarStoreSocketName = "JarStore";
    private static final String CMDSave = "save";
    private static final String CMDGet = "get";
    private static final String JarStoreDirectory = "jarStore";

    private OAMultiplexerServer multiplexerServer;
    private int cntSocket;
    private volatile boolean bStop;
    
    public ServerJarStoreController(OAMultiplexerServer multiplexerServer) {
        this.multiplexerServer = multiplexerServer;
    }

    public void stop() {
        this.bStop = true;
    }
    
    public void start() throws Exception {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    runJarStoreServerSocket();
                }
                catch (Exception e) {
                    LOG.log(Level.WARNING, "runJarStoreServerSocket() exception", e);
                }
            }
        }, JarStoreSocketName + "ServerSocket");
        t.setDaemon(true);
        t.start();
        LOG.fine("Started thread for ServerSocket to receive new jar files, name="+t.getName());
    }

    protected void runJarStoreServerSocket() throws Exception {
        final ServerSocket ss = multiplexerServer.createServerSocket(JarStoreSocketName);
        for ( ;; ) {
            final Socket socket = ss.accept();
            if (bStop) break;
            cntSocket++;
            
            LOG.fine("new socket connection for receiving Jar files, #"+cntSocket+", socket="+socket);
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        processJarStore(socket);
                    }
                    catch (Exception e) {
                        LOG.log(Level.FINE, "WARNING: processJarStore exception", e);
                    }
                }
            }, JarStoreSocketName+"Socket."+cntSocket);
            thread.start();
        }
    }
    
    protected void processJarStore(final Socket socket) throws Exception {
        final ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        final ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        
        String cmd = ois.readUTF();
        LOG.fine("ServerSocket received client command="+cmd);
        if (cmd == null);
        else if (cmd.equalsIgnoreCase(CMDSave)) saveJar(ois, oos);
        else if (cmd.equalsIgnoreCase(CMDGet)) getJar(ois, oos);
    }
    

    // Save jar file from Client
    protected void saveJar(final ObjectInputStream ois, final ObjectOutputStream oos) throws Exception {
/*qqqqqq        
        LOG.fine("ServerSocket saving jar file");
        ApplicationVersion applicationVersion = (ApplicationVersion) ois.readObject();
        LOG.fine("applicationVersion="+applicationVersion.getVersion());
        ApplicationType appType = applicationVersion.getApplicationType();
        if (appType == null) {
            LOG.warning("application type is null, cant process");
            return;
        }
        LOG.fine(String.format("ServerSocket saving jar file, Application=%s", appType.getName()));
        
        String s = applicationVersion.getFilePath();
        s = JarStoreDirectory + "/" + s;
        s = OAFile.convertFileName(s);
        LOG.fine(String.format("ServerSocket saving jar file, Application.file=%s", s));

        File file = new File(s);
        OAFile.mkdirsForFile(file);
        OutputStream fos = new FileOutputStream(file);
        
        final byte[] bs = new byte[8196];
        for (;;) {
            int x = ois.readInt();
            if (x <= 0) break;
            ois.readFully(bs, 0, x);
            fos.write(bs, 0, x);
        }
        fos.close();
        oos.write(1);
        oos.flush();
        LOG.fine(String.format("ServerSocket saved jar file, Application.file=%s", s));
*/        
    }

    // send Jar file to Client
    protected void getJar(final ObjectInputStream ois, final ObjectOutputStream oos) throws Exception {
/*qqqqqqqqqqqqqqqqqqq        
        LOG.fine("ServerSocket get jar file");
        ApplicationVersion applicationVersion = (ApplicationVersion) ois.readObject();
        LOG.fine("applicationVersion="+applicationVersion.getVersion());
        ApplicationType appType = applicationVersion.getApplicationType();
        if (appType == null) {
            LOG.warning("getJar, application type is null, cant process");
            return;
        }
        LOG.fine(String.format("ServerSocket get jar file, Application=%s", appType.getName()));

        String s = applicationVersion.getFilePath();
        s = JarStoreDirectory + "/" + s;
        s = OAFile.convertFileName(s);
        
        LOG.fine(String.format("ServerSocket get jar file, Application.file=%s", s));
        File file = new File(s);
        if (!file.exists()) {
            LOG.warning(String.format("ServerSocket get jar file, Application.file=%s does not exist", s));
            oos.write(0);
            return;
        }
        oos.write(1);
        
        final InputStream is = new FileInputStream(file);
        final BufferedInputStream bis = new BufferedInputStream(is);
    
        final byte[] bs = new byte[8196];
        for (int i=0;;i++) {
            int x = bis.read(bs, 0, bs.length);
            oos.writeInt(Math.max(x, 0));
            if (x <= 0) break;
            oos.write(bs, 0, x);
        }
        is.close();

        oos.flush();
        ois.readInt();  // wait for resposne
        LOG.fine(String.format("ServerSocket get jar file, Application.file=%s", s));
*/        
    }

    public static void main(String[] args) throws Exception {
        OAObject.setDebugMode(true);
        OAMultiplexerServer server = new OAMultiplexerServer(1098);
        server.start();

        ServerJarStoreController cont = new ServerJarStoreController(server);
        cont.start();
        
        for (;;) Thread.sleep(10 * 1000);
    }
}
