// Copied from OATemplate project by OABuilder 03/12/24 01:50 PM
package com.viaoa.appstore.control.single;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.viaoa.appstore.control.HelpController;
import com.viaoa.appstore.control.LogController;
import com.viaoa.appstore.control.ReleaseController;
import com.viaoa.appstore.control.client.ClientFrameController;
import com.viaoa.appstore.control.client.ClientProcessController;
import com.viaoa.appstore.control.server.DataSourceController;
import com.viaoa.appstore.control.server.ObjectController;
import com.viaoa.appstore.control.server.ServerSpellCheckController;
import com.viaoa.appstore.delegate.CronDelegate;
import com.viaoa.appstore.delegate.ExecutorServiceDelegate;
import com.viaoa.appstore.delegate.LogDelegate;
import com.viaoa.appstore.delegate.ModelDelegate;
import com.viaoa.appstore.delegate.ScheduledExecutorServiceDelegate;
import com.viaoa.appstore.model.oa.AppServer;
import com.viaoa.appstore.model.oa.AppUser;
import com.viaoa.appstore.model.oa.AppUserLogin;
import com.viaoa.appstore.model.oa.ServerApplication;
import com.viaoa.appstore.model.oa.cs.ClientRoot;
import com.viaoa.appstore.model.oa.cs.ServerRoot;
import com.viaoa.appstore.resource.Resource;
import com.viaoa.context.OAContext;
import com.viaoa.hub.Hub;
import com.viaoa.jfc.OAJfcUtil;
import com.viaoa.jfc.text.spellcheck.SpellChecker;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.process.OACronProcessor;
import com.viaoa.util.*;

/**
 * Main controller for starting in Single User mode.
 */
public abstract class SingleController {

    private static Logger LOG = Logger.getLogger(SingleController.class.getName());

    // Controllers
    private LogController controlLog;
    private DataSourceController controlDataSource;
    private ObjectController controlObject;
    private HelpController controlHelp;
    private volatile ClientFrameController controlFrame;
    protected ServerSpellCheckController controlServerSpellCheck;

    // Custom
    private ReleaseController controlRelease;
    
    private JFrame frmDummy;

    private ServerRoot serverRoot;
    private ClientRoot clientRoot;
    private Hub<ClientRoot> hubClientRoot;

    private final Object LOCKSaving = new Object(); 
    private final Object LOCKSaveWait = new Object();
    private final AtomicInteger aiSave = new AtomicInteger();
    private volatile boolean bApplicationClosed;
    private AppUserLogin appUserLogin;

    private SpellChecker spellChecker;
    
    // Custom
    private ClientProcessController controlClientProcess;
    

    public SingleController(JFrame frmDummy) {
        this.frmDummy = frmDummy;
        Resource.setLocale(null);

        String s = Resource.getValue(Resource.APP_SingleApplicationName);
        Resource.setRunTimeName(s);
    }

    public ServerRoot getServerRoot() {
        if (serverRoot == null) {
            serverRoot = new ServerRoot();
        }
        return serverRoot;
    }

    public Hub<ClientRoot> getClientRootHub() {
        if (hubClientRoot == null) {
            hubClientRoot = new Hub<>(ClientRoot.class);
            hubClientRoot.add(getClientRoot());
        }
        return hubClientRoot;
    }

    public ClientRoot getClientRoot() {
        if (clientRoot == null) {
            clientRoot = new ClientRoot();
            clientRoot.setId(1);
            clientRoot.save();
        }
        return clientRoot;
    }

    public boolean start() throws Exception {
        String packageName = "com.fose.model.oa";
        String[] cnames = OAReflect.getClasses(packageName);
        for (String fn : cnames) {
            Class c = Class.forName(packageName + "." + fn);
            OAObjectInfoDelegate.getObjectInfo(c);
        }

        boolean b = _start();

        long ms = System.currentTimeMillis();
        getFrameController().getFrame();
        long ms2 = System.currentTimeMillis();
        LOG.fine("frame load time " + (ms2 - ms) + "ms");
        getFrameController().getFrame().setVisible(true);
        setProcessing(false);

        int x = Resource.getInt(Resource.CLIENT_CheckAWTThreadMinutes);
        if (x > 0) {
            runCheckAWTThread(x);
        }

        LOG.config(Resource.getValue(Resource.APP_Welcome));
        
        
        // Custom
        if (Resource.getBoolean(Resource.APP_CheckForNewRelease)) {
            checkForNewRelease();
        }

        // Custom
        LOG.fine("starting process controller, to manage running apps");
        getClientProcessController();
        
        return b;
    }
    
    // Custom
    public void checkForNewRelease() {
        SwingWorker<Void, String> swTask = new SwingWorker<Void, String>() {
            OAProperties props;

            @Override
            protected Void doInBackground() throws Exception {
                props = getReleaseController().getGitProperties();
                if (!getReleaseController().hasUpdate()) props = null;
                return null;
            }

            @Override
            protected void done() {
                if (props == null) {
                    return;
                }
                final String currentRelease = Resource.getValue(Resource.APP_Release);
                final String gitRelease = props.getProperty("Release");
                LOG.fine(String.format("current release=%s, OAAppStore release=%s", currentRelease, gitRelease));

                final String gitVersion = props.getProperty("Version");
                int x = JOptionPane.showConfirmDialog(  getFrame(), "New version " + gitVersion + " (release: " + gitRelease + ") is available.\nIs it ok to download?",
                                                        Resource.getRunTimeName(), JOptionPane.YES_NO_OPTION,
                                                        JOptionPane.QUESTION_MESSAGE);
                if (x == JOptionPane.YES_OPTION) {
                    _getNewRelease(gitVersion, gitRelease);
                }
            }
        };
        swTask.execute();
    }
    
    // Custom
    protected void _getNewRelease(final String gitVersion, final String gitRelease) {
        setProcessing(true, "Downloading new version " + gitVersion + " (release: " + gitRelease + "), please wait ...");
        SwingWorker<Void, String> swTask = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                getReleaseController().getUpdate();
                getReleaseController().updateConfig();
                return null;
            }
    
            @Override
            protected void done() {
                setProcessing(false);
    
                JOptionPane.showMessageDialog(  getFrame(), "New version " + gitVersion + " (" + gitRelease + ") is loaded, please restart.",
                        Resource.getRunTimeName(), JOptionPane.INFORMATION_MESSAGE);
            }
            
        };
        swTask.execute();
    }
    
    // Custom
    public ReleaseController getReleaseController() {
        if (controlRelease == null) {
            controlRelease = new ReleaseController();
        }
        return controlRelease;
    }

    private boolean _start() throws Exception {
        // Logging
        getLogController();
        LOG.config("Logging started");
        
        // needs to be set before datasource controller
        LOG.fine("create and start cron process");
        OACronProcessor cp = new OACronProcessor();
        CronDelegate.setCronProcessor(cp);
        CronDelegate.getCronProcessor().start();

        // DataSource
        boolean bCheckForDatasourceCorruption = Resource.getBoolean(Resource.DB_CheckForCorruption, true);

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
        ModelDelegate.initialize(getServerRoot(), getClientRoot());
        
        // must be after ModelDelegate is initialized
        LOG.config("Starting Object Controller");
        getObjectController().start();

        AppServer appServer = ModelDelegate.getAppServer();
        appServer.setStarted(null);
        appServer.setCreated(new OADateTime());
        appServer.setRelease("" + Resource.getInt(Resource.APP_Release));

        appUserLogin = new AppUserLogin();
        appUserLogin.setAppUser(user);
        appUserLogin.setConnectionId(0);
        appUserLogin.setLocation("App Server");
        appUserLogin.setComputerName(System.getProperty("user.name"));

        appUserLogin.setHostName(Resource.getValue(Resource.APP_HostName));
        appUserLogin.setIpAddress(Resource.getValue(Resource.APP_HostIPAddress));

        appServer.setAppUserLogin(appUserLogin);

        ModelDelegate.setLocalAppUserLogin(appUserLogin);

        LOG.fine("SpellCheck Controller");
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                String root = Resource.getRootDirectory() + "/";
                root = OAFile.convertFileName(root);
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
                            SingleController.this.close();
                        }
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "Exception in shutDownHook", e);
                    }
                }
            });
        }

        appServer.setStarted(new OADateTime());
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
                    LOG.warning("AWTThread did not respond to invokeLater, dumping stack traces to log.");
                    controlLog.dumpStackTrace();
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

    protected LogController getLogController() {
        if (controlLog != null) {
            return controlLog;
        }
        controlLog = new LogController(false) {
            @Override
            protected void onStatusMessage(String msg) {
                SingleController.this.setStatus(msg);
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

    public DataSourceController getDataSourceController() {
        if (controlDataSource == null) {
            try {
                controlDataSource = new DataSourceController(getServerRoot(), getClientRootHub());
            } catch (Exception e) {
                LOG.log(Level.WARNING, "DataSource Error", e);
                throw new RuntimeException("Exception getting datasource controller", e);
            }
        }
        return controlDataSource;
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

    protected synchronized HelpController getHelpController() {
        if (controlHelp == null) {
            controlHelp = new HelpController();
        }
        return controlHelp;
    }

    public JFrame getFrame() {
        JFrame frm = null;
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
                SingleController.this.callExit();
            }

            @Override
            public void onSave() {
                SingleController.this.saveData();
            }

            @Override
            protected void setLookAndFeel(String laf) {
                SingleController.this.setLookAndFeel(laf);
            }
        };
        return fc;
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
                Resource.setValue(Resource.TYPE_Single, Resource.APP_LookAndFeel, laf);
                Resource.save();
            }
            LOG.fine("L&F set to " + laf);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error setting L&F to " + laf, e);
        } finally {
        }
    }

    private void callExit() {
        if (SwingUtilities.isEventDispatchThread()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SingleController.this.onExit();
                } 
            }).start();
        }
        else {
            onExit();
        }
    }

    protected abstract void onExit();

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
                            _saveData();
                        }
                        if (dateLastBackup != null && !dateLastBackup.equals(new OADate())) {
                            dateLastBackup = new OADate();
                            SingleController.this.backupDatabase();
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
                            LOCKSaveWait.wait(1000 * 60 * 5);
                        }
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "DBThread Error", e);
                    }
                }
                LOG.fine("exiting");
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

    
    // Custom
    public ClientProcessController getClientProcessController() {
        if (controlClientProcess == null) {
            controlClientProcess = new ClientProcessController() {
                
/*qqqqqqqqqqqqqqqqq                
                @Override
                protected void onLoadApplicationVersion(Application app) throws Exception {
                    if (app == null) return;
                    LOG.fine("checking and loading application version from server, application="+app.getFullName());
                    int release = SingleController.this.getClientJarStoreController().getRelease(app);
                    
                    ApplicationVersion appVersion = app.getApplicationVersion();
                    if (appVersion.getRelease() != release) {
                        SingleController.this.getClientJarStoreController().getJarForServer(app);
                    }
                    else {
                        SingleController.this.getClientJarStoreController().getJarFromServer(appVersion);
                    }
                }
qqqqqqqqqqqq*/
                
                @Override
                protected void onLoadApplicationVersion(ServerApplication app) throws Exception {
//qqqqqqqqqqqqqqqqqqqqqqqqqq                    
                }
                
            };
        }
        return controlClientProcess;
    }
    
    
    public void close() throws Exception {
        close(false);
    }
    
    public void close(final boolean bStartupError) throws Exception {
        LOG.config("Closing application ... please wait while shutdown completes ...");

        bApplicationClosed = true;
        synchronized (LOCKSaveWait) {
            LOCKSaveWait.notifyAll();
        }

        if (controlFrame != null) {
            controlFrame.setProcessing(true, "Please wait while closing ...");
        }

        if (controlObject != null) {
            LOG.config("Closing object controller");
            controlObject.stop();
        }
        
        if (!bStartupError) {
            _saveData();
        }
        LOG.config("Closing database");
        getDataSourceController().close();

        if (!bStartupError) {
            String s = Resource.getValue(Resource.APP_NewWordsFileName);
            if (OAStr.isNotEmpty(s)) {
                String root = Resource.getRootDirectory();
                s = OAFile.convertFileName(root + "/" + s);
                LOG.fine("Saving spellcheck new words to file " + s);
                getSpellCheckController().saveNewWordsTextFile(s);
            }
        }
        
        if (controlLog != null) {
            LOG.config("Closing log files ... " + Resource.getValue(Resource.APP_GoodBye));
            controlLog.close();
        }
        
        if (controlFrame != null) {
            controlFrame.close(); // saves window size/location to ini file and frm.setVisible(false)
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

    public static void main(String[] args) {
        // Resource.setRootDir(".");
        Resource.loadArguments(args);
        Resource.setRunType(Resource.RUNTYPE_Single);
        SingleController sc = new SingleController(null) { // StartupController.RUNTYPE_Single
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
        try {
            sc.start();
        } catch (Exception e) {
            System.out.println("Exception: " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }
    
}
