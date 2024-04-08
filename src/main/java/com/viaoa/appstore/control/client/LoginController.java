// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.control.client;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.*;

import javax.swing.*;

import com.viaoa.appstore.control.HelpController;
import com.viaoa.appstore.delegate.RemoteDelegate;
import com.viaoa.appstore.model.*;
import com.viaoa.appstore.model.oa.AppUser;
import com.viaoa.appstore.model.oa.AppUserLogin;
import com.viaoa.appstore.resource.Resource;
import com.viaoa.appstore.view.client.LoginDialog;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.util.*;

public abstract class LoginController {
    private static Logger LOG = Logger.getLogger(LoginController.class.getName());
    
    public static final String MSG_InvalidLogin        = "control.view.LoginController.InvalidLogin";
    
    private LoginDialog dlgLogin;
    private HelpController controlHelp;
    private JFrame frmParent;
    
    private AppUserLogin userLogin;
    
    public LoginController(JFrame frm, HelpController controlHelp) {
        this.frmParent = frm;
        this.controlHelp = controlHelp;
    }

    // try to login using info in ini/properties file
    public boolean autoLogin() {
        getLoginDialog(); // this will set ui components, using ini file values
        return connectAndLogin();
    }
    
    public LoginDialog getLoginDialog() {
        if (dlgLogin != null) return dlgLogin;
        dlgLogin = new LoginDialog(frmParent) {
            public void onOk() {
                startLoginProcess();
    
                // Void is the return type, Point is the publish/process type
                SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
                    long ts;
                    String error;
                    AppUserLogin userLogin;
                    @Override
                    protected Void doInBackground() throws Exception {
                        ts = System.currentTimeMillis();
                        error = connectToServer();
                        if (error == null) {
                            this.userLogin = loginUser();
                        }
                        if (error == null && this.userLogin != null) {
                            SwingUtilities.invokeAndWait(new Runnable() {
                                @Override
                                public void run() {
                                    getLoginDialog().getProgressBar().setIndeterminate(false);
                                    getLoginDialog().getStatusLabel().setText("loading frame ...");
                                    getProgressBar().setString("connected to server");
                                }
                            });
                        }
                        return null;
                    }
                    @Override
                    protected void done() {
                        if (error != null) {
                            endLoginProcess();
                            JOptionPane.showMessageDialog(getLoginDialog(),"Error connecting to server\n"+error, "Login Failed", JOptionPane.ERROR_MESSAGE);
                        }
                        else if (this.userLogin == null) {
                            endLoginProcess();
                            JOptionPane.showMessageDialog(getLoginDialog(), "Invalid Login",  Resource.getRunTimeName(),JOptionPane.ERROR_MESSAGE);
                        }
                        else {
                            LoginController.this.userLogin = this.userLogin;
                            beforeEndingLoginProcess();
                            endLoginProcess();
                            ts = System.currentTimeMillis() - ts;
                            if (ts < 700) {
                                try {
                                    Thread.sleep(700 - ts);
                                }
                                catch (Exception e) {
                                }
                            }
                            getLoginDialog().setVisible(false);
                            LoginController.this.setUserLogin(this.userLogin);
                            String userId = getLoginDialog().getUserTextField().getText(); 
                            String location = getLoginDialog().getLocationTextField().getText(); 
                            LoginController.this.onLogin(userId, location);
                        }
                    }
                };
                sw.execute();
            }
            public void onExit() {
                LoginController.this.onExit();
            }
        };

        
        dlgLogin.setTitle(Resource.getRunTimeName() + " Login");
        String s = Resource.getValue(Resource.APP_Server);
        if (s == null) s = "";
        dlgLogin.getServerTextField().setText(s);
        
        s = Resource.getValue(Resource.APP_ServerPort);
        if (s == null) s = "";
        dlgLogin.getPortTextField().setText(s);
        
        s = Resource.getValue(Resource.INI_User);
        if (s == null || s.length() == 0) s = System.getProperty("user.name");
        dlgLogin.getUserTextField().setText(s);
        
        if (s.length() > 0) dlgLogin.getPasswordTextField().requestFocusInWindow();
        else dlgLogin.getUserTextField().requestFocusInWindow();
        
        s = Resource.getValue(Resource.INI_Password);
        if (!OAString.isEmpty(s)) {
            try {
                s = OAEncryption.decrypt(s);
            } 
            catch (Exception e) {
                LOG.log(Level.WARNING, "could not decrypt, value="+s, e);
            }
        }

        if (s == null) s = "";
        dlgLogin.getPasswordTextField().setText(s);
        s = Resource.getValue(Resource.INI_Location);
        if (s == null || s.length() == 0) s = System.getProperty("user.name") +" computer";
        dlgLogin.getLocationTextField().setText(s);

        if (controlHelp != null) {
            controlHelp.setHelpButton(dlgLogin.getHelpCommand(), HelpController.PAGE_Index);
            controlHelp.enableHelpKey(dlgLogin.getRootPane(), HelpController.PAGE_Index);
        }
        else {
            dlgLogin.getHelpCommand().setEnabled(false);
        }
        return dlgLogin;
    }

    private boolean bConnecting;
    public boolean connectAndLogin() {
        if (bConnecting) return false;
        bConnecting = true;

        boolean bResult = true; 
        try {
            startLoginProcess();

            String msg = connectToServer();
            if (msg != null) {
                JOptionPane.showMessageDialog(getLoginDialog(),"Error connecting to server\n"+msg, "Login Failed", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            AppUserLogin userLogin = loginUser();
            if (userLogin == null) {
                JOptionPane.showMessageDialog(getLoginDialog(), "Invalid Login",  Resource.getRunTimeName(),JOptionPane.ERROR_MESSAGE);
                return false;
            }
            this.userLogin = userLogin;
            
            setUserLogin(this.userLogin);
            String userId = getLoginDialog().getUserTextField().getText(); 
            String location = getLoginDialog().getLocationTextField().getText(); 
            onLogin(userId, location);
        }
        finally {
            endLoginProcess();
        }
        return true;
    }   

    private String holdLblStatusText;
    protected void startLoginProcess() {
        getLoginDialog().getOkButton().setEnabled(false);
        getLoginDialog().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        holdLblStatusText = getLoginDialog().getStatusLabel().getText();
        getLoginDialog().setProcessing(true, "connecting");
        getLoginDialog().getStatusLabel().setText("Connecting ...");
    }
    protected void beforeEndingLoginProcess() {
    }
    protected void endLoginProcess() {
        getLoginDialog().setProcessing(false, null);
        getLoginDialog().getStatusLabel().setText("");
        
        getLoginDialog().setCursor(Cursor.getDefaultCursor());
        getLoginDialog().getStatusLabel().setText(holdLblStatusText);
        getLoginDialog().getOkButton().setEnabled(true);
    }
    
    protected String connectToServer() {
        String server = getLoginDialog().getServerTextField().getText();
        String s = getLoginDialog().getPortTextField().getText();
        int port = 0;
        if (s != null && OAString.isNumber(s)) port = OAConv.toInt(s); 
            
        String msg = null;
        try {
            if (!onConnectToServer(server, port)) return "Could not connect to server";
        }
        catch (Exception e) {
            LOG.log(Level.CONFIG, "Exception connecting to server", e);
            return "Error: " + e;
        }
        return null;
    }
    
    protected AppUserLogin loginUser() {
        String location = getLoginDialog().getLocationTextField().getText(); 
        String s = Resource.getValue(Resource.INI_Location);
        if (location != null && (s == null || !s.equals(location))) {
            Resource.setValue(Resource.TYPE_Client, Resource.INI_Location, location);
        }
        
        String server = getLoginDialog().getServerTextField().getText(); 
        s = Resource.getValue(Resource.APP_Server);
        if (server != null && (s == null || !server.equals(s))) {
            Resource.setValue(Resource.TYPE_Client, Resource.APP_Server, server);
        }
        
        String port = getLoginDialog().getPortTextField().getText();
        s = Resource.getValue(Resource.APP_ServerPort);
        if (port != null && (s == null || !port.equals(s))) {
            Resource.setValue(Resource.TYPE_Client, Resource.APP_ServerPort, port);
        }
        
        
        String userId = getLoginDialog().getUserTextField().getText(); 
        String password = getLoginDialog().getPasswordTextField().getText(); 

        if (Resource.getBoolean(Resource.INI_StoreLogin) || Resource.getBoolean(Resource.INI_StorePassword)) {
            s = Resource.getValue(Resource.INI_User);
            if (userId != null && (s == null || !userId.equals(s))) {
                Resource.setValue(Resource.TYPE_Client, Resource.INI_User, userId);
            }
        }
        if (Resource.getBoolean(Resource.INI_StorePassword)) {
            String passwordx;
            try {
                passwordx = OAEncryption.encrypt(password);
            }
            catch (Exception e) {
                LOG.log(Level.WARNING, "", e);
                passwordx = null;
            }
            s = Resource.getValue(Resource.INI_Password);
            if (passwordx != null && (s == null || !passwordx.equals(s))) {
                Resource.setValue(Resource.TYPE_Client, Resource.INI_Password, passwordx);
            }
        }
        Resource.save();
        password = OAString.convertToSHAHash(password);
        int connectionId = OASyncDelegate.getConnectionId();
        AppUserLogin userLogin = RemoteDelegate.getRemoteApp().getUserLogin(connectionId, userId, password, location, System.getProperty("user.name"));
        return userLogin;
    }
    
    
    public AppUserLogin getUserLogin() {
        return userLogin;
    }
    
    protected abstract boolean onConnectToServer(String server, int port) throws Exception;
    protected abstract void onExit();
    protected abstract void onLogin(String user, String location);
    protected abstract void setUserLogin(AppUserLogin user);
}
