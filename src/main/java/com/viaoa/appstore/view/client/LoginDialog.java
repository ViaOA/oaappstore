// Copied from OATemplate project by OABuilder 03/28/24 02:38 PM
package com.viaoa.appstore.view.client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import com.viaoa.appstore.resource.Resource;
import com.viaoa.jfc.*;

public abstract class LoginDialog extends JDialog {

	private JTextField txtUser, txtServer, txtPort, txtLocation;
	private JPasswordField txtPassword;
	private JButton cmdHelp;
    private JProgressBar progressBar;
    private JLabel lblStatus;
    private JButton cmdOk;
	

    public LoginDialog(JFrame frame) {
        super(frame, Resource.getRunTimeName()+ " Login", true);  // make modal so that calling code will wait
        this.setResizable(false);
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        this.setIconImage(Resource.getJarIcon(Resource.getValue(Resource.IMG_AppClientIcon)).getImage());
        
        this.addWindowListener(new WindowAdapter() {
        	public void windowClosing(WindowEvent e) {
        		super.windowClosing(e);
        		onExit();
        	}
        });

        getContentPane().setLayout(new BorderLayout());
        
        Class c = getClass();
        Icon icon = Resource.getJarIcon(Resource.getValue(Resource.IMG_Login));
        JLabel jlbl = new JLabel(icon);
        getContentPane().add(jlbl, BorderLayout.NORTH);
        
        getContentPane().add(getPanel(), BorderLayout.CENTER);
        
        getContentPane().add(getStatusPanel(), BorderLayout.SOUTH);
        
        pack();

        Dimension d = getSize();
        d.width += 15;
        d.height += 10;
        setSize(d);
        
        setLocationRelativeTo(frame);
        /*
        Dimension d = getPreferredSize(); 
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (dim.width/2) - (d.width);
        int y = (dim.height/2) - (d.height);
        setLocation(x,y);
        */
    }    

    @Override
    public void setVisible(boolean b) {
        getStatusLabel().setText("Login to server");
        super.setVisible(b);
    }
   
    
    private JPanel pan;
    protected JPanel getPanel() {
    	if (pan != null) return pan;
    	
        pan = new JPanel(new GridBagLayout());
    	pan.setBorder(new EmptyBorder(5,5,5,15));
        
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;         
        gc.insets = new Insets(2,10,0,0);
        JLabel jlbl;
        OALabel lbl;
        OATextField txt;
        
        // Login Id
        jlbl = new JLabel("Login Id:");
        pan.add(jlbl,gc);
        
        gc.gridwidth = GridBagConstraints.REMAINDER; 
        pan.add(getUserTextField(), gc);
        gc.gridwidth = 1;
        
        // Password
        jlbl = new JLabel("Password:");
        pan.add(jlbl,gc);
        
        gc.gridwidth = GridBagConstraints.REMAINDER; 
        pan.add(getPasswordTextField(), gc);
        gc.gridwidth = 1;

        // Server
        jlbl = new JLabel("Server:");
        pan.add(jlbl,gc);
        
        gc.gridwidth = GridBagConstraints.REMAINDER; 
        pan.add(getServerTextField(), gc);
        gc.gridwidth = 1;

        jlbl = new JLabel("Port:");
        pan.add(jlbl,gc);
        gc.gridwidth = GridBagConstraints.REMAINDER; 
        pan.add(getPortTextField(), gc);
        gc.gridwidth = 1;
        
        // Location
        jlbl = new JLabel("Your Location:");
        pan.add(jlbl,gc);
        
        gc.gridwidth = GridBagConstraints.REMAINDER; 
        pan.add(getLocationTextField(), gc);
        gc.gridwidth = 1;

        
        JPanel panCommand = new JPanel();

        panCommand.add(getOkButton());

        JButton cmd = new JButton("Quit");
        ActionListener al = new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		onExit();
        	}
        };
        cmd.addActionListener(al);
        cmd.registerKeyboardAction(al, "cmdQuit", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
        cmd.setMnemonic('Q');
        panCommand.add(cmd);

        panCommand.add(getHelpCommand());
        
        gc.insets = new Insets(15,10,0,0);
        gc.gridwidth = GridBagConstraints.REMAINDER; 
        gc.anchor = GridBagConstraints.CENTER;
        pan.add(panCommand, gc);
        gc.gridwidth = 1;
        return pan;
    }

    public JButton getOkButton() {
        if (cmdOk == null) {
            cmdOk = new JButton("OK");
            ActionListener al = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onOk();
                }
            };
            cmdOk.addActionListener(al);
            cmdOk.registerKeyboardAction(al, "cmdOK", KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
            cmdOk.setMnemonic('O');
        }
        return cmdOk;
    }
    
    public JTextField getUserTextField() {
        if (txtUser == null) txtUser = new JTextField(12);
        return txtUser;
    }
    public JPasswordField getPasswordTextField() {
        if (txtPassword == null) txtPassword = new JPasswordField(12);
        return txtPassword;
    }
    public JTextField getServerTextField() {
        if (txtServer == null) txtServer = new JTextField(12);
        return txtServer;
    }
    public JTextField getPortTextField() {
        if (txtPort == null) txtPort = new JTextField(5);
        return txtPort;
    }
    public JTextField getLocationTextField() {
        if (txtLocation == null) txtLocation = new JTextField(12);
        return txtLocation;
    }
    
    public JButton getHelpCommand() {
    	if (cmdHelp == null) {
	    	cmdHelp = new JButton("Help");
	        cmdHelp.setIcon(Resource.getJarIcon(Resource.IMG_Help));
	        cmdHelp.setMnemonic('H');

	        
	        cmdHelp.registerKeyboardAction(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
	                JButton cmd = LoginDialog.this.getHelpCommand(); 
	                if (cmd.isEnabled()) cmd.doClick();
	            }
	        }, "cmdOK", KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);

            
	        
    	}    	
    	return cmdHelp;
    }
    
    
    public void setProcessing(boolean b, String msg) {
        getProgressBar().setStringPainted(msg != null && msg.length() > 0);
        getProgressBar().setString(msg);
        getProgressBar().setIndeterminate(b);
    }

    public JProgressBar getProgressBar() {
        if (progressBar == null) {
            progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 20);
        }
        return progressBar;
    }

    public JLabel getStatusLabel() {
        if (lblStatus == null) {
            lblStatus = new JLabel(" ") {
                public void setText(String s) {
                    if (s == null) s = "";
                    for (int i=s.length(); i<20; i++) s += " ";
                    super.setText(s);
                }
            };
        }
        return lblStatus;
    }
    
    protected JPanel getStatusPanel() {
        JPanel p = new JPanel(new GridLayout(1, 2, 3, 3));
        
        JLabel lbl = getStatusLabel();
        lbl.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new EmptyBorder(0,6,0,8)));
        p.add(lbl);
        
        Border border = new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new EmptyBorder(1,1,1,1));
        
        getProgressBar().setBorder(new CompoundBorder(border, progressBar.getBorder()));
        Font font = getProgressBar().getFont();
        getProgressBar().setFont(font.deriveFont(10.0f));
        p.add(getProgressBar());
        
        return p;
    }

    
    
    protected JPanel getStatusPanel_WAS() {
        JPanel p = new JPanel(new GridBagLayout());
        
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0,0,0,0);

        gc.weightx = 1.0;
        gc.weighty = 1.0;
        
        JLabel lbl = getStatusLabel();
        lbl.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new EmptyBorder(0,6,0,8)));
        gc.fill = gc.HORIZONTAL;
        p.add(lbl, gc);
        gc.fill = gc.NONE;
        
        gc.fill = gc.BOTH;
        Border border = new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new EmptyBorder(1,1,1,1));
        
        getProgressBar().setBorder(new CompoundBorder(border, progressBar.getBorder()));
        Font font = getProgressBar().getFont();
        getProgressBar().setFont(font.deriveFont(10.0f));
        p.add(getProgressBar(), gc);
        
        return p;
    }


    public abstract void onOk();
    public abstract void onExit();
    
    public static void main(String[] args) {
        LoginDialog dlg = new LoginDialog(null) {
            @Override
            public void onExit() {
                System.exit(0);
            }
            @Override
            public void onOk() {
                // TODO Auto-generated method stub
            }
        };
        dlg.getHelpCommand().setEnabled(false);
        dlg.setVisible(true);
    }

    
}
