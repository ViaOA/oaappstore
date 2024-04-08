// Copied from OATemplate project by OABuilder 03/28/24 02:38 PM
package com.viaoa.appstore.view.client;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.viaoa.appstore.resource.Resource;
import com.viaoa.jfc.OAButton;
import com.viaoa.jfc.OACommand;
import com.viaoa.jfc.control.CutCopyPasteController;
import com.viaoa.jfc.control.UndoableController;
import com.viaoa.jfc.print.PrintController;
//import net.java.balloontip.BalloonTip;
//import net.java.balloontip.BalloonTip;
import com.viaoa.util.OADateTime;

public abstract class ClientFrame extends JFrame {
	private static Logger LOG = Logger.getLogger(ClientFrame.class.getName());

	private UndoableController controlUndoable;
	private CutCopyPasteController controlCutCopyPaste;
	private PrintController controlPrint;

	// cardPanel
	private JPanel cardPanel;
	private CardLayout cardLayout;
	// toolbar toogle buttons
	private HashMap<String, JToggleButton> hmButtonGroup;
	private volatile ButtonGroup buttonGroup;

	// menu check box items
	private HashMap<String, JRadioButtonMenuItem> hmRadioButtonMenuItemGroup;
	private ButtonGroup radioButtonMenuItemGroup;

	public static final String CARD_Splash = "splash";

	// frame components
	private JLabel lblStatus, lblDateTime, lblNotify;
	private JProgressBar progressBar;
	// private BalloonTip balloonTipNotify;

	// GlassPane
	private JPanel panGlass;
	private JLabel lblGlassPane;
	private JProgressBar glassPaneProgressBar;
	private boolean bDocMode;
	private DocGlassPanel docGlassPanel;

	private JToolBar toolBar;

	private JMenuBar menuBar;
	private JMenu menuFile;
	private JMenu menuEdit;
	private JMenu menuView;
	private JMenu menuLF;
	private JMenuItem miAbout;
	private JMenuItem miSave;

	public ClientFrame(String title, PrintController controlPrint) {
		super(title);

		this.controlPrint = controlPrint;

		this.setIconImage(Resource.getJarIcon(Resource.getValue(Resource.IMG_AppClientIcon)).getImage());

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(25, 25, dim.width - 50, dim.height - 50);

		/* or
		pack();
		Dimension d = getSize();
		d.width += 25;
		d.height += 25;
		setSize(d);
		*/

		setGlassPane(getMyGlassPane());

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onExit();
			}
		});

		setup();
	}

	protected void setup() {
		Container cont = getContentPane();
		cont.setLayout(new BorderLayout());

		cont.add(getToolBar(), BorderLayout.NORTH);
		cont.add(getStatusPanel(), BorderLayout.SOUTH);

		setJMenuBar(getMyMenuBar());

		cont.add(getCardPanel(), BorderLayout.CENTER);

		// notify components on statusBar
		//getNotifyBalloonTip().setVisible(false);
		setNotifyLabel("Server");
		getNotifyLabel().setToolTipText("click here to get the Server status");
		getNotifyLabel().setForeground(Color.lightGray);
		getNotifyLabel().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// ClientFrame.this.onNotifyLabelMouseClick();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				ClientFrame.this.onNotifyLabelMouseClick();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				ClientFrame.this.getNotifyLabel().setForeground(Color.BLACK);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				ClientFrame.this.getNotifyLabel().setForeground(Color.lightGray);
			}
		});
	}

	public void setNotifyLabel(String title) {
		title = (title == null || title.length() == 0) ? "Status" : title;
		getNotifyLabel().setText(title);
	}

	/*
	public void setNotifyBalloon(String msg) {
	    if (msg != null) getNotifyBalloonTip().setTextContents(msg);
	    getNotifyBalloonTip().setVisible(!OAString.isEmpty(msg));
	}
	*/

	// ClientFrame methods ==================================
	public void updateUI() {
		SwingUtilities.updateComponentTreeUI(this);
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

	public JLabel getDateTimeLabel() {
		if (lblDateTime == null) {
			lblDateTime = new JLabel((new OADateTime()).toString() + "  ");
			lblDateTime.setOpaque(true);
		}
		return lblDateTime;
	}

	public JLabel getStatusLabel() {
		if (lblStatus == null) {
			lblStatus = new JLabel(" ") {
				public void setText(String s) {
					if (s == null) {
						s = "";
					}
					for (int i = s.length(); i < 20; i++) {
						s += " ";
					}
					super.setText(s);
				}
			};
		}
		return lblStatus;
	}

	public JLabel getNotifyLabel() {
		if (lblNotify == null) {
			lblNotify = new JLabel(" ") {
				public void setText(String s) {
					if (s == null) {
						s = "";
					}
					for (int i = s.length(); i < 8; i++) {
						s += " ";
					}
					super.setText(s);
				}
			};
			//lblNotify.setToolTipText("Click here to get the status for the Term interface.");
			lblNotify.setOpaque(true);
		}
		return lblNotify;
	}

	/*
	public BalloonTip getNotifyBalloonTip() {
	    if (balloonTipNotify == null) {
	        balloonTipNotify = new BalloonTip(getNotifyLabel(), "Notification messages will be here ...") {
	            public void closeBalloon() {
	                setVisible(false);
	                // otherwise, the default will remove
	            }
	        };
	        // balloonTipNotify.enableClickToHide(true);
	    }
	    return balloonTipNotify;
	}
	*/

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

		lbl = getNotifyLabel();
		lbl.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new EmptyBorder(0, 6, 0, 8)));
		p.add(lbl, gc);

		gc.fill = gc.BOTH;

		getProgressBar();

		Border border = new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new EmptyBorder(1, 1, 1, 1));

		progressBar.setBorder(new CompoundBorder(border, progressBar.getBorder()));
		Font font = progressBar.getFont();
		progressBar.setFont(font.deriveFont(10.0f));
		p.add(progressBar, gc);

		return p;
	}

	public JProgressBar getGlassPaneProgressBar() {
		if (glassPaneProgressBar == null) {
			glassPaneProgressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
		}
		return glassPaneProgressBar;
	}

	public JLabel getGlassPaneLabel() {
		if (lblGlassPane == null) {
			lblGlassPane = new JLabel("Loading Data ...");
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
				e.setSource(ClientFrame.this);
				ClientFrame.this.processMouseEvent(e);
			} else {
				e.consume();
			}
		}

		@Override
		protected void processMouseMotionEvent(MouseEvent e) {
			Point pt = e.getPoint();
			if (pt.y < 30) {
				e.setSource(ClientFrame.this);
				ClientFrame.this.processMouseMotionEvent(e);
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
			//Color c = new Color(0, 0 , 255, 145);
			//panGlass.setBackground(c);
			GridBagConstraints gc = new GridBagConstraints();
			gc.insets = new Insets(3, 3, 3, 3);
			gc.gridwidth = gc.REMAINDER;

			JPanel p = new JPanel(new GridBagLayout());

			Border b = new EmptyBorder(3, 3, 3, 3);
			;
			b = new CompoundBorder(b, new LineBorder(Color.blue, 3));
			b = new CompoundBorder(b, new EmptyBorder(3, 3, 3, 3));
			p.setBorder(b);

			p.add(getGlassPaneLabel(), gc);
			p.add(getGlassPaneProgressBar(), gc);

			panGlass.add(p, gc);

			panGlass.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
		return panGlass;
	}

	public void setDocMode(boolean b) {
		this.bDocMode = b;
		if (!b) {
			getDocGlassPanel().clear();
		}

	}

	public boolean getDocMode() {
		return bDocMode;
	}

	public DocGlassPanel getDocGlassPanel() {
		if (docGlassPanel == null) {
			docGlassPanel = new DocGlassPanel();
		}
		return docGlassPanel;
	}

	public class DocGlassPanel extends JPanel {
		public DocGlassPanel() {
			enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
			setOpaque(false);
		}

		public void clear() {
			alLocation.clear();
		}

		@Override
		protected void processMouseEvent(MouseEvent e) {
			Point pt = e.getPoint();
			if (pt.y < 30) {
				e.setSource(ClientFrame.this);
				ClientFrame.this.processMouseEvent(e);
				return;
			}
			e.consume();
			if (!bDocMode) {
				return;
			}

			if (e.getID() != MouseEvent.MOUSE_PRESSED) {
				return;
			}

			Location loc = new Location(pt, (alLocation.size() + 1) + "");
			alLocation.add(loc);
			repaint();
		}

		public void paint(Graphics gr) {
			Graphics2D g = (Graphics2D) gr;
			super.paint(g);
			g.setColor(Color.black);

			g.setRenderingHint(	RenderingHints.KEY_ANTIALIASING,
								RenderingHints.VALUE_ANTIALIAS_ON);

			Stroke stroke = new BasicStroke(1f);
			g.setStroke(stroke);

			for (Location loc : alLocation) {
				Paint redtowhite = new GradientPaint(loc.pt.x, loc.pt.y, Color.YELLOW, loc.pt.x + 13, loc.pt.y + 13, Color.WHITE, true);
				g.setPaint(redtowhite);
				Ellipse2D e = new Ellipse2D.Double(loc.pt.x, loc.pt.y, 26, 26);
				g.fill(e);

				g.setPaint(Color.black);
				g.draw(e);

				Font font = new Font("TimesRoman", Font.BOLD, 12);
				g.setFont(font);
				g.drawString(loc.code, loc.pt.x + 8, loc.pt.y + 19);

			}
		}

		final ArrayList<Location> alLocation = new ArrayList<Location>();

		class Location {
			Point pt;
			String code;

			public Location(Point pt, String code) {
				this.pt = pt;
				this.code = code;
			}
		}

		@Override
		protected void processMouseMotionEvent(MouseEvent e) {
			Point pt = e.getPoint();
			if (pt.y < 30) {
				e.setSource(ClientFrame.this);
				ClientFrame.this.processMouseMotionEvent(e);
			} else {
				e.consume();
			}
		}
	}

	public JPanel getCardPanel() {
		if (cardPanel != null) {
			return cardPanel;
		}
		cardLayout = new CardLayout();
		cardPanel = new JPanel(cardLayout);

		Icon icon = Resource.getJarIcon(Resource.getValue(Resource.IMG_Splash));
		JLabel lbl = new JLabel(icon, JLabel.CENTER);
		cardPanel.add(lbl, CARD_Splash);
		showCardPanel(CARD_Splash);

		return cardPanel;
	}

	public void showCardPanel(String name) {
		if (cardPanel == null) {
			return;
		}
		cardLayout.show(cardPanel, name);

		// update toolbar and view menu
		if (buttonGroup != null && hmButtonGroup != null) {
			JToggleButton tb = hmButtonGroup.get(name);
			if (tb != null) {
				buttonGroup.setSelected(tb.getModel(), true);
			} else {
				buttonGroup.clearSelection();
			}
		}
		if (radioButtonMenuItemGroup != null && hmRadioButtonMenuItemGroup != null) {
			JRadioButtonMenuItem mi = hmRadioButtonMenuItemGroup.get(name);
			if (mi != null) {
				radioButtonMenuItemGroup.setSelected(mi.getModel(), true);
			} else {
				radioButtonMenuItemGroup.clearSelection();
			}
		}
	}

	public ButtonGroup getButtonGroup() {
		if (buttonGroup == null) {
			buttonGroup = new ButtonGroup();
			hmButtonGroup = new HashMap<String, JToggleButton>();
			radioButtonMenuItemGroup = new ButtonGroup();
			hmRadioButtonMenuItemGroup = new HashMap<String, JRadioButtonMenuItem>();
		}
		return buttonGroup;
	}

	/**
	 * Add a new card panel, and a toggle button on the toolbar
	 *
	 * @param cardComponent
	 * @param name
	 * @param mi
	 * @param button
	 */
	public void addPanel(JComponent cardComponent, final String name, JRadioButtonMenuItem mi, JToggleButton button) {
		if (cardComponent != null) {
			getCardPanel().add(cardComponent, name);
		}

		getButtonGroup();

		if (mi != null) {
			radioButtonMenuItemGroup.add(mi);
			hmRadioButtonMenuItemGroup.put(name, mi);

			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ClientFrame.this.showCardPanel(name);
				}
			});
			getViewMenu().add(mi);
		}
		if (button != null) {
			buttonGroup.add(button);
			hmButtonGroup.put(name, button);

			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ClientFrame.this.showCardPanel(name);
				}
			});
			getToolbarCommandPanel().add(button);
			OAButton.setup(button);
		}
	}

	private JPanel toolbarCommandPanel;

	public JPanel getToolbarCommandPanel() {
		if (toolbarCommandPanel != null) {
			return toolbarCommandPanel;
		}
		final int hgapForButtons = 20;
		toolbarCommandPanel = new JPanel() {
			final CopyOnWriteArrayList<JToggleButton> al = new CopyOnWriteArrayList<>();

			@Override
			public Component add(Component comp) {
				if (!(comp instanceof JToggleButton)) {
					return super.add(comp);
				}
				final JToggleButton btn = (JToggleButton) comp;
				if (al.contains(btn)) {
					return super.add(btn);
				}
				al.add(btn);
				btn.addComponentListener(new ComponentListener() {
					@Override
					public void componentShown(ComponentEvent e) {
						refresh();
					}

					@Override
					public void componentHidden(ComponentEvent e) {
						refresh();
					}

					@Override
					public void componentResized(ComponentEvent e) {
					}

					@Override
					public void componentMoved(ComponentEvent e) {
					}
				});
				refresh();
				return super.add(comp);
			}

			final AtomicBoolean abIgnore = new AtomicBoolean();

			void refresh() {
				if (!abIgnore.compareAndSet(false, true)) {
					return;
				}
				for (;;) {
					int x = al.size();
					try {
						setLayout(null);
						removeAll();
						setLayout(new GridLayout(1, 0, hgapForButtons, 0));
						for (JToggleButton btn : al) {
							if (btn.isVisible()) {
								add(btn);
							}
						}
						revalidate();
					} finally {
						abIgnore.set(false);
					}
					if (x == al.size()) {
						break;
					}
				}
			}
		};
		toolbarCommandPanel.setLayout(new GridLayout(1, 0, hgapForButtons, 0));
		toolbarCommandPanel.setOpaque(false);
		return toolbarCommandPanel;
	}

	private JLabel lblToolBar;

	public JLabel getToolBarLabel() {
		if (lblToolBar == null) {
			lblToolBar = new JLabel("   ");
			lblToolBar.setBorder(new EmptyBorder(2, 55, 2, 55));
			lblToolBar.setFont(lblToolBar.getFont().deriveFont(16.5f).deriveFont(Font.BOLD));
		}
		return lblToolBar;
	}

	public JToolBar getToolBar() {
		if (toolBar != null) {
			return toolBar;
		}
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setRollover(true);

		toolBar.add(Box.createHorizontalStrut(10));

		JPanel pan = new JPanel(new BorderLayout(0, 0));
		pan.add(getToolbarCommandPanel(), BorderLayout.WEST);
		toolBar.add(pan);

		toolBar.add(Box.createHorizontalStrut(10));
		toolBar.add(getToolBarLabel());
		toolBar.add(Box.createHorizontalStrut(10));

		// text needs to be toggled
		// toolBar.add(getOpenCloseNavigationPanelButton());

		/*
		toolBar.addSeparator();
		toolBar.add(Box.createHorizontalStrut(20));
		if (controlPrint != null) {
		    toolBar.add(controlPrint.getPrintPreviewButton());
		    toolBar.add(controlPrint.getPrintButton());
		}
		toolBar.addSeparator();
		*/

		toolBar.add(Box.createGlue());
		// toolBar.addSeparator();
		// addToolbarHelpButtons(toolBar);
		// toolBar.add(Box.createHorizontalStrut(10));

		return toolBar;
	}

	public void addToolbarHelpButtons(JToolBar toolBar) {
		if (toolBar == null) {
			return;
		}
		toolBar.add(getHelpButton());
		toolBar.add(getCSHelpButton());
	}

	private JMenuItem miExit;

	public JMenuItem getExitMenuItem() {
		if (miExit == null) {
			miExit = new JMenuItem("Exit");
			miExit.setIcon(Resource.getJarIcon(Resource.IMG_Exit));
			// miExit.setActionCommand(CMD_Exit);
			// miExit.addActionListener(this);
			miExit.setMnemonic(KeyEvent.VK_X);
			miExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_F4, java.awt.Event.ALT_MASK));
			miExit.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ClientFrame.this.onExit();
				}
			});
		}
		return miExit;
	}

	private JMenuItem miHelp;

	public JMenuItem getHelpMenuItem() {
		if (miHelp == null) {
			miHelp = new JMenuItem("Help ...");
			miHelp.setMnemonic('H');
			miHelp.setIcon(Resource.getJarIcon(Resource.IMG_Help));
			// miHelp.setToolTipText("");
			miHelp.setAccelerator(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		}
		return miHelp;
	}

	private JMenuItem miCSHelp;

	public JMenuItem getCSHelpMenuItem() {
		if (miCSHelp == null) {
			miCSHelp = new JMenuItem("Context Help ...");
			miCSHelp.setMnemonic('C');
			// miCSHelp.setToolTipText("");
			miCSHelp.setIcon(Resource.getJarIcon(Resource.getValue(Resource.IMG_CSHelp)));
		}
		return miCSHelp;
	}

	private JButton cmdHelp;

	public JButton getHelpButton() {
		if (cmdHelp == null) {
			cmdHelp = new JButton(Resource.getJarIcon(Resource.getValue(Resource.IMG_Help)));
			OACommand.setupButton(cmdHelp);
			cmdHelp.setToolTipText("Display Help window");
		}
		return cmdHelp;
	}

	private JButton cmdCSHelp;

	public JButton getCSHelpButton() {
		if (cmdCSHelp == null) {
			cmdCSHelp = new JButton(Resource.getJarIcon(Resource.getValue(Resource.IMG_CSHelp)));
			OACommand.setupButton(cmdCSHelp);
			cmdCSHelp.setToolTipText("Display Context Sensitive Help");
		}
		return cmdCSHelp;
	}

	public JMenuBar getMyMenuBar() {
		if (menuBar != null) {
			return menuBar;
		}
		menuBar = new JMenuBar();
		JMenu menu;
		JMenuItem mi;

		// MENU "File"
		menuBar.add(getFileMenu());

		// MENU "Edit"
		menuBar.add(getEditMenu());

		// MENU "View"
		menuBar.add(getViewMenu());

		// MENU "Help"
		menu = new JMenu("Help");
		menu.setMnemonic(KeyEvent.VK_H);
		menu.add(getHelpMenuItem());
		menu.add(getCSHelpMenuItem());
		menu.addSeparator();
		menu.add(getAboutMenuItem());
		menuBar.add(menu);

		return menuBar;
	}

	public JMenu getFileMenu() {
		if (menuFile != null) {
			return menuFile;
		}
		menuFile = new JMenu("File");
		menuFile.setMnemonic(KeyEvent.VK_F);
		menuFile.add(getSaveMenuItem());

		menuFile.addSeparator();

		if (controlPrint != null) {
			menuFile.add(controlPrint.getPageSetupMenuItem());
			menuFile.add(controlPrint.getPrintPreviewMenuItem());
			menuFile.add(controlPrint.getPrintMenuItem());
			menuFile.addSeparator();
			menuFile.add(controlPrint.getSaveAsPdfMenuItem());
			menuFile.addSeparator();
		}
		menuFile.add(getExitMenuItem());
		return menuFile;
	}

	public JMenu getEditMenu() {
		if (menuEdit != null) {
			return menuEdit;
		}
		menuEdit = new JMenu("Edit");
		menuEdit.setMnemonic(KeyEvent.VK_E);
		menuEdit.add(getUndoableController().getUndoMenuItem());
		menuEdit.add(getUndoableController().getRedoMenuItem());
		menuEdit.addSeparator();
		menuEdit.add(getCutCopyPasteController().getCutMenuItem());
		menuEdit.add(getCutCopyPasteController().getCopyMenuItem());
		menuEdit.add(getCutCopyPasteController().getPasteMenuItem());
		return menuEdit;
	}

	public JMenu getViewMenu() {
		if (menuView == null) {
			menuView = new JMenu("View");
			menuView.setMnemonic(KeyEvent.VK_V);
			// menuView.add(getMenuLF());
			// add for each panel
			//menuView.add(getAdminMenuItem());
		}
		return menuView;
	}

	public JMenu getMenuLF() {
		if (menuLF == null) {
			menuLF = new JMenu("Look & Feel");
		}
		return menuLF;
	}

	public JMenuItem getAboutMenuItem() {
		if (miAbout == null) {
			miAbout = new JMenuItem("About ...");
			miAbout.setMnemonic('A');
			// miAbout.setActionCommand(CMD_About);
			miAbout.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ClientFrame.this.onAbout();
				}
			});
		}
		return miAbout;
	}

	public JMenuItem getSaveMenuItem() {
		if (miSave == null) {
			miSave = new JMenuItem("Save");
			miSave.setIcon(Resource.getJarIcon(Resource.getValue(Resource.IMG_Save)));
			miSave.setMnemonic(KeyEvent.VK_S);
			miSave.setToolTipText("Save All Information");
			// miSave.setActionCommand(CMD_Save);
			miSave.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ClientFrame.this.onSave();
				}
			});
			miSave.setAccelerator(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_S, java.awt.Event.CTRL_MASK));
		}
		return miSave;
	}

	protected abstract void onSave();

	protected abstract void onExit();

	protected abstract void onAbout();

	protected abstract void onNotifyLabelMouseClick();

	public static void main(String[] args) {
		ClientFrame frm = new ClientFrame("Test", null) {
			@Override
			protected void onExit() {
				System.exit(0);
			}

			@Override
			protected void onSave() {
			}

			@Override
			protected void onAbout() {
			}

			@Override
			protected void onNotifyLabelMouseClick() {
			}
		};
		frm.setVisible(true);
	}
}
