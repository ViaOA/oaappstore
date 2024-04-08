// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.control;

import java.net.*;
import java.util.logging.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.*;
import javax.help.*;

import com.viaoa.appstore.resource.Resource;
import com.viaoa.util.OALogger;

public class HelpController implements CSH.Manager {

    public static final String PAGE_Index = "index";
    public static final String PAGE_UserInterface = "runws.userinterface";
    
    private HelpSet mainHS;
    private HelpBroker mainHB;
    private Window window;
    
    private static Logger LOG = OALogger.getLogger(HelpController.class);
    
    public HelpController() {
        try {
            String s = Resource.getValue(Resource.APP_HelpPath1);
            URL url = HelpSet.findHelpSet(null, s);
            if (url == null) {
                s = Resource.getValue(Resource.APP_HelpPath2);
                url = getClass().getResource(s);
                if (url == null) {
                    s = Resource.getValue(Resource.APP_HelpJarPath);
                    url = new URL(s);
                }
            }
            LOG.fine("Help set to " + url);
            mainHS = new HelpSet(null, url);
            mainHB = mainHS.createHelpBroker();
            
            mainHB.initPresentation();  // so that the frame is created
            getWindow();
        } 
        catch (Exception ee) {
            LOG.log(Level.CONFIG, "Error loading Help files", ee);
        }
    }

    public Window getWindow() {
        if (window != null) return window;

        if (mainHB instanceof DefaultHelpBroker) {
            DefaultHelpBroker dhb = (DefaultHelpBroker) mainHB;
            
            window = dhb.getWindowPresentation().getHelpWindow();
            
            if (window != null) {
                for (int i=0; i<window.getComponentCount(); i++) {
                    JComponent comp = (JComponent) window.getComponent(i);
                    if (!(comp instanceof JComponent)) continue;
                    
                    comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "esc");
                    comp.getActionMap().put("esc", new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            HelpController.this.window.setVisible(false);
                        }
                    });
                    break;
                }
            }
        }
        return window;
    }
    
    public void setHelpButton(JButton cmd, String page) {
        if (cmd == null) return;
        mainHB.enableHelpOnButton(cmd, page, mainHS);
        
    }
    public void setHelpMenuItem(JMenuItem cmd, String page) {
        if (cmd == null) return;
        mainHB.enableHelpOnButton(cmd, page, mainHS);
    }

    public void setCSHelpButton(JButton cmd) {
        if (cmd == null) return;
        cmd.addActionListener(new CSH.DisplayHelpAfterTracking(mainHB));
    }
    public void setCSHelpMenuItem(JMenuItem cmd) {
        if (cmd == null) return;
        cmd.addActionListener(new CSH.DisplayHelpAfterTracking(mainHB));
    }


    /**
     * Used to display help when user selects the system help key (ex: F1)
     */
    public void enableHelpKey(Component comp, String page) {
        mainHB.enableHelp(comp, page, mainHS);
    }   

    /**
     * Used by context sensitive help, to "know" the help page to display for a component.
     * @param comp
     * @param page
     */
    public void setCSHelpForComponent(Component comp, String page) {
        CSH.setHelpIDString(comp, page);
    }

    public String getHelpIDString(Object arg0, AWTEvent arg1) {
        return null;
    }

    public HelpSet getHelpSet(Object arg0, AWTEvent arg1) {
        return mainHS;
    }
    public HelpBroker getHelpBroker() {
        return mainHB;
    }
    
    public void updateUI() {
        if (getWindow() != null) {
            SwingUtilities.updateComponentTreeUI(getWindow());
        }
    }
}
