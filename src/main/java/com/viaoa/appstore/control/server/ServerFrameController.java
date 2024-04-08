// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.control.server;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Point;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.Stack;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

import com.viaoa.util.*;
import com.viaoa.appstore.control.AboutController;
import com.viaoa.appstore.control.HelpController;
import com.viaoa.appstore.delegate.JfcDelegate;
import com.viaoa.appstore.resource.Resource;
import com.viaoa.appstore.view.server.ServerFrame;
import com.viaoa.datasource.OADataSource;
import com.viaoa.datasource.jdbc.OADataSourceJDBC;
import com.viaoa.jfc.*;
import com.viaoa.object.OAObjectCacheDelegate;

public abstract class ServerFrameController {
	private static Logger LOG = Logger.getLogger(ServerFrameController.class.getName());
	
    public static final String Frame_X         = "frame.x";    
    public static final String Frame_Y         = "frame.y";    
    public static final String Frame_Width     = "frame.width";    
    public static final String Frame_Height    = "frame.height";   
    public static final String Frame_Extended  = "frame.extended"; 
	
	private ServerFrame frm;
	
	private AboutController controlAbout;
    private HelpController  controlHelp;
    private TrayIcon trayIcon;

    public ServerFrameController() {
        /* only works with Metal, and it covers taskbar
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        */
        this.setLookAndFeel(null);
    }

    
	public synchronized ServerFrame getFrame() {
		if (frm != null) return frm;
        LOG.fine("creating frame");        
		this.setLookAndFeel(null);

		frm = new ServerFrame(Resource.getRunTimeName()) {
		    @Override
			public void onExit() {
                int i;
                if (Resource.getBoolean(Resource.INI_AutoLogout)) {
                    i = JOptionPane.YES_OPTION;
                }
                else {
                    i = JOptionPane.showConfirmDialog(getFrame(), "Ok to shut down " + Resource.getRunTimeName(), "Exit "+Resource.getValue(Resource.APP_ApplicationName), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                }
                if (i == JOptionPane.YES_OPTION) {
                    ServerFrameController.this.onExit();
                }
			}
		    @Override
			public void onSave() {
				ServerFrameController.this.onSave();
			}
		    @Override
			public void onAbout() {
		        ServerFrameController.this.getAboutController().setVisible(true);
			}
		    @Override
		    public void onDisplay() {
                ServerFrameController.this.onDisplay();
		    }
		};
        JfcDelegate.setMainWindow(frm);

        
		frm.getVersionLabel().setText("Version " + Resource.getValue(Resource.APP_Version) + "." + Resource.getValue(Resource.APP_Release));
		notifyStatusThread();

		// system tray button
        try {
            if (SystemTray.isSupported()) {
                SystemTray.getSystemTray().add(getTrayIcon());;
            }
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "error while addin trayIcon", e);
        } 
		
		
        // Help Settings
		getHelpController();
		controlHelp.setHelpMenuItem(frm.getHelpMenuItem(), HelpController.PAGE_Index);
		controlHelp.setHelpButton(frm.getHelpButton(), HelpController.PAGE_Index);
        // Context sensitive Help
		controlHelp.setCSHelpButton(frm.getCSHelpButton());
		controlHelp.setCSHelpMenuItem(frm.getCSHelpMenuItem());
        // Set up Context Sensitive Help for UI components
        // controlHelp.setCSHelpForComponent(frmServer.getSaveButton(), HelpController.PAGE_UserInterface);
	
        frm.getTrayHelpMenuItem().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getHelpController().getWindow().setVisible(true);
            }
        });
		
		
		getAboutController();
		
        try {
            // set frame position and size
            int x = OAConv.toInt(Resource.getValue(Frame_X, "0"));
            if (x != 0) {
                int y = OAConv.toInt(Resource.getValue(Frame_Y, "0"));
                int w = OAConv.toInt(Resource.getValue(Frame_Width, "0"));
                int h = OAConv.toInt(Resource.getValue(Frame_Height, "0"));
                Dimension dScr = Toolkit.getDefaultToolkit().getScreenSize();
                if (w > 0 && h > 0 && (x+w > 0) && (y+h>0) && y < dScr.height && x < dScr.width) frm.setBounds(x, y, w, h);
            }
            else {
                OAJfcUtil.pack(frm);
            }
            x = OAConv.toInt(Resource.getValue(Frame_Extended, JFrame.NORMAL+""));
            if (x >= 0) {
                frm.setExtendedState(x);
            }
        }
        catch (Exception e) {
        }
        LOG.fine("frame created");        
		
        return frm;
	}

	public TrayIcon getTrayIcon() {
	    if (trayIcon != null) return trayIcon;
        Image img = Resource.getJarIcon(Resource.getValue(Resource.IMG_AppServerIcon)).getImage();
        String s = Resource.getValue(Resource.APP_ApplicationName);
        TrayIcon trayIcon = new TrayIcon(img, s, frm.getTrayMenuBar());
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ServerFrameController.this.onDisplay();
                // getTrayIcon().displayMessage(Resource.getValue(Resource.APP_ApplicationName), "Ready", TrayIcon.MessageType.INFO);        
            }
        });
	    return trayIcon;
	}
	
	public String getInfo() {
        System.gc();
        Vector vec = new Vector(50);
        vec.addElement("Memory ============================");
//        vec.addElement(" Total: %,d"+OAString.format(Runtime.getRuntime().totalMemory(),"#,###"));;
        vec.addElement(String.format(" Total: %,d", Runtime.getRuntime().totalMemory()));;
        vec.addElement("  Free: "+OAString.format(Runtime.getRuntime().freeMemory(), "#,###"));
        vec.addElement("   Max: "+OAString.format(Runtime.getRuntime().maxMemory(), "#,###"));

        vec.addElement("Object Cache ============================");
		OAObjectCacheDelegate.getInfo(vec);
		
		vec.addElement("DataSource ============================");
        OADataSource[] oadss = OADataSource.getDataSources();
        for (int i=0; oadss != null && i < oadss.length; i++) {
            OADataSource oads = oadss[i]; 
            OADataSourceJDBC ds = null;
            if (oads instanceof OADataSourceJDBC) {
            	ds = (OADataSourceJDBC) oads;
                ds.getInfo(vec);
            }
        }
        StringBuilder sb = new StringBuilder(1024*8);
        int x = vec.size(); 
        for (int i=0; i<x; i++) {
            String s = (String) vec.elementAt(i);
            sb.append(s + "\r\n");
        }
        return new String(sb);
	}
	
	public HelpController getHelpController() {
		if (controlHelp == null) {
			controlHelp = new HelpController();
		}
		return controlHelp;
	}

	
	private Thread threadStatus;
	private long lastStatusUpdate;
	private Object LOCKStatus = new Object();
    public void setProcessing(boolean b) {
        setProcessing(b, "");
    }
    public void setProcessing(final boolean b, final String msg) {
        if (SwingUtilities.isEventDispatchThread()) {
            _setProcessing(b, msg);
        }
        else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        _setProcessing(b, msg);
                    }
                });
            }
            catch (Exception e) {
            }
        }
    }    
    private Stack<String> stackProcessingMessage = new Stack<String>();
    public synchronized void _setProcessing(boolean b, String msg) {
        boolean bEmpty;
        if (b) {
            if (msg == null) msg = "";
            stackProcessingMessage.add(msg);
            bEmpty = false;
        }
        else {
            if (stackProcessingMessage.isEmpty()) return;
            stackProcessingMessage.pop();

            bEmpty = stackProcessingMessage.isEmpty();
            if (!stackProcessingMessage.isEmpty()) {
                msg = stackProcessingMessage.pop();
            }
            else msg = null;
        }
        
        if (frm == null) return;

        if (!bEmpty) {
            frm.getGlassPaneLabel().setText(msg);
            frm.getGlassPane().setVisible(true);
            frm.getGlassPaneProgressBar().setIndeterminate(true);
        }
        else {
            frm.getGlassPaneProgressBar().setIndeterminate(false);
            frm.getGlassPane().setVisible(false);
        }
        frm.getProgressBar().setStringPainted(msg != null && msg.length() > 0);
        frm.getProgressBar().setString(msg);
        frm.getProgressBar().setIndeterminate(!bEmpty);
    }

    public void setStatus(String msg) {
		boolean b = (lastStatusUpdate == 0);
		lastStatusUpdate = (new Date()).getTime();
		if (b) lastStatusUpdate += 60000 * 5;
		if (frm != null) frm.getStatusLabel().setText(msg);
    }
	
	/** 
	 * Thread to update the status bar, clear message after 30 seconds, set current time
	 */
    protected void notifyStatusThread() {
        if (threadStatus != null) {
            synchronized (LOCKStatus) {
                LOCKStatus.notifyAll();
            }
            return;
        }
        
        threadStatus = new Thread("FrameController.StatusThread") {
            String[] msgs = new String[] {"Love","Joy","Peace","Hope","Goodness","Success"};
            public void run() {
                int max = getFrame().getProgressBar().getMaximum();
                for (int i=0;;i++) {
                    getFrame().getDateTimeLabel().setText( msgs[i%msgs.length] );
                    getFrame().getDateTimeLabel().setText((new OADateTime()).toString());
                    
                    if (stackProcessingMessage.isEmpty() && lastStatusUpdate != 0) {
                        if (System.currentTimeMillis() > (lastStatusUpdate + (30 * 1000))) {
                            getFrame().getStatusLabel().setText((i%2==0)?"Have Fun":"Smile :)");
                            getFrame().getStatusLabel().setText("");
                            lastStatusUpdate = 0;
                        }
                        if (getFrame().getCursor() != Cursor.getDefaultCursor()) {
//                            getFrame().setCursor(Cursor.getDefaultCursor());
                            LOG.warning("setting cursor to default, processingCounter="+stackProcessingMessage.size());
                        }
                    }
                    try {
                        synchronized (LOCKStatus) {
                            LOCKStatus.wait((31 * 1000));
                        }
                    }
                    catch (Exception e) {
                        System.out.println("ClientFrameController.Error: "+e);
                    }
                }
            }
        };
        threadStatus.setDaemon(true);
        threadStatus.setPriority(Thread.MIN_PRIORITY);
        threadStatus.start();
    }
	
    protected AboutController getAboutController() {
    	if (controlAbout == null) {
    		controlAbout = new AboutController(this.getFrame());
    	};
    	return controlAbout;
    }
	

    public void close() {
        if (frm == null) return;
        Point p = frm.getLocation();
        Resource.getServerProperties().load(); // reload, in case it was changed external to program
        Resource.setValue(Resource.TYPE_Server, Frame_X, p.x+"");
        Resource.setValue(Resource.TYPE_Server, Frame_Y, p.y+"");
        Dimension d = frm.getSize();
        Resource.setValue(Resource.TYPE_Server, Frame_Width, d.width+"");
        Resource.setValue(Resource.TYPE_Server, Frame_Height, d.height+"");
        Resource.setValue(Resource.TYPE_Server, Frame_Extended, frm.getExtendedState()+"");
        // Resource.save();
        frm.setVisible(false);
    }

    public void setLookAndFeel(String laf) {
        LOG.fine("L&F="+laf);
        boolean bWasNull = (laf == null);
        if (laf == null) {
            laf = Resource.getValue(Resource.APP_LookAndFeel, (String) null);
        }
        
        try {
            laf = OAJfcUtil.setLookAndFeel(laf);
            
            if (frm != null) SwingUtilities.updateComponentTreeUI(frm);
            if (controlAbout != null) getAboutController().updateUI();
            if (controlHelp != null) getHelpController().updateUI();
                         
            if (!bWasNull) {
                Resource.setValue(Resource.TYPE_Server, Resource.APP_LookAndFeel, laf);
                // Resource.save();
            }
            LOG.fine("L&F set to "+laf);
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "Error setting L&F to "+laf, e);
        }
        finally {
        }
    }
    

    protected void onDisplay() {
        if (frm != null) {
            int x = frm.getExtendedState();                    
            if ( (x & Frame.ICONIFIED) > 0) {
                frm.setExtendedState( (x ^ 1));
            }
            frm.toFront();
        }
    }
    
	protected abstract void onSave();
	protected abstract void onExit();

    public static void main(String[] args) {
        ServerFrameController control = new ServerFrameController() {
            @Override
            protected void onExit() {
                close();
                System.exit(0);
            }
            @Override
            protected void onSave() {
            }
        };
        control.getFrame().setVisible(true);
        try {
            control.setProcessing(true, "Processing test ...");
            Thread.sleep(4500);
            control.setProcessing(false);
        }
        catch (Exception e) {
        }
    }
}

