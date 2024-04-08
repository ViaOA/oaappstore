// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.view;

import java.awt.*;
import javax.swing.*;

// import com.sun.awt.AWTUtilities;
import com.viaoa.appstore.resource.*;

public class SplashWindow extends JWindow {
    ImageIcon icon;
    int amount = 21;
    
    public SplashWindow() {
        icon = Resource.getJarIcon(Resource.getValue(Resource.IMG_Splash));
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
    
        // http://today.java.net/pub/a/today/2008/03/18/translucent-and-shaped-swing-windows.html
        // AWTUtilities.setWindowOpaque(this, false);
        // this.setAlwaysOnTop(true);
    }

    public void paint(Graphics g) {
        if (icon != null) {
            int w = icon.getIconWidth() + amount;
            int h = icon.getIconHeight() + amount;
        	paintBorder(getContentPane(),g,0,0, w, h);
            icon.paintIcon(getContentPane(),g,0,0);
        }
    }
    
    public void paintBorder(Component c, Graphics gr, int x, int y, int w, int h) {
        Graphics2D g = (Graphics2D) gr;
        Color shadow = Color.DARK_GRAY;
        
        g.translate(x, y);
        for (int i=0; i<amount; i++) {
        	int alpha = (amount-i) * (254/amount);
            Color color = new Color(shadow.getRed(),
                    shadow.getGreen(),
                    shadow.getBlue(),
                    alpha);
            g.setColor(color);
            // right
        	g.fillRect((w-amount)+i, i+3, 1, (h-amount)-3); 
        	// bottom
        	g.fillRect(i+3, (h-amount)+i, (w-amount)-2, 1); 
        }
        g.translate(-x, -y);
    }
    


    public void addNotify() {
        super.addNotify();
        _resize();
    }
    protected void _resize() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int w = icon.getIconWidth();
        int h = icon.getIconHeight();
        int x = (dim.width/2) - (w/2);
        int y = (dim.height/2) - (h/2);
        setBounds(x, y, w+amount,h+amount); 
    }        

    public static void main(String[] args) {
    	SplashWindow w = new SplashWindow();
    	w.setVisible(true);
    }
    
}
