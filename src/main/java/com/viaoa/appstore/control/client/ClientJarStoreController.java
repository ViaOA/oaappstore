package com.viaoa.appstore.control.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Logger;

import com.viaoa.appstore.model.oa.*;
import com.viaoa.appstore.resource.Resource;
import com.viaoa.comm.multiplexer.OAMultiplexerClient;
import com.viaoa.object.OAObject;
import com.viaoa.util.*;

/**
 * Used to get jar files from other servers/applications and to work with main JarStore server (OAAppStore)
 * 
 * @author vvia
 */
public class ClientJarStoreController {
    private static Logger LOG = Logger.getLogger(ClientJarStoreController.class.getName());
    
    private static final String JarStoreSocketName = "JarStore";
    private static final String CMDSave = "save";
    private static final String CMDGet = "get";
    public static final String JarStoreDirectory = "jarStore";
    
    // connection to this server that has the JarStore
    private final OAMultiplexerClient multiplexerClient;
    
    public ClientJarStoreController(OAMultiplexerClient client) {
        if (client == null) throw new RuntimeException("MultiplexerClient can not be null");
        this.multiplexerClient = client;
    }

    /**
     * Get a file from the server.
     */
    public void getJarFromServer(final ApplicationVersion applicationVersion) throws Exception {
        if (applicationVersion == null) throw new Exception("ApplicationVersion can not be null");
        
        final ApplicationType appType = applicationVersion.getApplicationType();
        if (appType == null) throw new Exception("ApplicationType can not be null");

        LOG.fine(String.format("ApplicationType=%s, version=%s", appType.getName(), applicationVersion.getVersion()));
        
        String s = applicationVersion.getApplicationType().getJarFileName();
        s = Resource.getRootDirectory() + "/" + JarStoreDirectory + "/" + s;
        s = OAFile.convertFileName(s);
        File file = new File(s);

/*qqq        
        LOG.fine(String.format("ApplicationType=%s, version=%s, file=%s, exists=%b, file.length=%d, appVersion.fileLength=%d", 
            appType.getName(), applicationVersion.getVersion(), s, 
            file.exists(), file.length(), applicationVersion.getFileLength()));
*/        

//qqqqqqq        if (!file.exists() || file.length() != applicationVersion.getFileLength()) {
        if (!file.exists()) {
            final Socket socket = multiplexerClient.createSocket(JarStoreSocketName);
            final ObjectOutputStream oosSocket = new ObjectOutputStream(socket.getOutputStream());
            final ObjectInputStream oisSocket = new ObjectInputStream(socket.getInputStream());
            
            oosSocket.writeUTF(CMDGet);
            oosSocket.writeObject(applicationVersion);
            
            if (oisSocket.read() == 0) {
                socket.close();
                throw new Exception("file not found on server");
            }
            
            
            LOG.fine("loading file from server");
            OAFile.mkdirsForFile(file);
            OutputStream fos = new FileOutputStream(file);
            final byte[] bs = new byte[8196];
            for (;;) {
                int x = oisSocket.readInt();
                if (x <= 0) break;
                oisSocket.readFully(bs, 0, x);
                fos.write(bs, 0, x);
            }
            fos.close();
            oosSocket.writeInt(1);
            LOG.fine("done loading file from server, file.length="+file.length());
            socket.close();
        }
    }

    // get jar file from an application's server, store locally and send to server.
    public int getRelease(final ApplicationVersion application) throws Exception {
        if (application == null) throw new Exception("Application can not be null");
        int release = -1;
        /*qqqqq            
        try {
            application.setRunningNow(Application.RUNNINGNOW_UNKNOWN);
            release = _getRelease(application);
            application.setLastConnect(new OADateTime());
            application.setRunningNow(Application.RUNNINGNOW_YES);
        }
        catch (Exception e) {
            application.setRunningNow(Application.RUNNINGNOW_NO);
            throw e;
        }
        */
        return release;
    }
    
/*qqqqqqqqqq    
    protected int _getRelease(final Application application) throws Exception {
        if (application == null) throw new Exception("Application can not be null");
        final Server server = application.getServer();
        if (server == null) throw new Exception("Server can not be null");
        final ApplicationType appType = application.getApplicationType();
        if (appType == null) throw new Exception("ApplicationType can not be null");

        String host = server.getHost();
        if (OAString.isEmpty(host)) {
            host = server.getIpAddress();
        }
        
        int port = application.getClientPort();
        if (port < 1 && appType != null) {
            port = appType.getClientPort();
        }
        if (port < 1) port = 1099;
        
        LOG.fine(String.format("appType=%s, server=%s, port=%d", appType.getName(), host, port));
        
        OAMultiplexerClient mc = new OAMultiplexerClient(host, port);
        mc.start();
        
        final Socket socket = mc.createSocket("getJarFile");
        final ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        
        final int apiVersion = ois.readInt();
        final int release = ois.readInt();
        final String version = (String) ois.readUTF();
        String serverFileName = (String) ois.readUTF();  // file name on server
        final long fileLength = ois.readLong(); 
        LOG.fine(String.format("apiVersion=%d, release=%d, version=%s, serverFileName=%s, fileLength=%d", apiVersion, release, version, serverFileName, fileLength));

        socket.getOutputStream().write(1);  // continue
        socket.getOutputStream().flush();
        socket.close();
        mc.close();
        
        return release;
    }    
    
    // get jar file from an application's server, store locally and send to server.
    public ApplicationVersion getJarForServer(final Application application) throws Exception {
        if (application == null) throw new Exception("Application can not be null");
        final Server server = application.getServer();
        if (server == null) throw new Exception("Server can not be null");
        final ApplicationType appType = application.getApplicationType();
        if (appType == null) throw new Exception("ApplicationType can not be null");
        
        ApplicationVersion applicationVersion = null;        
        
        String host = server.getHost();
        if (OAString.isEmpty(host)) {
            host = server.getIpAddress();
        }
        
        int port = application.getClientPort();
        if (port < 1 && appType != null) {
            port = appType.getClientPort();
        }
        if (port < 1) port = 1099;
        
        LOG.fine(String.format("appType=%s, server=%s, port=%d", appType.getName(), host, port));
        
        OAMultiplexerClient multiplexerClientSource = new OAMultiplexerClient(host, port);
        multiplexerClientSource.start();
        
        final Socket socket = multiplexerClientSource.createSocket("getJarFile");
        final ObjectInputStream oisSource = new ObjectInputStream(socket.getInputStream());
        
        final int apiVersion = oisSource.readInt();
        final int release = oisSource.readInt();
        final String version = oisSource.readUTF();
        String serverFileName = oisSource.readUTF();  // file name on server
        final long fileLength = oisSource.readLong();
        
        // see if we already have the version-release
        // String fileName = String.format("%s/%s/%s-%s-%d.jar", JarStoreDirectory, appType.getDirectoryName(), appType.getJarFilename(), version, release);
        String fileName = String.format("%s/%s/%s/%d/%s", Resource.getRootDirectory(), JarStoreDirectory, appType.getDirectoryName(), release, serverFileName);
        
        LOG.fine(String.format("apiVersion=%d, release=%d, version=%s, serverFileName=%s, fileLength=%d, this.fileName=%s", apiVersion, release, version, serverFileName, fileLength, fileName));
        
        fileName = OAFile.convertFileName(fileName);
        File file = new File(fileName);
        
        if (file.exists() && file.length() == fileLength) {
            LOG.fine("file already exists and has correct fileLength, will not load from server");
            socket.getOutputStream().write(0); // 0=close (not needed)
            socket.getOutputStream().flush();
        }
        else {
            socket.getOutputStream().write(1); // continue
            socket.getOutputStream().flush();

            applicationVersion = appType.getApplicationVersions().find(ApplicationVersion.P_Release, release);
            if (applicationVersion == null) {
                applicationVersion = new ApplicationVersion();
                appType.getApplicationVersions().add(applicationVersion);
            }
            applicationVersion.setVersion(version);
            applicationVersion.setRelease(release);
            applicationVersion.setServerFileName(serverFileName);
            applicationVersion.setFileLength(fileLength);
            applicationVersion.save();
            LOG.fine("applicationVersion.id="+applicationVersion.getId()+", connecting to server to store the file");
            
            OAFile.mkdirsForFile(file);
            final byte[] bs = new byte[8196];
            OutputStream fos = new FileOutputStream(file);
            
            final Socket socketStoreJar = multiplexerClient.createSocket(JarStoreSocketName);
            final ObjectOutputStream oosStoreJar = new ObjectOutputStream(socketStoreJar.getOutputStream());
            final ObjectInputStream oisStoreJar = new ObjectInputStream(socketStoreJar.getInputStream());
            oosStoreJar.writeUTF(CMDSave);
            oosStoreJar.writeObject(applicationVersion);
            
            for (int i=0;;i++) {
                int x = oisSource.readInt();
                oosStoreJar.writeInt(x);
                if (x <= 0) break;
                
                oisSource.readFully(bs, 0, x);
                fos.write(bs, 0, x);
                
                oosStoreJar.write(bs, 0, x);
            }
            oosStoreJar.flush();
            fos.close();
            socket.getOutputStream().write(1); // done
            socket.getOutputStream().flush();
            oisStoreJar.read(); // wait for done writing
            socketStoreJar.close();
            LOG.fine("applicationVersion.id="+applicationVersion.getId()+", done sending file to server, fileLength="+file.length());
        } 
        socket.getOutputStream().close();
        multiplexerClientSource.close();
        return applicationVersion;
    }
*/    
    
/*qqqqqqq    
    public static void main(String[] args) throws Exception {
        OAObject.setDebugMode(true);
        Application application = new Application();
        
        ApplicationType appType = new ApplicationType();
        //appType.setJarFilename("HMAX");
        appType.setDirectoryName("HMAX");
        application.setApplicationType(appType);

        Server server = new Server(); 
        server.setHost("localhost");
        application.setServer(server);

        OAMultiplexerClient client = new OAMultiplexerClient("localhost", 1098);
        client.start();

        ClientJarStoreController cont = new ClientJarStoreController(client);
        
        cont.getJarForServer(application);
        
//        cont.getJarFileFromServer(appType.getApplicationVersions().getAt(0));
        
        System.out.println("DONE");
    }
*/    
}
