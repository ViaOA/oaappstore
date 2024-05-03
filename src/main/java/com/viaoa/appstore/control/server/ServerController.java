// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.control.server;

import java.awt.Cursor;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.viaoa.context.OAContext;
import com.viaoa.context.OAUserAccess;
import com.viaoa.hub.Hub;
import com.viaoa.jfc.text.spellcheck.SpellChecker;
import com.viaoa.object.OACascade;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectReflectDelegate;
import com.viaoa.process.OACronProcessor;
import com.viaoa.sync.model.ClientInfo;
import com.viaoa.util.OAConv;
import com.viaoa.util.OADate;
import com.viaoa.util.OADateTime;
import com.viaoa.util.OAFile;
import com.viaoa.util.OAProperties;
import com.viaoa.util.OAString;
import com.viaoa.util.OATime;
import com.viaoa.web.filter.OAUserAccessFilter;
import com.viaoa.appstore.control.LogController;
import com.viaoa.appstore.delegate.CronDelegate;
import com.viaoa.appstore.delegate.ExecutorServiceDelegate;
import com.viaoa.appstore.delegate.LogDelegate;
import com.viaoa.appstore.delegate.ModelDelegate;
import com.viaoa.appstore.delegate.RemoteDelegate;
import com.viaoa.appstore.delegate.ScheduledExecutorServiceDelegate;
import com.viaoa.appstore.model.oa.AppServer;
import com.viaoa.appstore.model.oa.AppUser;
import com.viaoa.appstore.model.oa.AppUserError;
import com.viaoa.appstore.model.oa.AppUserLogin;
import com.viaoa.appstore.model.oa.cs.ClientRoot;
import com.viaoa.appstore.model.oa.cs.ServerRoot;
import com.viaoa.appstore.remote.RemoteAppImpl;
import com.viaoa.appstore.remote.RemoteAppInterface;
import com.viaoa.appstore.remote.RemoteClientJarStoreInterface;
import com.viaoa.appstore.remote.RemoteFileImpl;
import com.viaoa.appstore.remote.RemoteFileInterface;
import com.viaoa.appstore.remote.RemoteSpellCheckInterface;
import com.viaoa.appstore.resource.Resource;

/**
 * Starts up server controllers.
 *
 * @author VVia
 */
public abstract class ServerController {
	private static Logger LOG = Logger.getLogger(ServerController.class.getName());

    // Controllers
    private LogController controlLog;
    private DataSourceController controlDataSource;
    private RemoteServerController controlRemote;
    private ObjectController controlObject;
    private ServerFrameController controlServerFrame;
    private ServerSpellCheckController controlServerSpellCheck;
    private JettyController controlJetty;
    private OAUserAccessFilter filterUserAccess;
    private ConnectionController controlConnection;
    private SpellChecker spellChecker;
    private ServerJarStoreController controlServerJarStore; // custom
    private JarStoreController controlJarStore; // custom

    private ServerRoot serverRoot;
    private Hub<ClientRoot> hubClientRoots;

    // remote objects
    private RemoteFileInterface remoteFile;
    private RemoteSpellCheckInterface remoteSpellCheck;
    private RemoteAppInterface remoteServer;

    // remote clients
    /*$$Start: ServerController.remoteClient1 $$*/
    /*$$End: ServerController.remoteClient1 $$*/

    private boolean bRunAsService;
    private JFrame frmDummy;
    
    private final Object LOCKSaving = new Object(); 
    private final Object LOCKSaveWait = new Object();
    private final Object LOCKGetUser = new Object();
    private final AtomicInteger aiSave = new AtomicInteger();
    private volatile boolean bApplicationClosed;

    public ServerController(JFrame frmDummy) {
        this.bRunAsService = (Resource.getRunType() == Resource.RUNTYPE_Service);
        this.frmDummy = frmDummy;
        Resource.setRunTimeName(Resource.getValue(Resource.APP_ServerApplicationName));
    }

    public boolean start() throws Exception {
        try {
            if (!bRunAsService) {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        getServerFrameController().getFrame().setVisible(true);
                    }
                });
            }
            if (controlServerFrame != null) {
                controlServerFrame.setProcessing(true, "Starting Server ...");
            }

            boolean b = _start();

            LOG.config(Resource.getValue(Resource.APP_Welcome));
            return b;
        } finally {
            if (controlServerFrame != null) {
                controlServerFrame.setProcessing(false);
            }
        }
    }

    private boolean _start() throws Exception {
        // Logging
        getLogController();
        LOG.config("Logging started");
        if (bRunAsService) {
            LOG.config("Running as a Windows Service");
        }

        // needs to be set before datasource controller
        LOG.config("create and start cron process");
        OACronProcessor cp = new OACronProcessor();
        CronDelegate.setCronProcessor(cp);
        CronDelegate.getCronProcessor().start();

        // DataSource
        boolean bCheckForDatasourceCorruption = Resource.getBoolean(Resource.DB_CheckForCorruption, true);

        // set up so that a DB failure will cause the next start to call isDatabaseCorrupted
        // Resource.setValue(Resource.TYPE_Server, Resource.DB_CheckForCorruption, "true");
        // Resource.save();

        // 1: verify that database exists and can be opened
        LOG.config("Starting DataSource");
        if (!getDataSourceController().isDataSourceReady()) {
            String msg = "DataSource can not be opened.";
            LOG.severe(msg); // this will cause program to exit
            throw new Exception(msg);
        }

        // 2: verify that database files are not corrupted.
        //    if they are damaged, then perform a forward restore from backup + log files.
        for (int i = 0; bCheckForDatasourceCorruption && i < 2; i++) {
            LOG.config("Verifying DataSource files");
            if (!getDataSourceController().isDataSourceCorrupted()) {
                break;
            }

            String msg = "DataSource is not valid.";
            LOG.severe(msg); // this will cause program to exit
            if (i > 0) {
                throw new Exception(msg);
            }

            String dirName = Resource.getValue(Resource.DB_BackupDirectory, "dbbackup");

            LOG.config("performing a forward restore from directory " + dirName);
            getDataSourceController().restoreDataSource(dirName);
            LOG.config("successfully performed a forward restore.");
        }

        // don't need to call isDatabaseCorrupted on the next start up
        // Resource.setValue(Resource.TYPE_Server, Resource.DB_CheckForCorruption, "false");
        // Resource.save();

        // 3: verify that the database structures: tables/columns/indexes/etc
        LOG.config("Verifying database");
        if (Resource.getBoolean(Resource.DB_Verify, true) && !getDataSourceController().verifyDataSource()) {
            String msg = "Can not verify Database, please check database settings in the \"server.ini\" file.";
            LOG.severe(msg); // this will cause program to exit
            throw new Exception(msg);
        }

        // 4: load the data
        LOG.config("Loading data for Server Root");
        if (!getDataSourceController().loadServerRoot()) {
            return false; // error loading data from DS
        }

        // set admin user for this server
        AppUser user = null;
        for (AppUser au : getServerRoot().getAppUsers()) {
            if (au.getAdmin()) {
                user = au;
                break;
            }
        }
        if (user == null) {
            user = new AppUser();
            user.setFirstName("Admin");
            user.setLastName("Admin");
            user.setAdmin(true);
            user.setLoginId("admin");
            user.setPassword(OAString.convertToSHAHash("admin"));
            user.save();
            getServerRoot().getAppUsers().add(user);
        }
        ModelDelegate.setLocalAppUser(user);

        LOG.config("Initializing OAContext ... as admin user");
        OAContext.setContextHub(null, ModelDelegate.getLocalAppUserHub());

        // initialize serverRoot, ModelDelegate
        ModelDelegate.initialize(serverRoot, null);

        /*
        LOG.config("Update database");
        getDataSourceController().updateDataSource();
        */

        // must be after ModelDelegate is initialized
        LOG.config("Starting Object Controller");
        getObjectController().start();

        AppServer appServer = ModelDelegate.getAppServer();
        appServer.setStarted(null);
        appServer.setCreated(new OADateTime());
        appServer.setRelease("" + Resource.getInt(Resource.APP_Release));

        if (Resource.getBoolean(Resource.SERVER_PreloadData)) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    preloadData();
                }
            });
            t.setName("Preload Data");
            t.setPriority(Thread.MIN_PRIORITY);
            t.start();
        }

        final AppUserLogin userLogin = new AppUserLogin();
        userLogin.setAppUser(user);
        userLogin.setConnectionId(0);
        userLogin.setLocation("App Server");
        userLogin.setComputerName(System.getProperty("user.name"));

        userLogin.setHostName(Resource.getValue(Resource.APP_HostName));
        userLogin.setIpAddress(Resource.getValue(Resource.APP_HostIPAddress));

        appServer.setAppUserLogin(userLogin);

        ModelDelegate.setLocalAppUserLogin(userLogin);
        onClientConnect(null, 0); // create a fake connection
        getConnectionController().setUserLogin(0, userLogin);

        LOG.fine("SpellCheck Controller");
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                String root = Resource.getRootDirectory() + "/";
                String s = null;
                try {
                    s = Resource.getValue(Resource.APP_DictionaryFileName);
                    getSpellCheckController().loadDictionaryTextFile(OAFile.convertFileName(root + s));
                    s = Resource.getValue(Resource.APP_DictionaryFileName2);
                    getSpellCheckController().loadDictionaryTextFile(OAFile.convertFileName(root + s));
                    s = Resource.getValue(Resource.APP_NewWordsFileName);
                    getSpellCheckController().loadNewWordsTextFile(OAFile.convertFileName(root + s));
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Error loading SpellCheck file " + root + s + ", will continue.", e);
                }
            }
        });
        t.setName("SpellCheck.preload");
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();

        Resource.setSpellChecker(getSpellChecker());

        // Thread to save data every 5 minutes
        LOG.config("Starting DB thread");
        startDatabaseAutoSaveThread();

        // Remote Server
        LOG.config("Starting Remote Server");
        createRemoteServerController();

        LOG.config("Starting LogController.enableServerMonitor");
        // getLogController().enableServerMonitor(true, (OAServerImpl) getRemoteServer().getOAServer(), 30 * 60); // 30 minutes

        // start webserver
        int port = Resource.getInt(Resource.APP_JettyPort, 8080);
        int portSSL = Resource.getInt(Resource.APP_JettySSLPort, 0);
        LOG.config("Jetty port=" + port + ", sslPort=" + portSSL);
        getJettyController().init(port, portSSL, getJettyUserAccessFilter());
        getJettyController().start();

        // remove old log files
        Runnable r = new Runnable() {
            @Override
            public void run() {
                int i1 = OAConv.toInt(Resource.getValue(Resource.APP_LogRegularDays, "14"));
                int i2 = OAConv.toInt(Resource.getValue(Resource.APP_LogErrorDays, "30"));
                LOG.config("removing regular log files > " + i1 + " days.");
                LOG.config("removing error log files > " + i2 + " days.");
                getLogController().removeOldLogFiles(i1, i2);
            }
        };
        ExecutorServiceDelegate.submit(r);
        ScheduledExecutorServiceDelegate.scheduleEvery(r, new OATime(0, 30, 0)); // run at 12:30am

        if (Resource.getBoolean(Resource.INI_EnableShutdownHook, true)) {
            LOG.config("adding ShutdownHook to call close");
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        if (!bApplicationClosed) {
                            LOG.fine("calling ShutdownHook");
                            ServerController.this.close();
                        }
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "Exception in shutDownHook", e);
                    }
                }
            });
        }

        // Custom
        LOG.config("Starting ServerJarStoreController that create server side socket for clients to send App Jar file updates");
        getServerJarStoreController().start();

        // Custom
        LOG.config("Starting JarStoreController");
        getJarStoreController().start();
        
        appServer.setStarted(new OADateTime());
        return true;
    }

	protected void disconnect(int id) {
		LOG.fine("disconnect connectionId=" + id);
		Socket socket = getConnectionController().getSocket(id);
		if (socket != null) {
			try {
				socket.close();
			} catch (Exception e) {
			}
		}
	}

	protected JettyController getJettyController() {
		if (controlJetty == null) {
			controlJetty = new JettyController() {
				@Override
				public boolean isValidUser(String userId, String password) {
					AppUser user = _getUser(-1, userId, password, "", "");
					return (user != null);
				}
			};
		}
		return controlJetty;
	}

	public OAUserAccessFilter getJettyUserAccessFilter() {
		if (filterUserAccess != null) {
			return filterUserAccess;
		}
		filterUserAccess = new OAUserAccessFilter() {
			@Override
			protected OAObject getWebUser(String userId, String password) {
				// customize this to allow other types of users, ex: Employee, Customer
				AppUser user = _getUser(-1, userId, password, "", "");
				return user;
			}

			@Override
			protected OAObject getContextUser(OAObject webUser) {
				// customize this to return the context user that is defined in the OABuilder model, ex: AppUser that would be used for webUser
				return webUser;
			}

			@Override
			protected OAUserAccess getContextUserAccess(OAObject webUser, OAObject contextUser) {
				// customize this to allow for user access
				return null;
			}
		};
		filterUserAccess.setAuthType(OAUserAccessFilter.AuthType.HttpBasic);
		return filterUserAccess;
	}

	class LoaderThread extends Thread {
		int id;
		OACascade cascade;

		public LoaderThread(int id, OACascade cascade) {
			this.id = id;
			this.cascade = cascade;
			setName("LoaderThread." + id);
		}

		ArrayList<OAObject> al = new ArrayList<OAObject>();

		@Override
		public void run() {
			for (OAObject obj : al) {
				_preloadData(obj, cascade);
			}
			LOG.config(getName() + " completed");
		}
	}

	protected void preloadData() {
		LOG.config("Starting threads for Pre-Loading data");

		int cntThread = Resource.getInt(Resource.SERVER_PreloadDataMaxThreadCount);
		if (cntThread > 0) {
			/* todo: this can be customized to preload data
			cntThread = Math.min(cntThread, serverRoot.getPrograms().getSize());
			if (cntThread < 1) cntThread = 1;
			OACascade cascade = new OACascade(true);  // true=used by multiple threads
			LoaderThread[] threads = new ProgramLoaderThread[cntThread];
			for (int i=0; i<cntThread; i++) {
			    threads[i] = new LoaderThread(i, cascade);
			}
			
			int cnt = 0;
			for (Program prog : serverRoot.getPrograms()) {
			    threads[cnt++ % cntThread].al.add(prog);
			}
			for (int i=0; i<cntThread; i++) {
			    threads[i].start();
			}
			*/
		}

		serverRoot.loadReferences(1, 1, true, 500);
	}

	private void _preloadData(OAObject obj, OACascade cascade) {
		LOG.fine(Thread.currentThread().getName() + ", loading data for object=" + obj);
		try {
			Thread.sleep(((int) (Math.random() * 15)) * 1000);
		} catch (Exception e) {
		}
		OAObjectReflectDelegate.loadAllReferences(obj, 1, 1, true, cascade, 500);
	}

	public RemoteServerController getRemoteServerController() {
		return controlRemote;
	}

	protected void createRemoteServerController() throws Exception {
		if (controlRemote != null) {
			return;
		}
		int port = Resource.getInt(Resource.APP_ServerPort, 1099);
		controlRemote = new RemoteServerController(port) {
			@Override
			protected void onClientConnect(Socket socket, int connectionId) {
				ServerController.this.onClientConnect(socket, connectionId);
			}

			@Override
			protected void onClientDisconnect(int connectionId) {
				ServerController.this.onClientDisconnect(connectionId);
			}

			@Override
			public void onUpdate(ClientInfo ci) {
				getConnectionController().update(ci);
			}

			@Override
			protected String getLogFileName() {
				return getLogController().createLogFileName("remote");
			}

			@Override
			protected void onClientException(ClientInfo ci, String msg, Throwable ex) {
				ServerController.this.handleException(ci.getConnectionId(), false, msg, ex);
			}
		};

		// setup remote objects
		controlRemote.getSyncServer().createSyncLookup(RemoteAppInterface.BindName, getRemoteServer(), RemoteAppInterface.class);
		RemoteDelegate.setRemoteApp(getRemoteServer());

		controlRemote.getSyncServer().createLookup(	RemoteSpellCheckInterface.BindName, getRemoteSpellCheck(),
													RemoteSpellCheckInterface.class);
		RemoteDelegate.setRemoteSpellCheck(getRemoteSpellCheck());

		controlRemote.getSyncServer().createLookup(RemoteFileInterface.BindName, getRemoteFile(), RemoteFileInterface.class);
		RemoteDelegate.setRemoteFile(getRemoteFile());

		// now start RemoteServer
		controlRemote.start();
	}

	protected void handleException(int clientId, boolean bFromServer, String msg, Throwable e) {
		String s;
		if (bFromServer) {
			s = "From Server ";
		} else {
			s = "From client, connection=" + clientId;
		}

		AppUserLogin userLogin = getConnectionController().getAppUserLogin(clientId);
		if (userLogin != null) {
			s += " (" + userLogin.getAppUser().getDisplayName() + ")";
		}
		s = "Remote Error: " + s + msg;

		LOG.log(Level.WARNING, s, e);

		if (userLogin == null) {
			return;
		}

		if (userLogin.getAppUserErrors().size() > 100) {
			return;
		}
		AppUserError userError = new AppUserError();
		userError.setDateTime(new OADateTime());
		userError.setMessage(msg);

		if (e != null) {
			StringWriter sw = new StringWriter(2048);
			e.printStackTrace(new PrintWriter(sw));
			userError.setStackTrace(sw.toString());
		}
		userLogin.getAppUserErrors().add(userError);
	}

	public RemoteFileInterface getRemoteFile() {
		if (remoteFile != null) {
			return remoteFile;
		}
		remoteFile = new RemoteFileImpl();
		return remoteFile;
	}

	public RemoteSpellCheckInterface getRemoteSpellCheck() {
		if (remoteSpellCheck != null) {
			return remoteSpellCheck;
		}
		remoteSpellCheck = new RemoteSpellCheckInterface() {
			@Override
			public boolean isWordFound(String word) {
				return getSpellChecker().isWordFound(word);
			}

			@Override
			public String[] getSoundexMatchingWords(String word) {
				return getSpellChecker().getSoundexMatches(word);
			}

			@Override
			public String[] getMatchingWords(String word) {
				return getSpellChecker().getMatches(word);
			}

			@Override
			public void addNewWord(String word) {
				getSpellChecker().addNewWord(word);
			}
		};
		return remoteSpellCheck;
	}

	protected void writeToClientLogFile(int clientId, ArrayList<String> stackTrace) throws Exception {
		String fname = Resource.getLogsDirectory() + "/";
		fname += "Client_StackTrace_" + (new OADateTime()).toString("yyyyMMdd_HHmm") + ".log";
		fname = OAString.convertFileName(fname);
		File file = new File(fname);
		PrintWriter pw = new PrintWriter(file);
		pw.println("Client Id = " + clientId);
		for (String s : stackTrace) {
			pw.println(s);
		}
		pw.close();
	}

	public ServerRoot getServerRoot() {
		if (serverRoot == null) {
			serverRoot = new ServerRoot();
		}
		return serverRoot;
	}

	/**
	 * Used internally to manage all ClientRoot objects, so that the data can be saved to datasource.
	 */
	protected Hub<ClientRoot> getClientRoots() {
		if (hubClientRoots == null) {
			hubClientRoots = new Hub<ClientRoot>(ClientRoot.class);
		}
		return hubClientRoots;
	}

	public ConnectionController getConnectionController() {
		if (controlConnection == null) {
			controlConnection = new ConnectionController();
			RemoteDelegate.setConnectionController(controlConnection);
		}
		return controlConnection;
	}

	protected void onClientConnect(Socket socket, int connectionId) {
		LOG.fine("connectionId=" + connectionId);
		ClientRoot cr = new ClientRoot();
		cr.setId(connectionId);
		cr.save();
		getClientRoots().add(cr);

		getConnectionController().add(connectionId, socket, cr);
	}

	protected void onClientDisconnect(int connectionId) {
		LOG.fine("connectionId=" + connectionId);
		ClientRoot cr = getConnectionController().getClientRoot(connectionId);
		if (cr != null) {
			cr.save(OAObject.CASCADE_ALL_LINKS);
			getClientRoots().remove(cr);
		}

		AppUserLogin userLogin = getConnectionController().getAppUserLogin(connectionId);
		if (userLogin != null) {
			userLogin.setDisconnected(new OADateTime());
		}

		getConnectionController().remove(connectionId);

        // Custom
        getJarStoreController().removeClientJarStoreInterface(connectionId);
		
		/*$$Start: ServerController.remoteClient2 $$*/
/*$$End: ServerController.remoteClient2 $$*/
	}

	protected ObjectController getObjectController() {
		if (controlObject == null) {
			controlObject = new ObjectController();
		}
		return controlObject;
	}

	protected ServerSpellCheckController getSpellCheckController() {
		if (controlServerSpellCheck == null) {
			controlServerSpellCheck = new ServerSpellCheckController();
		}
		return controlServerSpellCheck;
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

	public DataSourceController getDataSourceController() {
		if (controlDataSource == null) {
			try {
				controlDataSource = new DataSourceController(getServerRoot(), getClientRoots());
			} catch (Exception e) {
				LOG.log(Level.WARNING, "DataSource Error", e);
				handleException(0, true, "DataSource error", e);
			}
		}
		return controlDataSource;
	}

    public void saveData() {
        aiSave.getAndIncrement();
        synchronized (LOCKSaveWait) {
            LOCKSaveWait.notify();
        }
    }

    private void startDatabaseAutoSaveThread() {
        LOG.fine("entering");
        Thread t = new Thread(new Runnable() {
            OADate dateLastBackup = new OADate(); // set today, so that it will run after midnight

            public void run() {
                for (int i = 0; !bApplicationClosed; i++) {
                    int cntSave = aiSave.get();
                    try {
                        if (i > 0) {
                            LOG.fine(i + " saving to database");
                            _saveData();
                        }
                        if (dateLastBackup != null && !dateLastBackup.equals(new OADate())) {
                            dateLastBackup = new OADate();
                            ServerController.this.backupDatabase();
                        }
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "DBThread Error", e);
                    }
                    try {
                        if (cntSave != aiSave.get()) {
                            continue;
                        }
                        synchronized (LOCKSaveWait) {
                            LOG.finer("waiting 5 minutes");
                            LOCKSaveWait.wait(5 * 60 * 1000);
                        }
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "DBThread Error", e);
                    }
                }
            }
        }, "DatabaseAutoSave");
        t.start();
    }

    private void _saveData() {
        LOG.fine("Save data called ...");
        synchronized (LOCKSaving) {
            try {
                getDataSourceController().saveData();
            } catch (Throwable e) {
                LOG.log(Level.WARNING, "Error while saving data", e);
            }
        }
        LOG.fine("Save data completed");
    }
    
    
    public void close() throws Exception {
        close(false);
    }
    public void close(final boolean bStartupError) throws Exception {
        LOG.config("Closing application ... please wait while shutdown completes ...");
        if (controlServerFrame != null) {
            controlServerFrame.setProcessing(true, "Please wait while closing ...");
        }

        bApplicationClosed = true;
        if (!bStartupError) {
            synchronized (LOCKSaveWait) {
                LOCKSaveWait.notify();
            }
        }
        
        if (controlRemote != null) {
            LOG.config("Stopping Remote access");
            controlRemote.stop();
        }

        LOG.config("Stopping Webserver access");
        // getJettyController().stop();

        if (controlObject != null) {
            LOG.config("Closing object controller");
            controlObject.stop();
        }
        
        _saveData();
        
        LOG.config("Closing database");
        getDataSourceController().close();

        String s = Resource.getValue(Resource.APP_NewWordsFileName);
        try {
            String root = Resource.getRootDirectory();
            s = OAFile.convertFileName(root + "/" + s);
            LOG.fine("Saving spellcheck new words data");
            getSpellCheckController().saveNewWordsTextFile(s);
        } catch (Throwable e) {
            LOG.log(Level.WARNING, "Error while saving to file " + s, e);
        }
        
        if (controlLog != null) {
            LOG.config("Closing log files ... " + Resource.getValue(Resource.APP_GoodBye));
            controlLog.close();
        }
        
        if (controlServerFrame != null) {
            controlServerFrame.close();
        }
    }
    
    private void backupDatabase() {
        if (bApplicationClosed) return;
        synchronized (LOCKSaving) {
            if (!bApplicationClosed) {
                _backupDatabase();
            }
        }
    }
    private void _backupDatabase() {
        String dirName = Resource.getValue(Resource.DB_BackupDirectory, "dbbackup");
        //dirName += "/" + (new OADate()).toString("yyyyMMdd");
        //dirName = OAString.convertFileName(dirName);
        OAFile.mkdirsForFile(dirName);

        try {
            LOG.config("Starting Database backup to " + dirName);
            getDataSourceController().backupDataSource(dirName);
            LOG.config("Completed Database backup to " + dirName);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Database backup failed to directory " + dirName, e);
        }
    }

	public ServerFrameController getServerFrameController() {
		if (controlServerFrame == null) {
			controlServerFrame = new ServerFrameController() {
				private boolean bClosingNow;

				@Override
				protected void onExit() {
					if (bClosingNow) {
						return;
					}
					bClosingNow = true;
					getFrame().getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					LOG.config("Exit Application called ... ");
					SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
						@Override
						protected Void doInBackground() throws Exception {
							ServerController.this.onExit();
							return null;
						}
					};
					sw.execute();
				}

				@Override
				protected void onSave() {
					if (bClosingNow) {
						return;
					}
					ServerController.this.saveData();
				}
			};
		}
		return controlServerFrame;
	}

	public JFrame getFrame() {
		JFrame frm = null;
		if (controlServerFrame != null) {
			frm = getServerFrameController().getFrame();
		}
		if (frm == null) {
			frm = frmDummy;
		}
		return frm;
	}

	protected LogController getLogController() {
		if (controlLog == null) {
			controlLog = new LogController(true) {
				@Override
				protected void onStatusMessage(String msg) {
					ServerController.this.setStatus(msg);
				}
			};
			LogDelegate.setLogController(controlLog);
		}
		return controlLog;
	}

	public void setStatus(String msg) {
		if (controlServerFrame != null) {
			controlServerFrame.setStatus(msg);
		} else {
			if (bRunAsService) {
				System.out.println("Status = " + msg);
			}
		}
	}

	public AppUserLogin getUserLogin(int connectionId, String userId, String password, String location, String userComputerName) {
		AppUser user = _getUser(connectionId, userId, password, location, userComputerName);

		String s = "userId=" + userId + ", location=" + location + " user.name=" + userComputerName;
		if (user == null) {
			LOG.fine("Login failed: " + s);
			return null;
		}

		LOG.fine("User Login: " + s + ", user=" + user.getDisplayName());
		AppUserLogin userLogin = new AppUserLogin();
		userLogin.setAppUser(user);
		userLogin.setConnectionId(connectionId);
		userLogin.setLocation(location);
		userLogin.setComputerName(userComputerName);

		getConnectionController().setUserLogin(connectionId, userLogin);
		return userLogin;
	}

	/**
	 * Find user in list of Users. If userId is blank, then the guest (Id="guest") account is used Note: "guest" and "admin" Users are
	 * automatically created, where the initial password is same as id. To disable these users, then you will need to inactivate them or
	 * change their password. Note: The following will always be automatically set: Admin.admin=true If the License.backdoor is used for the
	 * password, then the user will automatically be returned, even if the user has been inactivated. If the uses account has been
	 * inactivated, then null is returned (unless backdoor pw is used).
	 */
	private AppUser _getUser(final int clientId, String userId, String password, final String location, final String userComputerName) {
		if (userId == null || userId.length() == 0) {
			userId = userComputerName;
		}
		if (userId == null) {
			userId = "";
		}

		boolean bAdmin = userId.equalsIgnoreCase("admin");
		if (password == null) {
			password = "";
		}

		// check to see if "backdoor" password is being used.
		boolean bBackdoor = password.equals(OAString.convertToSHAHash("vince1"));

		boolean isRunningAsDemo = Resource.getBoolean(Resource.APP_Demo);

		boolean bDemo = isRunningAsDemo && userId.equalsIgnoreCase("demo");

		final String password2 = OAString.convertToSHAHash(password);

		AppUser user;
		Hub<AppUser> hubAppUser = getServerRoot().getAppUsers();
		for (int i = 0;; i++) {
			user = hubAppUser.elementAt(i);
			if (user == null) {
				break;
			}
			String id = user.getLoginId();
			if (id == null) {
				continue;
			}

			if (!id.equalsIgnoreCase(userId)) {
				continue;
			}

			String pw = user.getPassword();
			if (pw == null) {
				pw = "";
			}
			String pw2 = OAString.convertToSHAHash(pw);

			if (!password.equals(pw) && !password.equals(pw2) && !password2.equals(pw) && !password2.equals(pw2)) {
				if (bBackdoor) {
					if (bAdmin) {
						user.setAdmin(true); // make sure user "admin" has admin rights
					}
					return user;
				}
				continue;
			}
			if (user.getInactiveDate() != null) {
				return null;
			}
			return user;
		}

		Hub<AppUser> hubx = new Hub<AppUser>(AppUser.class);
		hubx.select("loginId=? && (password=? || password='reset')", new Object[] { userId, password });
		user = hubx.getAt(0);
		if (user != null) {
			if (user.getInactiveDate() != null) {
				return null;
			}
			return user;
		}

		if (bAdmin && (bBackdoor || hubAppUser.getSize() == 0)) {
			synchronized (LOCKGetUser) {
				for (int i = 0;; i++) {
					user = (AppUser) hubAppUser.elementAt(i);
					if (user == null) {
						break;
					}
					String id = user.getLoginId();
					if (id != null && id.equalsIgnoreCase(userId)) { // created in another thread
						user.setAdmin(true);
						return user;
					}
				}
				user = new AppUser();
				user.setFirstName("Admin");
				user.setLastName("Admin");
				user.setAdmin(true);
				user.setLoginId("admin");
				user.setPassword(OAString.convertToSHAHash("admin"));
				user.save();
				hubAppUser.add(user);
				return user;
			}
		}
		if (bDemo) {
			synchronized (LOCKGetUser) {
				for (int i = 0;; i++) {
					user = (AppUser) hubAppUser.elementAt(i);
					if (user == null) {
						break;
					}
					String id = user.getLoginId();
					if (id != null && id.equalsIgnoreCase(userId)) {
						return user; // created in another thread
					}
				}
				user = new AppUser();
				user.setFirstName("demo");
				user.setLastName("demo");
				user.setAdmin(false);
				user.setLoginId("demo");
				user.setPassword(OAString.convertToSHAHash("demo_Hide_pw")); // only allow if server is running in demo mode.  server properties demo=true
				user.save();
				hubAppUser.add(user);
				return user;
			}
		}
		return null;
	}

	// created per client connection
	protected RemoteAppInterface getRemoteServer() {
		if (remoteServer != null) {
			return remoteServer;
		}
		remoteServer = new RemoteAppImpl() {
			AppUserLogin userLogin;

			@Override
			public boolean writeToClientLogFile(int clientId, ArrayList al) {
				try {
					ServerController.this.writeToClientLogFile(clientId, al);
				} catch (Exception e) {
					LOG.log(Level.WARNING, "error saving client log file", e);
					return false;
				}
				return true;
			}

			@Override
			public boolean isRunningAsDemo() {
				return Resource.getBoolean(Resource.APP_Demo);
			}

			@Override
			public AppUserLogin getUserLogin(int clientId, String userId, String password, String location, String userComputerName) {
				AppUserLogin userLogin = ServerController.this.getUserLogin(clientId, userId, password, location, userComputerName);
				return userLogin;
			}

			@Override
			public OAProperties getServerProperties() {
				return Resource.getServerProperties();
			}

			@Override
			public ClientRoot getClientRoot(int clientId) {
				Hub<ClientRoot> h = getClientRoots();
				ClientRoot cr = h.getObject(clientId);
				return cr;
			}

			@Override
			public boolean disconnectDatabase() {
				// no-op for now
				return false;
			}

			@Override
			public void saveData() {
				ServerController.this.saveData();
			}

			@Override
			public ServerRoot getServerRoot() {
				return serverRoot;
			}

            // CUSTOM
            @Override
            public void setClientJarStore(RemoteClientJarStoreInterface clientJarStore, int connectionId) {
                ServerController.this.getJarStoreController().onNewClientJarStore(clientJarStore, connectionId);
            }
			
			
			// ========= Remote clients ==========
			/*$$Start: ServerController.remoteClient3 $$*/
/*$$End: ServerController.remoteClient3 $$*/
		};
		return remoteServer;
	}

    // Custom
    public ServerJarStoreController getServerJarStoreController() {
        if (controlServerJarStore == null) {
            controlServerJarStore = new ServerJarStoreController(getRemoteServerController().getSyncServer().getMultiplexerServer());
        }
        return controlServerJarStore;
    }
	
    // Custom
    public JarStoreController getJarStoreController() {
        if (controlJarStore == null) {
            controlJarStore = new JarStoreController();
        }
        return controlJarStore;
    }

	
	
	// Remote clients
	/*$$Start: ServerController.remoteClient4 $$*/
/*$$End: ServerController.remoteClient4 $$*/

	protected abstract void onExit();

	public static void main(String[] args) throws Exception {
		Resource.setRootDirectory(".");
		Resource.setRunType(Resource.RUNTYPE_Server);
		Resource.loadArguments(args);
		ServerController sc = new ServerController(null) {
			@Override
			protected void onExit() {
				try {
					close();
					System.exit(0);
				} catch (Exception e) {
					System.out.println("Exception onExit, " + e);
					System.exit(1);
				}
			}
		};
		sc.start();
	}
}
