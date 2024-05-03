// Copied from OATemplate project by OABuilder 03/12/24 01:50 PM
package com.viaoa.appstore.control;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.viaoa.appstore.control.client.ClientController;
import com.viaoa.appstore.control.server.ServerController;
import com.viaoa.appstore.control.single.SingleController;
import com.viaoa.appstore.control.webserver.WebserverController;
import com.viaoa.appstore.resource.Resource;
import com.viaoa.appstore.view.DummyFrame;
import com.viaoa.appstore.view.SplashWindow;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAConv;
import com.viaoa.util.OAConverter;
import com.viaoa.util.OANetwork;
import com.viaoa.util.OAString;

/**
 * startup that has the main(). Verifies that the run type is correct, else displays a usage console message. Verifies the JVM version.
 * Displays splash window, if run type is not service. Based on run type, will start the server or client controller. controls exit from
 * other controllers.
 *
 * @author VVia
 */
public class StartupController {
	private static Logger LOG = Logger.getLogger(StartupController.class.getName());

	private DummyFrame frmDummy;
	private SplashWindow splashWindow;

	// Controllers
	protected ServerController controlServer;
	protected WebserverController controlWebserver;
	protected ClientController controlClient;
	protected SingleController controlSingle;
	protected int runType;

	public StartupController(int runType, String[] args) {
		this.runType = runType;
		// must set this first
		Resource.setRunType(runType);

        // load args[] into runtime properties before using properties
        Resource.loadArguments(args);
		
		final boolean bUsesSwing = (runType == Resource.RUNTYPE_Client || runType == Resource.RUNTYPE_Server || runType == Resource.RUNTYPE_Single);

		String hostName = Resource.getValue(Resource.APP_HostName);
		String hostIPAddress = Resource.getValue(Resource.APP_HostIPAddress);
		try {
			if (OAString.isEmpty(hostName)) {
				hostName = OANetwork.getHostName();
				Resource.setValue(Resource.TYPE_Runtime, Resource.APP_HostName, hostName);
			}
			if (OAString.isEmpty(hostIPAddress)) {
				hostIPAddress = OANetwork.getIPAddress();
				Resource.setValue(Resource.TYPE_Runtime, Resource.APP_HostIPAddress, hostIPAddress);
			}
			String s = OANetwork.getIPAddresses();
			Resource.setValue(Resource.TYPE_Runtime, Resource.APP_HostIPAddresses, s);
		} catch (Exception e) {
		}

		// do this after setting rootDir
		if (runType == Resource.RUNTYPE_Server || runType == Resource.RUNTYPE_Service) {
			Resource.setValue(Resource.TYPE_Runtime, Resource.APP_Server, hostName);
			// server.ini
			Resource.getServerProperties();
		} else if (runType == Resource.RUNTYPE_Webserver) {
			// webserver.ini
			Resource.getWebserverProperties();
		} else if (runType == Resource.RUNTYPE_Client) {
			// client.ini
			Resource.getClientProperties();
		} else if (runType == Resource.RUNTYPE_Single) {
			// single.ini
			Resource.getSingleProperties();
		}

		System.out.println("Run type=" + Resource.STARTUP_TYPES[runType]);
		System.out.println("Root Directory=" + Resource.getRootDirectory());
		System.out.println("Version=" + Resource.getValue(Resource.APP_Version));
		System.out.println("Release=" + Resource.getValue(Resource.APP_Release));

		if (runType == Resource.RUNTYPE_Server || runType == Resource.RUNTYPE_Service || runType == Resource.RUNTYPE_Webserver) {
			System.out.println("ServerPort=" + Resource.getValue(Resource.APP_ServerPort));
			System.out.println("JettyPort=" + Resource.getValue(Resource.APP_JettyPort));
			System.out.println("JettySSLPort=" + Resource.getValue(Resource.APP_JettySSLPort));
			System.out.println("JettyDirectory=" + Resource.getValue(Resource.APP_JettyDirectory));
		}

		if (bUsesSwing) {
			getSplashWindow().setVisible(true);
		}
        com.viaoa.jfc.editor.html.protocol.classpath.Handler.register();

		// JDK version
		String verMini = Resource.getValue(Resource.APP_JDKVersion);
		String verCurrent = System.getProperty("java.runtime.version"); // was: java.version
		System.out.println("JDK version=" + verCurrent);
		
		/*
		if (!verifyJavaVersion(verMini, verCurrent)) {
		    onStartupError(Resource.MSG_InvalidJVM, new Object[] { verCurrent, verMini }, null);
		}
		*/

		if (Resource.getBoolean(Resource.INI_Debug)) {
			OAObject.setDebugMode(true);
		}

		boolean b = false;
		try {
			switch (runType) {
			case Resource.RUNTYPE_Client:
				b = getClientController().start();
				break;
			case Resource.RUNTYPE_Single:
				b = getSingleController().start();
				break;
			case Resource.RUNTYPE_Server:
			case Resource.RUNTYPE_Service:
				b = getServerController().start();
				break;
			case Resource.RUNTYPE_Webserver:
				b = getWebserverController().start();
				break;
			default:
				throw new Exception("Invalid startup type.");
			}
		} catch (Exception e) {
			onStartupError(Resource.MSG_StartupError, new Object[] { "" }, e);
		}
		if (b) {
			setupExceptionHandler();
		} else {
			onStartupError(Resource.MSG_StartupError, new String[] { "Failed to start" }, null);
		}
		if (bUsesSwing) {
			getSplashWindow().setVisible(false);
		}
	}

	protected SplashWindow getSplashWindow() {
		if (splashWindow == null) {
			splashWindow = new SplashWindow();
		}
		return splashWindow;
	}

	// All of these have msg set up as a Resource name.
	private void onStartupError(String msg, Object[] args, Throwable t) {
		if (t != null) {
			System.out.println("Exception: " + t);
			t.printStackTrace();
		}
		String title = Resource.getRunTimeName() + " Error";
		msg = Resource.getValue(msg, args);
		if (t != null) {
			String s = t.getMessage();
			if (s != null) {
				if (s.length() > 80) {
					s = s.substring(0, 80) + " ...";
				}
				msg += "\n" + s;
			}
		}
		System.out.println("Startup Error: " + msg);
		if (runType != Resource.RUNTYPE_Service) {
			JOptionPane.showMessageDialog(getDummyFrame(), msg, title, JOptionPane.ERROR_MESSAGE);
		}
		LOG.log(Level.WARNING, "startup exception, will exit", t);
		exitApplication(true);
	}

	public ClientController getClientController() {
		if (controlClient == null) {
			controlClient = new ClientController(getDummyFrame()) {
				protected void onExit() {
					StartupController.this.exitApplication();
				}
			};
		}
		return controlClient;
	}

	public SingleController getSingleController() {
		if (controlSingle == null) {
			controlSingle = new SingleController(getDummyFrame()) {
				protected void onExit() {
					StartupController.this.exitApplication();
				}
			};
		}
		return controlSingle;
	}

	public ServerController getServerController() {
		if (controlServer == null) {
			controlServer = new ServerController(getDummyFrame()) {
				protected void onExit() {
					StartupController.this.exitApplication();
				}
			};
		}
		return controlServer;
	}

	public WebserverController getWebserverController() {
		if (controlWebserver == null) {
			controlWebserver = new WebserverController() {
				protected void onExit() {
					StartupController.this.exitApplication();
				}
			};
		}
		return controlWebserver;
	}

	JFrame getDummyFrame() {
		if (frmDummy == null) {
			if (runType != Resource.RUNTYPE_Service) {
				frmDummy = new DummyFrame();
			}
		}
		return frmDummy;
	}

	protected void setupExceptionHandler() {
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				String s = "uncaught Exception in Thread=" + t.getName();
				LOG.log(Level.WARNING, s, e);
			}
		});
	}

	/**
	 * Minimum version of JDK required to run program ex: 1.6.0
	 */
	protected boolean verifyJavaVersion(String verMini, String verCurrent) {
		String s = OAString.field(verMini, ".", 1);
		if (s == null) {
			return false;
		}
		int v1 = OAConv.toInt(s);

		s = OAString.field(verMini, ".", 2);
		if (s == null) {
			return false;
		}
		int v2 = OAConv.toInt(s);

		s = OAString.field(verMini, ".", 3);
		if (s == null) {
			s = "0";
		}
		int v3 = OAConv.toInt(s);

		LOG.config("Using JDK " + verCurrent);
		boolean bValid = false;

		if (verCurrent != null) {
			verCurrent = OAString.field(verCurrent, "-", 1);
			if (v1 == OAConverter.toInt(OAString.field(verCurrent, ".", 1))) {
				if (v2 <= OAConverter.toInt(OAString.field(verCurrent, ".", 2))) {
					s = OAString.field(verCurrent, ".", 3);
					s = OAString.field(s, "_", 1);
					if (v3 <= OAConverter.toInt(s)) {
						bValid = true;
					}
				}
			}
		}
		return bValid;
	}

    private void exitApplication() {
        exitApplication(false);
    }
	private void exitApplication(final boolean bStartupError) {
		try {
			if (controlSingle != null) {
				controlSingle.close(bStartupError);
			} else if (controlClient != null) {
				controlClient.close(bStartupError);
			} else if (controlServer != null) {
				controlServer.close(bStartupError);
			}
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Exception during exit", e);
		}

		if (bStartupError) LOG.config("Startup errors, will exit now");
		else LOG.config("Looks Great! :) good bye");
		System.exit(0);
	}

	public static void main(String[] args) {
		/* debug
		    System.out.println("StartupController ************* MultiplexerSERVER/CLIENT.DEBUG = true;");
		    MultiplexerServer.DEBUG = true;
		    MultiplexerClient.DEBUG = true;
		*/
		System.setProperty("sun.java2d.dpiaware", "false");

		int runType = -1;
		boolean bDemo = false;
		for (int i = 0; runType < 0 && args != null && i < args.length; i++) {
			if (args[i] == null) {
				continue;
			}
			for (int j = 0; runType < 0 && j < Resource.STARTUP_TYPES.length; j++) {
				if (args[i].equalsIgnoreCase(Resource.STARTUP_TYPES[j])) {
					runType = j;
				}
			}
		}
		if (runType < 0) {
			displayUsage();
		} else {
			StartupController startup = new StartupController(runType, args);
		}
		for (;;) {
			try {
				Thread.sleep(60 * 1000);
			} catch (Exception e) {
				LOG.log(Level.WARNING, "Exception in main thread, that is doing nothing", e);
				break;
			}
		}
	}

	public static void displayUsage() {
		String s = "usage - must be one of the following: ";
		for (int j = 0; j < Resource.STARTUP_TYPES.length; j++) {
			if (j > 0) {
				s += ", ";
			}
			s += Resource.STARTUP_TYPES[j];
		}
		System.out.println("Error, " + s);
	}

}
