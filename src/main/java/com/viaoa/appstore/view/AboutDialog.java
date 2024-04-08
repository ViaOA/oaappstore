// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.view;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import com.viaoa.appstore.resource.Resource;
import com.viaoa.jfc.*;
import com.viaoa.jfc.text.OATextController;

public abstract class AboutDialog extends JDialog implements ActionListener {

	private static final String CMD_Info         = "Info";
	private static final String CMD_Refresh      = "Refresh";
	private static final String CMD_Close        = "Close";

	public static final String CARD_Splash      = "Splash";
	public static final String CARD_Info        = "Info";
	
	private ImageIcon icon;

	private JLabel lblTitle, lblCopyright, lblSplash;
    private JTextArea txta;
    
    private CardLayout cardLayout;
    private JPanel panCard;
    private JButton cmdRefresh, cmdClose;
    private JToggleButton cmdInfo;

    public AboutDialog(JFrame frm) {
    	super(frm, "", false);
        this.setResizable(true);
        
        icon = Resource.getJarIcon(Resource.IMG_Splash);

        this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout(2,2));
        getContentPane().add(getPanel());
        pack();
        showPanel(CARD_Splash);
        this.setLocationRelativeTo(frm);
    }    

    public void showPanel(String name) {
        cardLayout.show(panCard, name);
    	getInfoButton().setSelected(name != null && name.equals(CARD_Info));
	}
    
    public JLabel getSplashLabel() {
    	if (lblSplash == null) {
    		lblSplash = new JLabel(icon, JLabel.CENTER);
    	}
    	return lblSplash;
    }
    
	public JLabel getTitleLabel() {
		if (lblTitle == null) {
			lblTitle = new JLabel();
			lblTitle.setHorizontalAlignment(JLabel.CENTER);
			lblTitle.setFont(new Font(getName(),0,12));
		}
		return lblTitle;
	}

	public JLabel getCopyrightLabel() {
		if (lblCopyright == null) {
			lblCopyright = new JLabel();
			lblCopyright.setHorizontalAlignment(JLabel.CENTER);
			lblCopyright.setFont(new Font(getName(),0,9));
		}
		return lblCopyright;
	}
	
	public JTextArea getTextArea() {
		if (txta == null) {
			txta = new JTextArea("", 3, 30);
	        txta.setLineWrap(false);
	        txta.setBorder(new EmptyBorder(8,8,5,5));
	        txta.setEditable(false);
            new OATextController(txta, null, false);
		}
		return txta;
	}

    public JButton getCloseButton() {
    	if (cmdClose == null) {
		    cmdClose = new JButton("Close");
		    cmdClose.setMnemonic(KeyEvent.VK_X);
	        cmdClose.registerKeyboardAction(this, CMD_Close, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
		    cmdClose.setActionCommand(CMD_Close);
		    cmdClose.addActionListener(this);
    	}
    	return cmdClose;
    }
	
	public JToggleButton getInfoButton() {
    	if (cmdInfo == null) {
		    cmdInfo = new JToggleButton("Info");
	        // OAButton.setup(cmdInfo);
		    cmdInfo.setActionCommand(CMD_Info);
		    cmdInfo.addActionListener(this);
		    cmdInfo.setToolTipText("Program Information");
    	}
    	return cmdInfo;
    }

	public JButton getRefreshButton() {
    	if (cmdRefresh == null) {
		    cmdRefresh = new JButton("Refresh");
	        OAButton.setup(cmdRefresh);
		    cmdRefresh.setActionCommand(CMD_Refresh);
		    cmdRefresh.addActionListener(this);
		    cmdRefresh.setToolTipText("Click here to Refresh Information");
    	}
    	return cmdRefresh;
    }
	
    protected JPanel getPanel() {
    	JPanel panMain = new JPanel(new BorderLayout(2,2));

        Border border;
        border = new EmptyBorder(3,3,3,3);
        panMain.setBorder(border);

        JPanel pan;
        
        // North Title
        pan = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 1));
        border = new EmptyBorder(3,3,3,3);
        pan.setBorder(border);
        
        pan.add(getTitleLabel());
        panMain.add(pan, BorderLayout.NORTH);

        // Center / CardPanel
        cardLayout = new CardLayout(5,5);
        panCard = new JPanel(cardLayout);
        //border = new CompoundBorder(new EtchedBorder(EtchedBorder.LOWERED), new EmptyBorder(2,2,2,2));
        border = new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new EmptyBorder(2,2,2,2));
        panCard.setBorder(border);

        JScrollPane sp = new JScrollPane(getTextArea());
        sp.setColumnHeaderView(getRefreshButton());

        panCard.add(sp, CARD_Info);
        panCard.add(getSplashLabel(), CARD_Splash);
        
        panMain.add(panCard, BorderLayout.CENTER);

        // Bottom Buttons and Copyright
        JPanel pan2 = new JPanel(new BorderLayout(2,2));
        
        pan = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 1));
        pan.add(getInfoButton());
        pan.add(getCloseButton());
        pan2.add(pan, BorderLayout.CENTER);
        
        pan = new JPanel(new BorderLayout(6,2));
        pan.add(getCopyrightLabel(), BorderLayout.CENTER);
        pan.add(new JLabel("   "), BorderLayout.WEST);
        pan.add(new JLabel("   "), BorderLayout.EAST);
        pan2.add(pan, BorderLayout.SOUTH);

        panMain.add(pan2, BorderLayout.SOUTH);
        return panMain;
    }
    
    public void actionPerformed(ActionEvent e) {
    	if (e == null) return;
        String cmd = e.getActionCommand();
    	if (cmd == null) return;
        if (cmd.equalsIgnoreCase(CMD_Info)) {
        	boolean b = getInfoButton().isSelected();
        	onShowInformation(b);
        }
        else if (cmd.equals(CMD_Refresh)) {
        	onRefreshInformation();
        }
        else if (cmd.equals(CMD_Close)) {
        	setVisible(false);
        }
    }

    protected abstract void onShowInformation(boolean bSelected);
    protected abstract void onRefreshInformation();
}


