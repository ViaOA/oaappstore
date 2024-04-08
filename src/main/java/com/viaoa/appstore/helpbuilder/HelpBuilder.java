// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.helpbuilder;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.swing.*;

import com.viaoa.appstore.control.client.ClientFrameController;
import com.viaoa.appstore.view.AboutDialog;
import com.viaoa.appstore.view.client.ClientFrame;
import com.viaoa.jfc.*;
import com.viaoa.image.OAImageUtil;
import com.viaoa.util.OAFile;

/**
 * This is used to create screen shots for all areas of the UI.
 * @author vvia
 */
public class HelpBuilder {
    private ClientFrameController control;
    private ClientFrame frm;
    private Robot robot;
    private File directory;
    
    public HelpBuilder(ClientFrameController control, File directory) {
        this.control = control;
        this.frm = control.getFrame();
        this.directory = directory;
        getRobot();
    }

    public Robot getRobot() {
        if (robot == null) {
            try {
                robot = new Robot();
                robot.setAutoDelay(5);
            }
            catch (Exception e) {
                throw new RuntimeException("error creating robot", e);
            }
        }
        return robot;
    }
    
    public JFrame getFrame() {
        return frm;
    }
    
    public void runRobot() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    runTest();
                }
                catch (Exception e) {
                    System.out.println("Error: "+e);
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    /**
     * Create screen shots of the UI.
     */
    protected boolean runTest() throws Exception {
        captureMain();
        captureMenu();
        captureAbout();
        captureOutlookbars();
        return true;
    }
    
    
    /**
     * This will use the glasspane to draw numbers on the different parts
     * of the main UI.
     */
    protected boolean captureMain() throws Exception {
        frm.setDocMode(true);
        frm.getDocGlassPanel().clear();
        frm.setGlassPane(frm.getDocGlassPanel());
        frm.getGlassPane().setVisible(true);
        
        // menubar
        Component comp = findComponent(frm.getContentPane(), JToolBar.class, 3, 0);
        if (comp != null) {
            Point pt = comp.getLocationOnScreen();
            mouseClick(pt.x + 115, pt.y + 9);
        }
        
        // number all outlookbars
        JButton[] cmds = getOutlookBarButtons();
        for (int barPos=0; barPos < cmds.length; barPos++) {
            mouseClick(cmds[barPos]);
        }        

        // number main panel
        Component panCard = getCurrentCardPanel();
        if (panCard != null) {
            Point pt = panCard.getLocationOnScreen();
            mouseClick(pt.x + 50, pt.y + 50);
        }
        
        capture();
        frm.setDocMode(false);
        frm.getGlassPane().setVisible(false);
        frm.setGlassPane(frm.getMyGlassPane());
        
        return true;
    }
    
    protected void captureMenu() throws Exception {
        JMenuBar mb = (JMenuBar) frm.getJMenuBar();
        int x = mb.getMenuCount();
        for (int i=0; i<x; i++) {
            JMenu menu = mb.getMenu(i);
            Point pt = menu.getLocationOnScreen();
            mouseClick(menu);
            
            Dimension d = menu.getSize();
            Dimension d2 = menu.getPopupMenu().getSize();
            capture(pt.x, pt.y, d2.width, d.height+d2.height);

            robot.keyPress(KeyEvent.VK_ESCAPE);
            robot.keyRelease(KeyEvent.VK_ESCAPE);
        }
    }

    protected void captureAbout() throws Exception {
        final AboutDialog dlg = control.getAboutController().getAboutDialog();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                dlg.setVisible(true);
            }
        });
        t.start();
        capture(dlg);
        control.getAboutController().setVisible(false);
    }
    

    protected boolean captureOutlookbars() throws Exception {
        OAOutlookBar bar = getOutlookBar();
        if (bar == null) return false;
        JButton[] cmds = getOutlookBarButtons();
        for (int barPos=0; barPos < cmds.length; barPos++) {
            showTreeNode(barPos);
            capture();
            
            Component c = bar.getVisibleComponent();
            c = findComponent(c, OATree.class, 4, 0);
            if  (c instanceof OATree) {
                OATree t = (OATree) c;
                t.collapseAll();
                
                int cnt = 0;
                for (OATreeNode tn : t.getRoot().getChildrenTreeNodes()) {
                    ArrayList<Integer> al = new ArrayList<Integer>();
                    al.add(cnt++);
                    captureOutlookbars2(barPos, tn, al);
                }
            }                
        }
        return true;
    }
    
    protected void captureOutlookbars2(int barPos, OATreeNode t, ArrayList<Integer> al) throws Exception {
        int[] ints = new int[al.size()];
        for (int i=0; i<al.size(); i++) {
            ints[i] = al.get(i);
        }
        
        if (!showTreeNode(barPos, ints)) return;
        
        capture();

        Component panCard = getCurrentCardPanel();

        // check for search panel. It will have a search button in a toolbar.        
        if (panCard != null) {
            for (int i=0; ;i++) {
                Component comp = findComponent(panCard, JToolBar.class, 4, i);
                if (comp == null) break;

                // first button on toolbar will be for search
                comp = findComponent(comp, JButton.class, 2, 0);
                if (comp == null) continue; 
                JButton cmd = (JButton) comp;
                if (!"Search ...".equalsIgnoreCase(cmd.getText())) continue;
                mouseClick(comp); // show search
                
                capture();
                robot.keyPress(KeyEvent.VK_ESCAPE);
                robot.keyRelease(KeyEvent.VK_ESCAPE);
                Thread.sleep(100);
                break;
            }
        }

        int cnt = 0;
        for (OATreeNode c : t.getChildrenTreeNodes()) {
            ArrayList<Integer> alx = (ArrayList<Integer>) al.clone();
            alx.add(cnt);
            captureOutlookbars2(barPos, c, alx);
            cnt++;
        }
    }

    private int ssId;
    protected void capture() {
        capture(frm);
    }
    protected void capture(Component comp) {
        try {
            Thread.sleep(100);
        }
        catch (Exception e) {
        }
        BufferedImage bi = robot.createScreenCapture(comp.getBounds());
        String fn = directory.getPath() + "\\ss"+(ssId++)+".jpg";
        File file = new File(fn);
        OAFile.mkdirsForFile(file);
        OAImageUtil.saveAsJpeg(bi, file);
    }
    protected void capture(int x, int y, int w, int h) {
        try {
            Thread.sleep(100);
        }
        catch (Exception e) {
        }
        Rectangle rect = new Rectangle(x,y,w,h);
        BufferedImage bi = robot.createScreenCapture(rect);
        
        File file = new File("c:\\temp\\helpbuilder\\ss"+(ssId++)+".jpg");
        if (!file.exists()) OAFile.mkdirsForFile(file);
        OAImageUtil.saveAsJpeg(bi, file);
    }

    
    public JComponent getCurrentCardPanel() {
        JComponent p = null;

        for (Component comp : frm.getCardPanel().getComponents() ) {
            if (comp.isVisible()) {
                p = (JComponent) comp;
                break;
            }
        }
        
        if (p == null) return null;
        
        for (Component comp : p.getComponents() ) {
            if (comp.isVisible()) {
                p = (JComponent) comp;
                break;
            }
        }                
        for (Component comp : p.getComponents() ) {
            if (comp.isVisible()) {
                p = (JComponent) comp;
                break;
            }
        }                
        return p;
    }

    protected boolean showTreeNode(int barPos, int... treePoses) throws Exception {
        OAOutlookBar bar = getOutlookBar();
        if (bar == null) return false;

        JButton[] cmds = getOutlookBarButtons();
        if (cmds == null || barPos >= cmds.length) return false;
        Point pt = cmds[barPos].getLocationOnScreen();

        mouseClick(pt.x+12, pt.y+12);
        Thread.sleep(75);

        if (treePoses == null || treePoses.length == 0) return true;
        
        Component c = bar.getVisibleComponent();
        c = findComponent(c, OATree.class, 4, 0);
        if  (!(c instanceof OATree)) return false;
            
        OATree t = (OATree) c;
        t.collapseAll();
        
        Point ptTree = t.getLocationOnScreen();
        
        int currentRow = 0;
        for (int i=0; i < treePoses.length;i++) {
            //Thread.sleep(75);
            currentRow += treePoses[i]; 
            if (i > 0) currentRow++; 
            int cnt = t.getRowCount();
            if (currentRow >= cnt) return false;
            
            Rectangle rect = t.getRowBounds(currentRow);
            int x = ptTree.x + rect.x + rect.width/2;
            int y = ptTree.y + rect.y + 8;
            
            if (i+1 == treePoses.length) mouseClick(x, y);
            else {
                mouseDoubleClick(x, y);
                int cnt2 = t.getRowCount();
                if (cnt == cnt2) return false;
            }
        }
        return true;
    }
    
    protected void mouseClick(Component comp) {
        if (comp == null) return;
        Point pt = comp.getLocationOnScreen();
        mouseClick(pt.x+2, pt.y+2);
    }
    protected void mouseClick(int x, int y) {
        Point pt = MouseInfo.getPointerInfo().getLocation();
        robot.mouseMove(x, y);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        robot.mouseMove(pt.x, pt.y);
    }
    protected void mouseDoubleClick(int x, int y) {
        Point pt = MouseInfo.getPointerInfo().getLocation();
        robot.mouseMove(x, y);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        robot.mouseMove(pt.x, pt.y);
    }
    
    public OAOutlookBar getOutlookBar() {
        OAOutlookBar bar = (OAOutlookBar) findComponent(frm.getContentPane(), OAOutlookBar.class, 0, 0);
        return bar;
    }
    public JButton[] getOutlookBarButtons() {
        ArrayList<JButton> al = new ArrayList<JButton>();
        OAOutlookBar bar = getOutlookBar();
        if (bar == null) return null;
        for (int i=0; ;i++) {
            JButton cmd = (JButton) findComponent(bar, JButton.class, 2, i);
            if (cmd == null) break;
            al.add(cmd);
        }
        JButton[] bs = new JButton[al.size()];
        al.toArray(bs);
        return bs;
    }
    
    
    
    class Finder {
        Class clazz;
        int posFind;
        int posCurrent;
        Component compFound;
        int maxDepth;
    }
    
    public Component findComponent(Class clazz, int pos) {
        Finder f = new Finder();
        f.clazz = clazz;
        f.posFind = pos;
        for (Component comp : frm.getContentPane().getComponents()) {
            findComponent(comp, f, 0);
            if (f.compFound != null) break;
        }
        return f.compFound;
    }
    public Component findComponent(Component compFrom, Class clazz, int maxDepth, int pos) {
        Finder f = new Finder();
        f.clazz = clazz;
        f.posFind = pos;
        f.maxDepth = maxDepth;
        findComponent(compFrom, f, 0);
        return f.compFound;
    }

    protected void findComponent(Component compFrom, Finder f, int currentDepth) {
        if (f.clazz.isAssignableFrom(compFrom.getClass())) {
            if (f.posCurrent == f.posFind) {
                f.compFound = compFrom;
                return;
            }
            f.posCurrent++;
        }
        if (!(compFrom instanceof Container)) {
            return;
        }
        if (f.maxDepth > 0 && currentDepth >= f.maxDepth) return;
        
        Container cont = (Container) compFrom; 
        for (Component comp : cont.getComponents()) {
            findComponent(comp, f, currentDepth+1);
            if (f.compFound != null) return;
        }        
    }
}

