// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.control.client;

import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.viaoa.appstore.control.client.ClientJarStoreController;
import com.viaoa.appstore.model.oa.ServerApplication;
import com.viaoa.appstore.remote.RemoteClientJarStoreInterface;
import com.viaoa.appstore.control.HelpController;
import com.viaoa.appstore.control.LogController;
import com.viaoa.appstore.datasource.DataSource;
import com.viaoa.appstore.delegate.LogDelegate;
import com.viaoa.appstore.delegate.ModelDelegate;
import com.viaoa.appstore.delegate.RemoteDelegate;
import com.viaoa.appstore.model.oa.*;
import com.viaoa.appstore.model.oa.cs.ClientRoot;
import com.viaoa.appstore.model.oa.cs.ServerRoot;
import com.viaoa.appstore.resource.Resource;
import com.viaoa.appstore.util.Util;
import com.viaoa.context.OAContext;
import com.viaoa.datasource.OADataSource;
import com.viaoa.jfc.OAJfcUtil;
import com.viaoa.jfc.text.spellcheck.SpellChecker;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.sync.OASync;
import com.viaoa.sync.OASyncClient;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.sync.model.ClientInfo;
import com.viaoa.sync.remote.RemoteSessionInterface;
import com.viaoa.util.OAConv;
import com.viaoa.util.OAEncryption;
import com.viaoa.util.OALogger;
import com.viaoa.util.OAProperties;
import com.viaoa.util.OAReflect;
import com.viaoa.util.OAString;
import com.viaoa.util.Tuple;

/**
 * Main controller for starting in Client mode, login and frame.
 */
public abstract class ClientController {
	private static Logger LOG = Logger.getLogger(ClientController.class.getName());

	// Internationalized name/values stored in "values.properties" in the format directory
	public static final String MSG_ServerErrors = "control.ClientController.ServerErrors";

	private JFrame frmDummy;

	private LogController controlLog;
	private LoginController controlLogin;
	private HelpController controlHelp;
	private volatile ClientFrameController controlFrame;
	private ClientObjectController controlObject;
	private RemoteClientController controlRemote;
	private OADataSource dsClient;

    // Custom
    // qqqqqqqqqqq private ClientProcessController controlClientProcess;
	// Custom
//qqqqqqqq    private ClientJarStoreController controlClientJarStore;
    
    // Custom
    //private RemoteClientJarStoreInterface remoteClientJarStore;

	private ClientSpellCheckController controlClientSpellCheck;
	private SpellChecker spellChecker;
	private boolean bExitCalled;

	public ClientController(JFrame frmDummy) {
		this.frmDummy = frmDummy;
		Resource.setLocale(null); // todo: allow this to come from ini file

		String s = Resource.getValue(Resource.APP_ClientApplicationName);
		if (s == null) {
			s = "";
		}
		String s2 = Resource.getValue(Resource.APP_ServerDisplayName);
		if (!OAString.isEmpty(s2)) {
			s += " (" + s2 + ")";
		}

		Resource.setRunTimeName(s);
	}

	public boolean start() throws Exception {
		getLogController();

		String packageName = "com.viaoa.appstore.model.oa";
		String[] cnames = OAReflect.getClasses(packageName);
		for (String fn : cnames) {
			Class c = Class.forName(packageName + "." + fn);
			OAObjectInfoDelegate.getObjectInfo(c);
		}

		final StartSwingInfo ssi = new StartSwingInfo();

		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				try {
					startSwing(ssi);
				} catch (Exception e) {
					ssi.exception = e;
				}
			}
		});

		try {
			for (;;) {
				if (ssi.shouldExit) {
					break;
				}
				if (ssi.exception != null) {
					break;
				}
				if (ssi.swingWorker == null) {
					Thread.sleep(100);
				} else {
					ssi.swingWorker.get();
					break;
				}
			}
		} catch (Exception e) {
			ssi.exception = e;
		}
		if (ssi.exception != null) {
			ssi.shouldExit = true;
			LOG.log(Level.WARNING, "startup exception", ssi.exception);
			JOptionPane.showMessageDialog(	getFrame(),
											"Exception during startup, will exit\n" + ssi.exception,
											Resource.getRunTimeName(),
											JOptionPane.ERROR_MESSAGE);
		}
		if (ssi.shouldExit) {
			callExit();
		}

		getObjectController();

		OAProperties serverProps = RemoteDelegate.getRemoteApp().getServerProperties();
		String s = serverProps.getString(Resource.APP_Version);
		if (!OAString.isEmpty(s)) {
			Resource.setValue(Resource.TYPE_Client, Resource.APP_Version, s);
		}

		int x = Resource.getInt(Resource.CLIENT_CheckAWTThreadMinutes);
		if (x > 0) {
			runCheckAWTThread(x);
		}

		s = Resource.getValue(Resource.APP_NewWordsFileName);
		LOG.config("SpellCheck Controller loading file " + s);
		try {
			getSpellCheckController().loadLocalNewWordsTextFile(s);
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Error loading SpellCheck file " + s + ", will continue.", e);
		}

		LOG.config("Resource.SpellChecker set");
		Resource.setSpellChecker(getSpellChecker());

		LOG.config(Resource.getValue(Resource.APP_Welcome));

		// Custom
        LOG.fine("starting process controller, to manage running apps");
//qqqqqqqqqq        getClientProcessController();
		
		return true;
	}

	private static class StartSwingInfo {
		volatile SwingWorker<Void, Void> swingWorker;
		volatile Exception exception;
		volatile int release;
		volatile int serverRelease;
		volatile ServerRoot serverRoot;
		volatile ClientRoot clientRoot;
		volatile boolean shouldExit;
	}

	protected void startSwing(final StartSwingInfo ssi) throws Exception {
		final SwingWorker<Void, Void> sw0 = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				long ms = System.currentTimeMillis();
				OAEncryption.getCipher();
				long ms2 = System.currentTimeMillis();
				LOG.fine("OAEncryption.getCipher time " + (ms2 - ms) + "ms");
				return null;
			}
		};
		sw0.execute();

		LOG.fine("Initiale OAContext");
		OAContext.setContextHub(null, ModelDelegate.getLocalAppUserHub());

		setLookAndFeel(null);

		// go ahead and build frame in a background thread
		//   data wont be loaded until it's done.  Must be after L&F is set
		final SwingWorker<Void, Void> sw1 = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				long ms = System.currentTimeMillis();
				getFrameController().getFrame();
				long ms2 = System.currentTimeMillis();
				LOG.fine("frame load time " + (ms2 - ms) + "ms");
				return null;
			}
		};
		sw1.execute();

		if (!performLogin()) {
			ssi.shouldExit = true;
			return; // modal/block
		}

		ssi.release = OAConv.toInt(Resource.getValue(Resource.APP_Release));
		ssi.serverRelease = OAConv.toInt(RemoteDelegate.getRemoteApp().getRelease());
		if (ssi.release != ssi.serverRelease) {
			try {
				String version = RemoteDelegate.getRemoteApp().getResourceValue(Resource.APP_Version);
				String s = "Loading new version " + version + ", release: " + ssi.serverRelease + ", (current: " + ssi.release + ")";
				LOG.config(s);
				setProcessing(true, s);

				getRemoteClientController().onUpdateSoftwareForWindows(version, ssi.serverRelease);

				setProcessing(false);

				JOptionPane.showMessageDialog(	getFrame(),
												"New version " + ssi.serverRelease + " has been loaded\nplease restart the program",
												Resource.getRunTimeName(), JOptionPane.INFORMATION_MESSAGE);
				getFrameController().getFrame().setVisible(false);
			} catch (Exception e) {
				ssi.exception = e;
			}
			ssi.shouldExit = true;
			return;
		}

		// load the data from server in the background
		final SwingWorker<Void, Void> sw2 = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				LOG.fine("Loading data from server ...");
				ssi.serverRoot = RemoteDelegate.getRemoteApp().getServerRoot();
				int connectionId = OASync.getConnectionId();
				ssi.clientRoot = RemoteDelegate.getRemoteApp().getClientRoot(connectionId);
				LOG.fine("received data from server");
				return null;
			}
		};
		sw2.execute();

		try {
			sw1.get(1, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
		}

		getFrameController().getFrame().setVisible(true);

		// display modal processing message
		setProcessing(true, "Loading data from server ...");

		// wait for data to load and then initialize ui with data
		final SwingWorker<Void, Void> sw4 = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				try {
					sw2.get(30, TimeUnit.SECONDS);
				} catch (TimeoutException e) {
				}
				LOG.config("Initializing model  ...");
				initializeModel(ssi.serverRoot, ssi.clientRoot);
				LOG.config("Client Admin ready");
				return null;
			}
		};
		sw4.execute();

		// wait for initialize, then remove modal processing message
		ssi.swingWorker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				long ms = System.currentTimeMillis();
				sw4.get();
				ms = System.currentTimeMillis() - ms;
				if (ms < 1000) {
					Thread.sleep(1000 - ms);
				}
				return null;
			}

			@Override
			protected void done() {
				setProcessing(false);
			}
		};
		ssi.swingWorker.execute();
	}

	// not needed or used
	public OADataSource getDataSource() {
		if (dsClient != null) {
			return dsClient;
		}
		try {
			DataSource dsx = new DataSource();
			dsx.open();
			dsClient = dsx.getOADataSource();
			dsx.close();
		} catch (Exception e) {
			LOG.log(Level.WARNING, "error creating DataSource for client", e);
		}
		return dsClient;
	}

	public ClientObjectController getObjectController() {
		if (controlObject == null) {
			controlObject = new ClientObjectController();
		}
		return controlObject;
	}

	private boolean performLogin() {
		boolean b = false;
		if (Resource.getBoolean(Resource.INI_AutoLogin)) {
			b = getLoginController().autoLogin();
		}

		if (!b) {
			getLoginController().getLoginDialog().setVisible(true); // modal, connectServer() will be called, else exit()
		}

		return true;
	}

	private volatile boolean bCheckingAWT;

	private void runCheckAWTThread(final int minutes) {
		LOG.fine("start thread");
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				_runCheckAWTThread(minutes);
			}
		}, "CheckAWTThread");
		t.setDaemon(true);
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	private void _runCheckAWTThread(int minutes) {
		// run AWT thread checker, will send stack traces to server if it is frozen
		int errorCount = 0;
		for (;;) {
			try {
				bCheckingAWT = true;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						bCheckingAWT = false;
					}
				});
				Thread.sleep(((errorCount > 0) ? 1 : minutes) * 60 * 1000);
				if (bCheckingAWT && !Resource.getBoolean(Resource.INI_Debug)) {
					LOG.warning("AWTThread did not respond to invokeLater, dumping stack traces to log and sending to server.");
					ArrayList<String> list = controlLog.dumpStackTrace(); // writes to file, sends to server
					int connectionId = OASync.getSyncClient().getClientInfo().getConnectionId();
					RemoteDelegate.getRemoteApp().writeToClientLogFile(connectionId, list);
					if (++errorCount == 3) {
						callExit();
					}
				} else {
					errorCount = 0;
				}
			} catch (Exception e) {
				LOG.log(Level.WARNING, "Error while running LogController.dumpStackTrace", e);
				break;
			}
		}
	}

	/**
	 * This will "plug-in" the Hubs from ServerRoot into the ModelDelegate, which is used in the Models.
	 */
	private void initializeModel(ServerRoot rootServer, ClientRoot rootClient) {
		LOG.info("initialize model");

		ModelDelegate.initialize(rootServer, rootClient);

		getFrameController().afterModelLoaded();

		LOG.info("initialize models done");
	}

	protected LogController getLogController() {
		if (controlLog != null) {
			return controlLog;
		}
		controlLog = new LogController(false) {
			@Override
			protected void onStatusMessage(String msg) {
				ClientController.this.setStatus(msg);
			}

			@Override
			protected void onErrorMessage(String msg, Throwable thrown) {
				ClientController.this.onLoggerErrorMessage(msg, thrown);
			}
		};
		LogDelegate.setLogController(controlLog);

		// Have Logger remove all log files
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				int i1 = OAConv.toInt(Resource.getValue(Resource.APP_LogRegularDays, "14"));
				int i2 = OAConv.toInt(Resource.getValue(Resource.APP_LogErrorDays, "30"));
				LOG.config("removing regular log files > " + i1 + " days.");
				LOG.config("removing error log files > " + i2 + " days.");
				getLogController().removeOldLogFiles(i1, i2);
			}
		}, "LogController.removeOldLogFiles");
		t.start();

		return controlLog;
	}

	// send all log.warnings to server
	private Thread threadErrorMessage;
	private LinkedBlockingQueue<Tuple<String, Throwable>> queErrorMessage = new LinkedBlockingQueue<Tuple<String, Throwable>>();
	private long msLastErrorMessage;

	protected void onLoggerErrorMessage(String msg, Throwable thrown) {
		final long msNow = System.currentTimeMillis();
		if (msLastErrorMessage + 500 > msNow) {
			return;
		}
		msLastErrorMessage = msNow;

		queErrorMessage.offer(new Tuple(msg, thrown));

		if (threadErrorMessage != null) {
			return;
		}

		threadErrorMessage = new Thread(new Runnable() {
			@Override
			public void run() {
				for (;;) {
					try {
						Tuple<String, Throwable> t = queErrorMessage.take();
						System.out.println("Sending warning to server: " + t.a + ", exception: " + t.b.toString());
						if (OASyncDelegate.isConnected()) {
							RemoteSessionInterface rci = OASyncDelegate.getRemoteSession();
							if (rci != null) {
								rci.sendException("client exception: " + t.a, t.b);
							}
						}
					} catch (Exception e) {
					}
				}
			}
		}, "ErrorMessageHandler");
		threadErrorMessage.setDaemon(true);
		threadErrorMessage.start();
	}

	protected ClientSpellCheckController getSpellCheckController() {
		if (controlClientSpellCheck == null) {
			controlClientSpellCheck = new ClientSpellCheckController();
		}
		return controlClientSpellCheck;
	}

	private SpellChecker getSpellChecker() {
		if (spellChecker != null) {
			return spellChecker;
		}
		spellChecker = new SpellChecker() {
			@Override
			public boolean isWordFound(String word) {
				return getSpellCheckController().isWordFound(word);
			}

			@Override
			public String[] getMatches(String text) {
				return getSpellCheckController().getMatches(text, 50);
			}

			@Override
			public String[] getSoundexMatches(String text) {
				return getSpellCheckController().getSoundexMatches(text);
			}

			@Override
			public void addNewWord(String word) {
				getSpellCheckController().addNewWord(word);
			}
		};

		return spellChecker;
	}

	protected synchronized HelpController getHelpController() {
		if (controlHelp == null) {
			controlHelp = new HelpController();
		}
		return controlHelp;
	}

	public LoginController getLoginController() {
		if (controlLogin == null) {
			controlLogin = new LoginController(getFrame(), getHelpController()) {
				@Override
				protected boolean onConnectToServer(String server, int port) throws Exception {
					boolean bResult = ClientController.this.connectServer(server, port);
					return bResult;
				}

				@Override
				protected void onExit() {
					ClientController.this.callExit();
				}

				@Override
				protected void onLogin(String user, String location) {
					ClientInfo ci = OASync.getSyncClient().getClientInfo();
					ci.setUserId(user);
					ci.setUserName(System.getProperty("user.name"));
					ci.setLocation(location);
					int release = OAConv.toInt(Resource.getValue(Resource.APP_Release));
					ci.setVersion("" + release);
					RemoteSessionInterface sess = OASync.getRemoteSession();
					if (sess != null) {
						sess.update(ci);
					}
				}

				@Override
				protected void setUserLogin(final AppUserLogin userLogin) {
					ModelDelegate.setLocalAppUserLogin(userLogin);
				}

				@Override
				protected void beforeEndingLoginProcess() {
					if (SwingUtilities.isEventDispatchThread()) {
						ClientController.this.getFrameController().getFrame();
					}
				}
			};
		}
		return controlLogin;
	}

	public JFrame getFrame() {
		JFrame frm = null;
		// if (controlClientFrame != null) frm = getClientFrameController().getFrame();
		if (controlFrame != null) {
			if (getFrameController().isFrameCreated()) {
				frm = getFrameController().getFrame();
			}
		}
		if (frm == null) {
			frm = frmDummy;
		}
		return frm;
	}

	public ClientFrameController getFrameController() {
		if (controlFrame != null) {
			return controlFrame;
		}

		synchronized (this) {
			if (controlFrame != null) {
				return controlFrame;
			}
			controlFrame = createFrameController();
		}
		return controlFrame;
	}

	private ClientFrameController createFrameController() {
		ClientFrameController fc = new ClientFrameController(getHelpController()) {
			@Override
			public void onExit() {
				ClientController.this.callExit();
			}

			@Override
			public void onSave() {
				ClientController.this.saveData();
			}

			@Override
			protected void setLookAndFeel(String laf) {
				ClientController.this.setLookAndFeel(laf);
			}
		};
		return fc;
	}

	protected void saveData() {
		RemoteDelegate.getRemoteApp().saveData();
		LOG.config("Data has been saved on Server");
		/*
		try {
		    if (controlDataSource == null) {
		        controlDataSource = new DataSourceController();
		    }
		    controlDataSource.writeSerializeToFile(false);
		}
		catch (Exception e) {
		    System.out.println("Exception: " + e);
		    e.printStackTrace();
		}
		*/
		// save local dictionary
		if (controlClientSpellCheck != null) {
			String s = Resource.getValue(Resource.APP_NewWordsFileName);
			LOG.fine("SpellCheck Controller saving file " + s);
			try {
				getSpellCheckController().saveLocalNewWordsTextFile(s);
			} catch (Throwable e) {
				LOG.log(Level.FINE, "Error saving SpellCheck file " + s, e);
			}
		}
	}

	boolean setStatus(String msg) {
		if (controlFrame != null) {
			if (controlFrame.isFrameCreated()) {
				getFrameController().setStatus(msg);
				return false;
			}
		}
		return false;
	}

	boolean setProcessing(boolean bVisible) {
		return setProcessing(bVisible, "");
	}

	boolean setProcessing(boolean bVisible, String msg) {
		if (controlFrame != null) {
			if (controlFrame.isFrameCreated()) {
				getFrameController().setProcessing(bVisible, msg);
				return true;
			}
		}
		return false;
	}

	public void setLookAndFeel(String laf) {
		LOG.fine("L&F=" + laf);
		boolean bWasNull = (laf == null);
		if (laf == null) {
			laf = Resource.getValue(Resource.APP_LookAndFeel, (String) null);
		}

		try {
			laf = OAJfcUtil.setLookAndFeel(laf);

			// SwingUtilities.updateComponentTreeUI(..)
			if (controlFrame != null) {
				getFrameController().updateUI();
			}
			if (controlHelp != null) {
				getHelpController().updateUI();
			}

			if (!bWasNull) {
				Resource.setValue(Resource.TYPE_Client, Resource.APP_LookAndFeel, laf);
				Resource.save();
			}
			LOG.fine("L&F set to " + laf);
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Error setting L&F to " + laf, e);
		} finally {
		}
	}

	protected void onClientDisconnect(Exception e) {
		if (bExitCalled) {
			return;
		}
		JOptionPane.showMessageDialog(	getFrame(),
										Resource.getRunTimeName() + " has been disconnected from the server, \nplease restart the program",
										Resource.getRunTimeName(), JOptionPane.INFORMATION_MESSAGE);
		ClientController.this.callExit();
	}

	private boolean connectServer(String serverName, int port) throws Exception {
		try {
			if (port < 1) {
				port = Registry.REGISTRY_PORT;
				String s = Resource.getValue(Resource.APP_ServerPort);
				if (s != null && OAString.isNumber(s)) {
					port = OAConv.toInt(s);
				}
			}

			// see if it is already connected
			if (getRemoteClientController().isConnected()) {
				OASyncClient sc = getRemoteClientController().getSyncClient();
				if (sc != null) {
					if (sc.getPort() == port) {
						if (sc.getHost().equals(serverName)) {
							return true; // already connected
						}
					}
				}
				getRemoteClientController().close();
			}

			LOG.config("Connecting to Server=" + serverName + ", port=" + port);

			getRemoteClientController().start(serverName, port);
			if (getRemoteClientController().isConnected()) {
				LOG.config("Connected to Server");
				return true;
			}
		} catch (Exception e) {
			throw e;
		}
		LOG.config("Connection to Server failed");
		return false;
	}

	public RemoteClientController getRemoteClientController() {
		try {
			return _getRemoteClientController();
		} catch (Exception e) {
			LOG.log(OALogger.ERROR, "Error getting RMIClient", e);
		}
		return controlRemote;
	}

	private RemoteClientController _getRemoteClientController() throws Exception {
		if (controlRemote != null) return controlRemote;
		
		// Custom
		controlRemote = new RemoteClientController() {
			protected @Override void onDisconnect(Exception e) {
				ClientController.this.onClientDisconnect(e);
			}

			@Override
			protected JFrame getFrame() {
				return ClientController.this.getFrame();
			}
		};
		return controlRemote;
	}

    // Custom
/*qqqqqqqqqqqqq	
    public ClientProcessController getClientProcessController() {
        if (controlClientProcess == null) {
            controlClientProcess = new ClientProcessController() {
                @Override
                protected void onLoadApplicationVersion(ServerApplication serverApp) throws Exception {
                    if (serverApp == null) return;
                    
                    ApplicationVersion appVer = serverApp.getApplicationVersion();
                    
                    ApplicationType appType = appVer.getApplicationType();
                    LOG.fine("checking and loading application version from server, application="+appType.getName());
                    int release = ClientController.this.getClientJarStoreController().getRelease(serverApp);
                    
                    if (appVer.getRelease() != release) {
                        ClientController.this.getClientJarStoreController().getJarForServer(serverApp);
                    }
                    else {
                        ClientController.this.getClientJarStoreController().getJarFromServer(serverApp);
                    }
                }
            };
        }
        return controlClientProcess;
    }
*/                    
	
	
	// Custom
/*qqqqqqqq    
    protected ClientJarStoreController getClientJarStoreController() {
        if (controlClientJarStore == null) {
            controlClientJarStore = new ClientJarStoreController(OASync.getSyncClient().getRemoteMultiplexerClient().getMultiplexerClient());
        }
        return controlClientJarStore;
    }
*/	
    public void close() {
        close(false);
    }    
	public void close(final boolean bStartupError) {
		if (controlFrame != null) {
			controlFrame.close(); // saves window size/location to ini file and frm.setVisible(false)
		}
		if (controlRemote != null && controlRemote.isConnected()) {
			try {
				// saveData();
				controlRemote.close();
			} catch (Exception e) {
			}
		}
	}

	private void callExit() {
		bExitCalled = true;
		onExit();
	}

	protected abstract void onExit();

	public static void main(String[] args) {
		// Resource.setRootDir(".");
		Resource.loadArguments(args);
		Resource.setRunType(Resource.RUNTYPE_Client);
		ClientController sc = new ClientController(null) { // StartupController.RUNTYPE_Client
			@Override
			protected void onExit() {
				close();
				LOG.config("Looks Great! :) good bye");
				System.exit(0);
			}
		};
		try {
			sc.start();
		} catch (Exception e) {
			System.out.println("Exception: " + e);
			e.printStackTrace();
			System.exit(1);
		}
	}
}
