// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.control.client;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.File;
import java.net.URL;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import com.viaoa.appstore.control.AboutController;
import com.viaoa.appstore.control.HelpController;
import com.viaoa.appstore.control.LogController;
import com.viaoa.appstore.delegate.JfcDelegate;
import com.viaoa.appstore.delegate.ModelDelegate;
import com.viaoa.appstore.model.oa.AppUser;
import com.viaoa.appstore.model.oa.AppUserLogin;
import com.viaoa.appstore.resource.Resource;
import com.viaoa.appstore.view.client.ClientFrame;
/*$$Start: ClientFrameController.customize0 $$*/
import com.viaoa.appstore.view.client.panel.myApps.MyAppsPanel;
import com.viaoa.appstore.view.client.panel.envs.EnvsPanel;
import com.viaoa.appstore.view.client.panel.appTypes.AppTypesPanel;
import com.viaoa.appstore.view.client.panel.admin.AdminPanel;
/*$$End: ClientFrameController.customize0 $$*/
import com.viaoa.jfc.OAJfcUtil;
import com.viaoa.jfc.control.OAJfcController;
import com.viaoa.jfc.print.PrintController;
import com.viaoa.jfc.report.OAHTMLReport;
import com.viaoa.util.OAConv;
import com.viaoa.util.OADateTime;

/**
 * Controls FrameJfc
 */
public abstract class ClientFrameController {
	private static Logger LOG = Logger.getLogger(ClientController.class.getName());

	public static final String Frame_X = "frame.x";
	public static final String Frame_Y = "frame.y";
	public static final String Frame_Width = "frame.width";
	public static final String Frame_Height = "frame.height";
	public static final String Frame_Extended = "frame.extended";

	private volatile ClientFrame frm;

	private HelpController controlHelp;
	private AboutController controlAbout;
	private PdfController controlPdf;
	private PrintController controlPrint;
	private ReportController controlReport;
	private FileDialogController controlFileDialog;

	public ClientFrameController(HelpController controlHelp) {
		LOG.fine("called");
		this.controlHelp = controlHelp;
	}

	public boolean isFrameCreated() {
		return (frm != null);
	}

	public boolean isFrameCompleted() {
		return abFrameCreated.get();
	}

	/***
	 * protected void onShowServerInfoMessage() { ServerInfo si = ModelDelegate.getServerInfo(); if (si != null &&
	 * si.getSendMessageDateTime() != null) { String msg = si.getSendMessage(); if (!OAString.isEmpty(msg)) { OADateTime dt =
	 * si.getSendMessageDateTime(); msg = "<b style='color:gray'>" + dt.toString() + "</br>
	 * <br>
	 * " + msg; getFrame().setNotifyBalloon("<html>" + msg); } } } protected void onShowServerStatus() { ServerInfo si =
	 * ModelDelegate.getServerInfo(); if (si != null) { String msg = si.getSendMessage(); if (OAString.isEmpty(msg)) msg = ""; else {
	 * OADateTime dt = si.getSendMessageDateTime(); if (dt != null) { msg = "<b style='color:gray'>" + dt.toString() + "</br>
	 * <br>
	 * " + msg; } } msg = "<b><u>Server is running</u></b><br>
	 * " + msg; getFrame().setNotifyBalloon("<html>" + msg); } }
	 */

	private final Object lockFrame = new Object();
	private final AtomicBoolean abFrameCreated = new AtomicBoolean(false);

	public ClientFrame getFrame() {
		if (frm != null) {
			return frm;
		}

		LOG.fine("creating frame");
		synchronized (lockFrame) {
			if (frm != null) {
				return frm;
			}
			frm = new ClientFrame(Resource.getRunTimeName(), getPrintController()) {
				protected @Override void onExit() {
					int i;
					if (Resource.getBoolean(Resource.INI_AutoLogout)) {
						i = JOptionPane.YES_OPTION;
					} else {
						i = JOptionPane.showConfirmDialog(	getFrame(), "Ok to close " + Resource.getRunTimeName(),
															"Exit " + Resource.getValue(Resource.APP_ApplicationName),
															JOptionPane.YES_NO_OPTION,
															JOptionPane.QUESTION_MESSAGE);
					}
					if (i == JOptionPane.YES_OPTION) {
						ClientFrameController.this.onExit();
					}
				}

				@Override
				protected void onSave() {
					ClientFrameController.this.onSave();
				}

				@Override
				protected void onAbout() {
					ClientFrameController.this.getAboutController().setVisible(true);
				}

				@Override
				protected void onNotifyLabelMouseClick() {
					// ClientFrameController.this.onShowServerStatus();
				}
			};
			JfcDelegate.setMainWindow(frm);
		}
		setProcessing(true, "Creating User Interface ...");
		JfcDelegate.setFrameController(this);

		getPrintController().setParentWindow(frm);

		OAJfcUtil.setStatusBarLabel(frm.getStatusLabel());

		notifyStatusThread();

		try {
			// set frame position and size
			int x = OAConv.toInt(Resource.getValue(Frame_X, "0"));
			int y = OAConv.toInt(Resource.getValue(Frame_Y, "0"));
			int w = OAConv.toInt(Resource.getValue(Frame_Width, "0"));
			int h = OAConv.toInt(Resource.getValue(Frame_Height, "0"));
			int ext = OAConv.toInt(Resource.getValue(Frame_Extended, JFrame.NORMAL + ""));

			final Dimension dimScreen = Toolkit.getDefaultToolkit().getScreenSize();

			Rectangle bounds;
			if (x < 0) {
				//was x = 0;
			}
			if (y < 0) {
				//was y = 0;
			}
            if (w > 100 && h > 100) {
			//was: if (x >= 0 && y >= 0 && w > 100 && h > 100) {
				bounds = new Rectangle(x, y, w, h);
			} else {
				OAJfcUtil.pack(frm);
				bounds = frm.getBounds();
				if (bounds.width > dimScreen.width) {
					bounds.width = 0;
				}
				if (bounds.height > dimScreen.height) {
					bounds.height = 0;
				}
				bounds.width = Math.max((int) (dimScreen.width * .8), bounds.width);
				bounds.height = Math.max((int) (dimScreen.height * .8), bounds.height);

				// center
				int x1 = (dimScreen.width - bounds.width) / 2;
				int y1 = (dimScreen.height - bounds.height) / 2;
				bounds.x = x1;
				bounds.y = y1;

				if (ext != JFrame.MAXIMIZED_BOTH) {
					ext = JFrame.NORMAL;
				}
			}

			int maxW = 0;
			int maxH = 0;
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			for (GraphicsDevice gd : ge.getScreenDevices()) {
				for (GraphicsConfiguration gc : gd.getConfigurations()) {
					Rectangle rec = gc.getBounds();
					maxW += rec.width;
					maxH = Math.max(rec.height, maxH);
				}
			}
			if (bounds.x + bounds.width > maxW || bounds.y + bounds.height > maxH) {
				bounds = new Rectangle(25, 25, dimScreen.width - 50, (dimScreen.height - 50));
				if (ext != JFrame.MAXIMIZED_BOTH) {
					ext = JFrame.NORMAL;
				}
			}

			frm.setBounds(bounds);
			frm.setExtendedState(ext);
		} catch (Exception e) {
			LOG.log(Level.WARNING, "error while setting frame.bounds", e);
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

        getAboutController();
		
		
		// Look & Feel
		JMenu menu = frm.getMenuLF();
		ButtonGroup group = new ButtonGroup();
		UIManager.LookAndFeelInfo[] lf = UIManager.getInstalledLookAndFeels();
		String laf = UIManager.getLookAndFeel().getName();
		for (int i = 0; i < lf.length; i++) {
			String s = lf[i].getName();
			JRadioButtonMenuItem rmi = new JRadioButtonMenuItem(s + " Look and Feel");
			rmi.setSelected(s.equals(laf));
			rmi.setActionCommand(lf[i].getClassName());
			rmi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ClientFrameController.this.setLookAndFeel(e.getActionCommand());
				}
			});
			group.add(rmi);
			menu.add(rmi);
		}

		frm.getRootPane().registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean b = !frm.getDocMode();
				frm.setDocMode(b);
				if (b) {
					frm.setGlassPane(frm.getDocGlassPanel());
					frm.getGlassPane().setVisible(true);
				} else {
					frm.getGlassPane().setVisible(false);
					frm.setGlassPane(frm.getMyGlassPane());
				}
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_F11, KeyEvent.CTRL_DOWN_MASK, false), JComponent.WHEN_IN_FOCUSED_WINDOW);

		frm.getRootPane().registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OAJfcController.DEBUGUI = !OAJfcController.DEBUGUI;
				System.out.println("[ctrl+F12] OAJfcController.DEBUGUI=" + OAJfcController.DEBUGUI);
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_F12, KeyEvent.CTRL_DOWN_MASK, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
		OAJfcController.DEBUGUI = Resource.getBoolean(Resource.INI_UIDebug, false);

		// this will add panels, etc based on oaModel
		customize();

		String s = Resource.getValue(Resource.APP_MainCardPanel, ClientFrame.CARD_Splash);
		frm.showCardPanel(s);

		setProcessing(false);
		abFrameCreated.set(true);

		getFileDialogController().init();
		LOG.fine("frame created");
		return frm;
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
		} else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						_setProcessing(b, msg);
					}
				});
			} catch (Exception e) {
			}
		}
	}

	private Stack<String> stackProcessingMessage = new Stack<String>();

	public void _setProcessing(boolean b, String msg) {
		boolean bEmpty;
		if (b) {
			if (msg == null) {
				msg = "";
			}
			synchronized (stackProcessingMessage) {
				stackProcessingMessage.add(msg);
			}
			bEmpty = false;
		} else {
			synchronized (stackProcessingMessage) {
				if (stackProcessingMessage.isEmpty()) {
					return;
				}
				msg = stackProcessingMessage.pop();

				bEmpty = stackProcessingMessage.isEmpty();
				if (!bEmpty) {
					msg = stackProcessingMessage.peek(); // get msg to display
				} else {
					msg = null;
				}
			}
		}

		if (frm == null) {
			return;
		}

		if (!bEmpty) {
			frm.getGlassPaneLabel().setText(msg);
			frm.getGlassPane().setVisible(true);
			frm.getGlassPaneProgressBar().setIndeterminate(true);
		} else {
			frm.getGlassPaneProgressBar().setIndeterminate(false);
			frm.getGlassPane().setVisible(false);
		}
		frm.getProgressBar().setStringPainted(msg != null && msg.length() > 0);
		frm.getProgressBar().setString(msg);
		frm.getProgressBar().setIndeterminate(!bEmpty);
	}

	public void setStatus(String msg) {
		lastStatusUpdate = System.currentTimeMillis();
		if (frm != null) {
			frm.getStatusLabel().setText(msg);
		}
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
			String[] msgs = new String[] { "Love", "Joy", "Peace", "Hope", "Goodness", "Success" };

			public void run() {
				int max = getFrame().getProgressBar().getMaximum();
				for (int i = 0;; i++) {
					getFrame().getDateTimeLabel().setText(msgs[i % msgs.length]);
					getFrame().getDateTimeLabel().setText((new OADateTime()).toString());

					if (stackProcessingMessage.isEmpty() && lastStatusUpdate != 0) {
						if (System.currentTimeMillis() > (lastStatusUpdate + (30 * 1000))) {
							getFrame().getStatusLabel().setText((i % 2 == 0) ? "Have Fun" : "Smile :)");
							getFrame().getStatusLabel().setText("");
							lastStatusUpdate = 0;
						}
						if (getFrame().getCursor() != Cursor.getDefaultCursor()) {
							//                            getFrame().setCursor(Cursor.getDefaultCursor());
							LOG.fine("setting cursor to default, processingCounter=" + stackProcessingMessage.size());
						}
					}

					try {
						synchronized (LOCKStatus) {
							LOCKStatus.wait((31 * 1000));
						}
					} catch (Exception e) {
						System.out.println("ClientFrameController.Error: " + e);
					}
				}
			}
		};
		threadStatus.setDaemon(true);
		threadStatus.setPriority(Thread.MIN_PRIORITY);
		threadStatus.start();
	}

	public AboutController getAboutController() {
		if (controlAbout == null) {
			controlAbout = new AboutController(this.getFrame());
		}
		return controlAbout;
	}

	public void close() {
		if (frm == null) {
			return;
		}
		Point p = frm.getLocation();
		int type = Resource.getRunType();
		if (type == Resource.RUNTYPE_Single) {
			type = Resource.TYPE_Single;
		} else {
			type = Resource.TYPE_Client;
		}

		Resource.setValue(type, Frame_X, p.x + "");
		Resource.setValue(type, Frame_Y, p.y + "");
		Dimension d = frm.getSize();
		Resource.setValue(type, Frame_Width, d.width + "");
		Resource.setValue(type, Frame_Height, d.height + "");
		Resource.setValue(type, Frame_Extended, frm.getExtendedState() + "");
		Resource.save();
		frm.setVisible(false);
	}

	public PdfController getPdfController() {
		if (controlPdf == null) {
			controlPdf = new PdfController();
		}
		return controlPdf;
	}

	public void onSaveAsPdf() {
		LOG.fine("called");
		if (getPdfController() == null || controlPrint == null) {
			return;
		}

		AppUserLogin userLogin = ModelDelegate.getLocalAppUserLogin();
		AppUser user = userLogin.getAppUser();

		final String userName = user == null ? "" : user.getFullName();

		JFileChooser fc = getFileDialogController().getPdfFileChooser();
		int x = fc.showSaveDialog(this.frm);
		if (x != JFileChooser.APPROVE_OPTION) {
			return;
		}
		final File file = fc.getSelectedFile();
		final String fileName = file.getPath();

		try {
			file.createNewFile();
		} catch (Exception e) {
			String s = "";
			for (int i = 0; i < fileName.length(); i++) {
				char ch = fileName.charAt(i);
				if (Character.isDigit(ch) || Character.isLetter(ch)) {
					continue;
				}
				if ("\\ _-.".indexOf(ch) >= 0) {
					continue;
				}
				if (ch == ':' && i == 1) {
					continue;
				}
				if (s.length() == 0) {
					s = "\nThe following could be the bad characters: ";
				}
				s += ch + " ";
			}
			JOptionPane.showMessageDialog(frm, "Invalid/bad file name: " + fileName
					+ "\nPlease remove any characters that are not valid,\nand try again." + s, "Save as Pdf", JOptionPane.ERROR_MESSAGE);
			return;
		}

		SwingWorker<Boolean, String> sw = new SwingWorker<Boolean, String>() {
			String error;

			@Override
			protected Boolean doInBackground() throws Exception {
				try {
					controlPdf.saveToPdf(controlPrint.getPrintable(), controlPrint.getPageFormat(), fileName, "oaAppStore Report", userName);
				} catch (Throwable e) {
					error = e.getMessage();
					LOG.log(LogController.Level_ERROR, "Exception calling saveToPdf", e);
				}
				return true;
			}

			@Override
			protected void done() {
				setStatus("");
				setProcessing(false);
				if (error != null) {
					JOptionPane.showMessageDialog(	frm, "Could not save file\n" + error + "\nPlease verify that a valid file name is used.",
													"Save as Pdf", JOptionPane.ERROR_MESSAGE);
				} else {
					try {
						URL url = getClass().getResource(Resource.getJarImageDirectory() + "/" + Resource.getValue(Resource.IMG_PdfBig));
						ImageIcon icon = new ImageIcon(url);
						int x = JOptionPane.showConfirmDialog(	frm, "Report saved as Pdf file \"" + file.getName()
								+ "\"\nWould you like to view the Pdf document?", Resource.getValue(Resource.APP_ClientApplicationName),
																JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, icon);
						if (x == JOptionPane.YES_OPTION) {
							Desktop.getDesktop().open(file);
						}
					} catch (Exception e) {
						LOG.log(LogController.Level_ERROR, "Error saving Order as Pdf file, user can try again", e);
					}
				}
			}
		};
		setStatus("Saving report as Pdf file \"" + file.getName() + "\" ...");
		setProcessing(true, "Saving ...");
		sw.execute();
	}

	// refreshes pet reports
	protected void refreshReport(final OAHTMLReport rpt, final boolean bShowStatus) {
		if (bShowStatus) {
			setProcessing(true, "Preparing report ...");
		}
		SwingWorker<String, Integer> sw = new SwingWorker<String, Integer>() {
			@Override
			protected String doInBackground() throws Exception {
				try {
					rpt.refreshDetail();
				} catch (Exception e) {
					LOG.log(Level.WARNING, "Error while building report", e);
				}
				return null;
			}

			@Override
			protected void done() {
				if (bShowStatus) {
					setProcessing(false);
				}
				ClientFrameController.this.setPrintable(rpt);
			}
		};
		sw.execute();
	}

	public PrintController getPrintController() {
		if (controlPrint == null) {
			controlPrint = new PrintController() {
				@Override
				public void onSaveAsPdf() {
					ClientFrameController.this.onSaveAsPdf();
				}

				@Override
				public void setPageFormat(PageFormat pf) {
					// need to know the report name
					if (pf != ClientFrameController.this.getReportController().getPageFormat("")) {
						ClientFrameController.this.getReportController().setPageFormat("", pf);
						ClientFrameController.this.getReportController().savePageFormat("", pf);
					}
					super.setPageFormat(pf);
				}
			};
			if (frm != null) {
				controlPrint.setParentWindow(frm);
			}
			controlPrint.setPrintable(null);
			JfcDelegate.setPrintController(controlPrint);
		}
		return controlPrint;
	}

	public ReportController getReportController() {
		if (controlReport != null) {
			return controlReport;
		}

		controlReport = new ReportController();

		// todo: need to do this for all reports
		// validate the pageFormat that is used by the report controller
		//PageFormat pageFormat = controlReport.getPageFormat();
		//pageFormat = getPrintController().validate(pageFormat);  //qqq getPrintController will be null if frame is not ready
		//controlReport.setPageFormat(pageFormat);

		return controlReport;
	}

	public FileDialogController getFileDialogController() {
		if (controlFileDialog == null) {
			controlFileDialog = new FileDialogController();
		}
		return controlFileDialog;
	}

	protected void setPrintable(Printable p) {
		getPrintController().setPrintable(p);
	}

	public void updateUI() {
		if (frm != null) {
			frm.updateUI();
		}
		if (controlAbout != null) {
			controlAbout.updateUI();
		}
		if (controlPrint != null) {
			controlPrint.updateUI();
		}
	}

	// called by ClientController
	protected void afterModelLoaded() {

	}

	protected void customize() {
		/*$$Start: ClientFrameController.customize1 $$*/
        MyAppsPanel panMyApps = new MyAppsPanel();
        frm.addPanel(panMyApps.getMainComponent(), "MyAppsPanel", panMyApps.getMenuItem(), panMyApps.getToggleButton());
        EnvsPanel panEnvs = new EnvsPanel();
        frm.addPanel(panEnvs.getMainComponent(), "EnvsPanel", panEnvs.getMenuItem(), panEnvs.getToggleButton());
        AppTypesPanel panAppTypes = new AppTypesPanel();
        frm.addPanel(panAppTypes.getMainComponent(), "AppTypesPanel", panAppTypes.getMenuItem(), panAppTypes.getToggleButton());
        AdminPanel panAdmin = new AdminPanel();
        frm.addPanel(panAdmin.getMainComponent(), "AdminPanel", panAdmin.getMenuItem(), panAdmin.getToggleButton());
/*$$End: ClientFrameController.customize1 $$*/
	}

	/**
	 * Called when user closes the window.
	 *
	 * @see #close to properly close the window.
	 */
	protected abstract void onExit();

	protected abstract void onSave();

	protected abstract void setLookAndFeel(String laf);
}
