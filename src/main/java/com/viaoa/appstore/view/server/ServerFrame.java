// Copied from OATemplate project by OABuilder 03/03/24 01:27 PM
package com.viaoa.appstore.view.server;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

import com.viaoa.appstore.delegate.JfcDelegate;
import com.viaoa.appstore.delegate.ModelDelegate;
import com.viaoa.appstore.model.AppServerModel;
import com.viaoa.appstore.model.AppUserErrorModel;
import com.viaoa.appstore.model.AppUserLoginModel;
import com.viaoa.appstore.model.AppUserModel;
import com.viaoa.appstore.model.oa.AppServer;
import com.viaoa.appstore.model.oa.AppUser;
import com.viaoa.appstore.model.oa.AppUserError;
import com.viaoa.appstore.model.oa.AppUserLogin;
import com.viaoa.appstore.resource.Resource;
import com.viaoa.appstore.view.oa.AppServerJfc;
import com.viaoa.appstore.view.oa.AppUserErrorJfc;
import com.viaoa.appstore.view.oa.AppUserJfc;
import com.viaoa.appstore.view.oa.AppUserLoginJfc;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubListenerAdapter;
import com.viaoa.jfc.OACommand;
import com.viaoa.jfc.OAJfcUtil;
import com.viaoa.jfc.control.CutCopyPasteController;
import com.viaoa.jfc.control.UndoableController;
import com.viaoa.util.OADateTime;

public abstract class ServerFrame extends JFrame implements ActionListener {
	private final int TAB_Home = 0;
	private final int TAB_ServerInfo = 1;
	private final int TAB_Connections = 2;
	private final int TAB_ProgramSettings = 3;

	private final String CMD_Display = "Display";
	private final String CMD_Save = "Save";
	private final String CMD_Exit = "Exit";
	private final String CMD_About = "About";
	private final String CMD_Help = "Help";

	private JLabel lblVersion;
	private JLabel lblLastTime;
	private JLabel lblLastTime2;

	private JToolBar toolBar;

	private UndoableController controlUndoable;
	private CutCopyPasteController controlCutCopyPaste;

	private JButton cmdSave, cmdExit;
	private JLabel lblStatus, lblDateTime;
	private JProgressBar progressBar;
	private JMenuBar menuBar;

	// GlassPane
	private JPanel panGlass;
	private JLabel lblGlassPane;
	private JProgressBar glassPaneProgressBar;
	private JTabbedPane tabbedPane;

	private AppServerJfc jfcAppServer;
	private AppUserJfc jfcAppUser;
	private AppUserLoginJfc jfcAppUserLogin;
	private AppUserErrorJfc jfcAppUserError;

	public ServerFrame(String title) {
		super(title);

		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		this.setIconImage(Resource.getJarIcon(Resource.getValue(Resource.IMG_AppServerIcon)).getImage());

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onExit();
			}
		});

		this.setJMenuBar(getMyMenuBar());
		JfcDelegate.setMainWindow(this);

		setGlassPane(getMyGlassPane());

		this.getContentPane().setLayout(new BorderLayout());

		this.getContentPane().add(getToolBar(), BorderLayout.NORTH);
		this.getContentPane().add(getStatusPanel(), BorderLayout.SOUTH);

		JTabbedPane tab = getTabbedPane();

		tab.addTab("Home", Resource.getJarIcon("home.gif"), getHomePanel(), "");
		tab.addTab("App Server", Resource.getJarIcon("appServer.gif"), getAppServerJfc().getCardPanel(), "Information about server");
		tab.addTab("Users", Resource.getJarIcon("appUsers.gif"), getAppUserJfc().getCardPanel(), "Application Users");
		tab.addTab("Logins", Resource.getJarIcon("appUserLogins.gif"), getAppUserLoginJfc().getCardPanel(), "Users that are logged in");
		tab.addTab(	"Errors", Resource.getJarIcon("appUserErrors.gif"), getAppUserErrorJfc().getCardPanel(),
					"Exceptions from server and clients");

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPane.setFocusable(true);

		Border b; // = new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new EmptyBorder(5,3,3,5));
		// b = new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new LineBorder(Color.gray, 4));
		b = new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new MatteBorder(new Insets(5, 3, 5, 3), Color.gray));
		tab.setBorder(b);
		tab.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		this.getContentPane().add(tab, BorderLayout.CENTER);

		JfcDelegate.setMainWindow(this);

		OAJfcUtil.pack(this);
	}

	public UndoableController getUndoableController() {
		if (controlUndoable == null) {
			controlUndoable = new UndoableController();
		}
		return controlUndoable;
	}

	public CutCopyPasteController getCutCopyPasteController() {
		if (controlCutCopyPaste == null) {
			controlCutCopyPaste = new CutCopyPasteController();
		}
		return controlCutCopyPaste;
	}

	public AppServerJfc getAppServerJfc() {
		if (jfcAppServer != null) {
			return jfcAppServer;
		}
		final Hub<AppServer> hub = ModelDelegate.getCreateOneAppServerHub().createSharedHub();
		hub.setPos(0);
		hub.addHubListener(new HubListenerAdapter() {
			@Override
			public void afterAdd(HubEvent e) {
				hub.setPos(0);
			}

			@Override
			public void afterNewList(HubEvent e) {
				hub.setPos(0);
			}
		});
		AppServerModel model = new AppServerModel(hub);
		model.setAllowAdd(false);
		model.setAllowNew(false);
		model.setAllowRemove(false);
		model.setAllowDelete(false);
		model.setAllowGotoList(false);
		model.setPluralDisplayName("App Server");
		jfcAppServer = new AppServerJfc(model);
		jfcAppServer.getCardPanel();
		jfcAppServer.showCardPanel(AppServerJfc.CARD_Edit);
		return jfcAppServer;
	}

	public AppUserJfc getAppUserJfc() {
		if (jfcAppUser != null) {
			return jfcAppUser;
		}
		Hub<AppUser> hub = ModelDelegate.getAppUsers().createSharedHub();
		AppUserModel model = new AppUserModel(hub);
		model.setAllowAdd(false);
		model.setAllowNew(true);
		model.setAllowRemove(false);
		model.setAllowDelete(true);
		jfcAppUser = new AppUserJfc(model);
		return jfcAppUser;
	}

	public AppUserLoginJfc getAppUserLoginJfc() {
		if (jfcAppUserLogin != null) {
			return jfcAppUserLogin;
		}
		Hub<AppUserLogin> hub = ModelDelegate.getLastDayAppUserLogins().createSharedHub();
		AppUserLoginModel model = new AppUserLoginModel(hub);
		model.setAllowAdd(false);
		model.setAllowNew(false);
		model.setAllowRemove(false);
		model.setAllowDelete(false);
		jfcAppUserLogin = new AppUserLoginJfc(model);
		return jfcAppUserLogin;
	}

	public AppUserErrorJfc getAppUserErrorJfc() {
		if (jfcAppUserError != null) {
			return jfcAppUserError;
		}
		Hub<AppUserError> hub = ModelDelegate.getAppUserErrors().createSharedHub();
		AppUserErrorModel model = new AppUserErrorModel(hub);
		model.setAllowAdd(false);
		model.setAllowNew(false);
		model.setAllowRemove(false);
		model.setAllowDelete(false);
		jfcAppUserError = new AppUserErrorJfc(model);
		return jfcAppUserError;
	}

	public JTabbedPane getTabbedPane() {
		if (tabbedPane == null) {
			tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		}
		return tabbedPane;
	}

	public JToolBar getToolBar() {
		if (toolBar != null) {
			return toolBar;
		}
		toolBar = new JToolBar();
		// toolBar.setFloatable(false);

		toolBar.add(getSaveButton());
		toolBar.add(getExitButton());

		toolBar.addSeparator();
		// toolBar.add(getTestButton());
		// toolBar.add(getVerifyButton());

		toolBar.add(Box.createGlue());

		toolBar.addSeparator();
		toolBar.add(getHelpButton());
		toolBar.add(getCSHelpButton());

		return toolBar;
	}

	public JButton getSaveButton() {
		if (cmdSave == null) {
			cmdSave = new JButton("Save");
			cmdSave.setIcon(Resource.getJarIcon(Resource.IMG_Save));
			cmdSave.setActionCommand(CMD_Save);
			cmdSave.setToolTipText("Save all data to database");
			cmdSave.addActionListener(this);
			cmdSave.setMnemonic('S');
			OACommand.setup(cmdSave);
		}
		return cmdSave;
	}

	public JButton getExitButton() {
		if (cmdExit == null) {
			cmdExit = new JButton("Exit");
			cmdExit.setIcon(Resource.getJarIcon(Resource.IMG_Exit));
			cmdExit.setActionCommand(CMD_Exit);
			cmdExit.setToolTipText("Save all data, stop server and exit.");
			cmdExit.addActionListener(this);
			cmdExit.setMnemonic('x');
			OACommand.setup(cmdExit);
		}
		return cmdExit;
	}

	JPanel getHomePanel() {
		JPanel pan = new JPanel(new BorderLayout());

		pan.add(getVersionLabel(), BorderLayout.NORTH);

		ImageIcon icon = Resource.getJarIcon(Resource.getValue(Resource.IMG_Splash));
		JLabel lbl = new JLabel(icon);
		JScrollPane sp = new JScrollPane(lbl);
		pan.add(sp, BorderLayout.CENTER);

		return pan;
	}

	public JLabel getVersionLabel() {
		if (lblVersion == null) {
			lblVersion = new JLabel("");
		}
		return lblVersion;
	}

	public JLabel getLastTimeLabel() {
		if (lblLastTime == null) {
			lblLastTime = new JLabel("");
		}
		return lblLastTime;
	}

	public JLabel getLastTimeLabel2() {
		if (lblLastTime2 == null) {
			lblLastTime2 = new JLabel("");
		}
		return lblLastTime2;
	}

	protected JPanel getStatusPanel() {
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(0, 0, 0, 0);

		JLabel lbl = getDateTimeLabel();
		lbl.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new EmptyBorder(0, 8, 0, 20)));
		p.add(lbl, gc);

		lbl = getStatusLabel();
		lbl.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new EmptyBorder(0, 6, 0, 8)));
		gc.fill = gc.HORIZONTAL;
		gc.weightx = 1.0;
		p.add(lbl, gc);
		gc.fill = gc.NONE;
		gc.weightx = 0;

		gc.fill = gc.BOTH;
		p.add(getProgressBar(), gc);

		return p;
	}

	public JLabel getStatusLabel() {
		if (lblStatus == null) {
			lblStatus = new JLabel(" ") {
				public void setText(String s) {
					if (s == null) {
						s = "";
					}
					for (int i = s.length(); i < 5; i++) {
						s += " ";
					}
					super.setText(s);
				}
			};
		}
		return lblStatus;
	}

	public JLabel getDateTimeLabel() {
		if (lblDateTime == null) {
			lblDateTime = new JLabel((new OADateTime()).toString());
			lblDateTime.setOpaque(true);
		}
		return lblDateTime;
	}

	public JProgressBar getProgressBar() {
		if (progressBar == null) {
			progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 20) {
				@Override
				public Dimension getMinimumSize() {
					Dimension d = super.getPreferredSize();
					d.width = (int) (d.width * .50);
					return d;
				}
			};
		}
		return progressBar;
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals(CMD_Save)) {
			onSave();
		} else if (cmd.equals(CMD_Display)) {
			onDisplay();
		} else if (cmd.equals(CMD_Exit)) {
			onExit();
		} else if (cmd.equals(CMD_About)) {
			onAbout();
		}
	}

	public PopupMenu getTrayMenuBar() {
		PopupMenu pm = new PopupMenu("");
		MenuItem mi;

		mi = new MenuItem("Display " + Resource.getValue(Resource.APP_ApplicationName));
		mi.setActionCommand(CMD_Display);
		mi.addActionListener(this);
		pm.add(mi);

		mi = new MenuItem("Save");
		mi.setActionCommand(CMD_Save);
		mi.addActionListener(this);
		pm.add(mi);

		pm.addSeparator();

		// "File>Exit"
		mi = new MenuItem("Exit " + Resource.getValue(Resource.APP_ApplicationName));
		mi.setActionCommand(CMD_Exit);
		mi.addActionListener(this);
		pm.add(mi);

		pm.addSeparator();

		pm.add(getTrayHelpMenuItem());

		mi = new MenuItem("About ...");
		mi.setActionCommand(CMD_About);
		mi.addActionListener(this);
		pm.add(mi);

		return pm;
	}

	protected JMenuBar getMyMenuBar() {
		if (menuBar != null) {
			return menuBar;
		}
		menuBar = new JMenuBar();
		JMenu menu;
		JMenuItem mi;

		// MENU "File"
		menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);

		// "File>Save"
		mi = new JMenuItem("Save");
		mi.setActionCommand(CMD_Save);
		mi.addActionListener(this);
		mi.setMnemonic(KeyEvent.VK_S);
		mi.setIcon(Resource.getJarIcon(Resource.IMG_Save));
		mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_S, java.awt.Event.CTRL_MASK));
		menu.add(mi);

		menu.addSeparator();

		// "File>Exit"
		mi = new JMenuItem("Exit");
		mi.setActionCommand(CMD_Exit);
		mi.addActionListener(this);
		mi.setIcon(Resource.getJarIcon(Resource.IMG_Exit));
		mi.setMnemonic(KeyEvent.VK_X);
		mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_F4, java.awt.Event.ALT_MASK));
		menu.add(mi);
		menuBar.add(menu);

		// MENU "Edit"
		menu = new JMenu("Edit");
		menu.setMnemonic(KeyEvent.VK_E);
		menu.add(getUndoableController().getUndoMenuItem());
		menu.add(getUndoableController().getRedoMenuItem());
		menu.addSeparator();
		menu.add(getCutCopyPasteController().getCutMenuItem());
		menu.add(getCutCopyPasteController().getCopyMenuItem());
		menu.add(getCutCopyPasteController().getPasteMenuItem());
		menuBar.add(menu);

		// MENU "view"
		/*
		menu = new JMenu("View");
		menu.setMnemonic(KeyEvent.VK_V);
		menuBar.add(menu);
		*/

		// MENU "Help"
		menu = new JMenu("Help");
		menu.setMnemonic(KeyEvent.VK_H);

		menu.add(getHelpMenuItem());
		menu.add(getCSHelpMenuItem());

		menu.addSeparator();

		//Help > "About"
		mi = new JMenuItem("About");
		mi.setActionCommand(CMD_About);
		mi.addActionListener(this);
		mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_A, java.awt.Event.CTRL_MASK));
		menu.add(mi);
		menuBar.add(menu);

		return menuBar;
	}

	private MenuItem miTrayHelp;

	public MenuItem getTrayHelpMenuItem() {
		if (miTrayHelp == null) {
			miTrayHelp = new MenuItem("Help ...");
		}
		return miTrayHelp;
	}

	private JMenuItem miHelp;

	public JMenuItem getHelpMenuItem() {
		if (miHelp == null) {
			miHelp = new JMenuItem("Help ...");
			miHelp.setMnemonic('H');
			// miHelp.setActionCommand(CMD_Help);
			// miHelp.setToolTipText("");
			// miHelp.addActionListener(this);
			miHelp.setIcon(Resource.getJarIcon(Resource.IMG_Help));
			miHelp.setAccelerator(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		}
		return miHelp;
	}

	protected JButton cmdHelp;

	public JButton getHelpButton() {
		if (cmdHelp == null) {
			cmdHelp = new JButton(Resource.getJarIcon(Resource.IMG_Help));
			OACommand.setupButton(cmdHelp);
			cmdHelp.setToolTipText("Display Help window");
		}
		return cmdHelp;
	}

	protected JButton cmdCSHelp;

	public JButton getCSHelpButton() {
		if (cmdCSHelp == null) {
			cmdCSHelp = new JButton(Resource.getJarIcon(Resource.IMG_CSHelp));
			OACommand.setupButton(cmdCSHelp);
			cmdCSHelp.setToolTipText("Display Context Sensitive Help");
		}
		return cmdCSHelp;
	}

	private JMenuItem miCSHelp;

	public JMenuItem getCSHelpMenuItem() {
		if (miCSHelp == null) {
			miCSHelp = new JMenuItem("Context Help ...");
			miCSHelp.setMnemonic('C');
			// miCSHelp.setActionCommand(CMD_CSHelp);
			// miCSHelp.setToolTipText("");
			miCSHelp.addActionListener(this);
			miCSHelp.setIcon(Resource.getJarIcon(Resource.IMG_CSHelp));
		}
		return miCSHelp;
	}

	public JProgressBar getGlassPaneProgressBar() {
		if (glassPaneProgressBar == null) {
			glassPaneProgressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
		}
		return glassPaneProgressBar;
	}

	public JLabel getGlassPaneLabel() {
		if (lblGlassPane == null) {
			lblGlassPane = new JLabel("Loading Data ...") {
				@Override
				public void setText(String text) {
					if (text == null) {
						text = "";
					}
					text = "   " + text + "   ";
					super.setText(text);
				}
			};
			lblGlassPane.setBackground(Color.white);
			lblGlassPane.setOpaque(true);

			// lbl.setForeground(Color.blue);
		}
		return lblGlassPane;
	}

	// GlassPane that blocks all mouse events, except moving the window.
	class MyGlassPanel extends JPanel {
		public MyGlassPanel() {
			enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
		}

		@Override
		protected void processMouseEvent(MouseEvent e) {
			Point pt = e.getPoint();
			if (pt.y < 30) {
				e.setSource(ServerFrame.this);
				ServerFrame.this.processMouseEvent(e);
			} else {
				e.consume();
			}
		}

		@Override
		protected void processMouseMotionEvent(MouseEvent e) {
			Point pt = e.getPoint();
			if (pt.y < 30) {
				e.setSource(ServerFrame.this);
				ServerFrame.this.processMouseMotionEvent(e);
			} else {
				e.consume();
			}
		}
	}

	/**
	 * GlassPane panel with label and progressBar on it.
	 */
	public JPanel getMyGlassPane() {
		if (panGlass == null) {
			panGlass = new MyGlassPanel();
			panGlass.setLayout(new GridBagLayout());
			panGlass.setOpaque(false);

			GridBagConstraints gc = new GridBagConstraints();
			gc.insets = new Insets(3, 3, 3, 3);
			gc.gridwidth = gc.REMAINDER;

			JPanel p = new JPanel(new GridBagLayout());

			Color c = new Color(255, 255, 255, 120);
			p.setBackground(c);

			Border b = new EmptyBorder(2, 2, 2, 2);

			Color color = UIManager.getColor("InternalFrame.activeTitleBackground");
			b = new CompoundBorder(b, new LineBorder(color, 5));
			b = new CompoundBorder(b, new EmptyBorder(25, 25, 25, 25));
			p.setBorder(b);

			p.add(getGlassPaneLabel(), gc);
			p.add(getGlassPaneProgressBar(), gc);

			panGlass.add(p, gc);

			panGlass.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
		return panGlass;
	}

	public abstract void onDisplay();

	public abstract void onSave();

	public abstract void onAbout();

	public abstract void onExit();

	public static void main(String[] args) {
		ServerFrame frm = new ServerFrame("Test ServerFrame") {
			@Override
			public void onAbout() {
			}

			@Override
			public void onExit() {
				System.exit(0);
			}

			@Override
			public void onSave() {
			}

			@Override
			public void onDisplay() {
			}
		};
		String cmd = "esc";
		frm.getTabbedPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), cmd);
		frm.getTabbedPane().getActionMap().put(cmd, new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		frm.setVisible(true);
	}

}
