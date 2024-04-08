// Copied from OATemplate project by OABuilder 03/12/24 01:50 PM
package com.viaoa.appstore.control.server;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.*;

import com.viaoa.concurrent.OAExecutorService;
import com.viaoa.datasource.*;
import com.viaoa.datasource.jdbc.OADataSourceJDBC;
import com.viaoa.datasource.objectcache.OADataSourceObjectCache;
import com.viaoa.hub.*;
import com.viaoa.json.OAJson;
import com.viaoa.object.*;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.sync.OASyncServer;
import com.viaoa.transaction.OATransaction;
import com.viaoa.util.*;
import com.viaoa.xml.*;
import com.viaoa.process.*;

import com.viaoa.appstore.datasource.DataSource;
import com.viaoa.appstore.model.oa.*;
import com.viaoa.appstore.model.oa.propertypath.*;
import com.viaoa.appstore.model.oa.filter.*;
import com.viaoa.appstore.model.oa.cs.*;
import com.viaoa.appstore.delegate.*;
import com.viaoa.appstore.resource.Resource;

/**
 * Used to manage object persistence, includes serialization and OADataSource support.
 **/
public class DataSourceController {
	private static Logger LOG = OALogger.getLogger(DataSourceController.class);

    private final boolean bIsUsingDatabase;
    private final DataSource dataSource;
    private final OADataSourceObjectCache dsObjectCache;

    private final ServerRoot serverRoot;
    private final Hub<ClientRoot> hubClientRoot;

    private OAExecutorService executorService;
    private final AtomicInteger aiExecutor = new AtomicInteger();
    private CopyOnWriteArrayList<String> alExecutorService = new CopyOnWriteArrayList<String>();
    private CopyOnWriteArrayList<String> alSelectError = new CopyOnWriteArrayList<String>();

    private OADate dateLastSerialize;
    private OATime timeLastSerialize;
    private OADate dateLastJson;
    private OATime timeLastJson;
    private OADate dateLastXml;
    private OATime timeLastXml;

    public DataSourceController(ServerRoot serverRoot, Hub<ClientRoot> hubClientRoot) throws Exception {
        this.serverRoot = serverRoot;
        this.hubClientRoot = hubClientRoot;

        bIsUsingDatabase = Resource.getBoolean(Resource.DB_IsUsingDatabase, false);

        if (bIsUsingDatabase) {
            dataSource = new DataSource();
            dataSource.open();

            if (Resource.getBoolean(Resource.DB_IgnoreWrites, false)) {
                String msg = "Note: using Database, but NOT saving ... all writes are ignored (insert/update/delete)";
                LOG.warning(msg);
                for (int i = 0; i < 10; i++) {
                    System.out.println(msg);
                }
                dataSource.getOADataSource().setIgnoreWrites(true); // readonly
            }
            dataSource.getOADataSource().setAssignIdOnCreate(false);
        }
        else {
            String msg = "Note: Database is NOT being used, db.isUsingDatabase=false";
            dataSource = null;

            // make sure that it is saving to file
            if (!Resource.getBoolean(Resource.INI_SaveDataToFile, false)) {
                if (!Resource.getBoolean(Resource.INI_SaveDataToJsonFile, false)) {
                    if (!Resource.getBoolean(Resource.INI_SaveDataToXmlFile, false)) {
                        msg += ", WARNING: NOT saving - not using database, file(s) data.* (.bin, .json, .xml) **** WARNING ****";
                    }
                }
            }
            LOG.warning(msg);
            for (int i = 0; i < 10; i++) {
                System.out.println(msg);
            }
        }

        // use OADataSourceObjectCache for model classes that are not already selected by OADS.
        final Set<Class> hsClass = new HashSet<>();
        String packageName = AppServer.class.getPackage().getName(); // oa model classes
        for (String fn : OAReflect.getClasses(packageName)) {
            Class c = Class.forName(packageName + "." + fn);
            if (dataSource == null || !dataSource.getOADataSource().isClassSupported(c)) {
                hsClass.add(c);
            }
        }
        dsObjectCache = new OADataSourceObjectCache() {
            @Override
            public boolean isClassSupported(Class clazz, OAFilter filter) {
                return hsClass.contains(clazz);
            }
        };
        dsObjectCache.setAssignIdOnCreate(true);
    }

	public boolean isUsingDatabase() {
		return bIsUsingDatabase;
	}

	public OADataSourceJDBC getOADataSourceJDBC() {
		if (this.dataSource == null) {
			return null;
		}
		return this.dataSource.getOADataSource();
	}

    public OADataSourceObjectCache getObjectCacheDataSource() {
        return dsObjectCache;
    }
	
	public boolean loadServerRoot() throws Exception {
        // if (this.dataSource == null) return false; // might need to use object cache filters
        LOG.log(Level.CONFIG, "selecting startup data");

        LOG.info("starting to select data");
        executorService = new OAExecutorService("DataSourceController.startup");

        if (!isUsingDatabase()) {
            boolean b = readFromSerializeFile();
            if (!b) {
                if (!Resource.getBoolean(Resource.INI_SaveDataToJsonFile, false) || !readFromJsonFile()) {
                    if (!Resource.getBoolean(Resource.INI_SaveDataToXmlFile, false) || !readFromXmlFile()) {
                        LOG.log(Level.WARNING, "No options available to load data (database, serialized, JSON, XML)");
                    }
                }
            }
        }

		/*$$Start: DatasourceController.loadServerRoot $$*/
        aiExecutor.incrementAndGet();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                String msg = "serverRoot.getApplicationTypes()";
                try {
                    alExecutorService.add(msg);
                    if (isUsingDatabase()) {
                        select(serverRoot.getApplicationTypes(), "", null);
                    }
                    else {
                        OAObjectCacheDelegate.setSelectAllHub(serverRoot.getApplicationTypes());
                    }
                }
                catch (Exception e) {
                    String s = "DataSourceController error selecting ApplicationTypes, exception="+e;
                    alSelectError.add(s);
                    LOG.log(Level.WARNING, s, e);
                }
                finally {
                    aiExecutor.decrementAndGet();
                    alExecutorService.remove(msg);
                }
            }
        });
        aiExecutor.incrementAndGet();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                String msg = "serverRoot.getCreateOneAppRuntimeHub()";
                try {
                    alExecutorService.add(msg);
                    if (isUsingDatabase()) {
                        select(serverRoot.getCreateOneAppRuntimeHub(), "", null);
                    }
                    if (serverRoot.getCreateOneAppRuntimeHub().getAt(0) == null) {
                        // createOne=true
                        serverRoot.getCreateOneAppRuntimeHub().add(new AppRuntime());
                    }
                    else {
                        serverRoot.getCreateOneAppRuntimeHub().cancelSelect();
                    }
                }
                catch (Exception e) {
                    String s = "DataSourceController error selecting CreateOneAppRuntimeHub, exception="+e;
                    alSelectError.add(s);
                    LOG.log(Level.WARNING, s, e);
                }
                finally {
                    aiExecutor.decrementAndGet();
                    alExecutorService.remove(msg);
                }
            }
        });
        aiExecutor.incrementAndGet();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                String msg = "serverRoot.getCreateOneAppServerHub()";
                try {
                    alExecutorService.add(msg);
                    if (isUsingDatabase()) {
                        select(serverRoot.getCreateOneAppServerHub(), "", null);
                    }
                    if (serverRoot.getCreateOneAppServerHub().getAt(0) == null) {
                        // createOne=true
                        serverRoot.getCreateOneAppServerHub().add(new AppServer());
                    }
                    else {
                        serverRoot.getCreateOneAppServerHub().cancelSelect();
                    }
                }
                catch (Exception e) {
                    String s = "DataSourceController error selecting CreateOneAppServerHub, exception="+e;
                    alSelectError.add(s);
                    LOG.log(Level.WARNING, s, e);
                }
                finally {
                    aiExecutor.decrementAndGet();
                    alExecutorService.remove(msg);
                }
            }
        });
        aiExecutor.incrementAndGet();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                String msg = "serverRoot.getAppUsers()";
                try {
                    alExecutorService.add(msg);
                    if (isUsingDatabase()) {
                        select(serverRoot.getAppUsers(), "", null);
                    }
                    else {
                        OAObjectCacheDelegate.setSelectAllHub(serverRoot.getAppUsers());
                    }
                }
                catch (Exception e) {
                    String s = "DataSourceController error selecting AppUsers, exception="+e;
                    alSelectError.add(s);
                    LOG.log(Level.WARNING, s, e);
                }
                finally {
                    aiExecutor.decrementAndGet();
                    alExecutorService.remove(msg);
                }
            }
        });
        aiExecutor.incrementAndGet();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                String msg = "serverRoot.getEnvironments()";
                try {
                    alExecutorService.add(msg);
                    if (isUsingDatabase()) {
                        select(serverRoot.getEnvironments(), "", EnvironmentPP.seq());
                    }
                    else {
                        OAObjectCacheDelegate.setSelectAllHub(serverRoot.getEnvironments());
                        serverRoot.getEnvironments().cancelSort();
                        serverRoot.getEnvironments().sort(EnvironmentPP.seq());
                    }
                    serverRoot.getEnvironments().setAutoSequence(EnvironmentPP.seq());
                }
                catch (Exception e) {
                    String s = "DataSourceController error selecting Environments, exception="+e;
                    alSelectError.add(s);
                    LOG.log(Level.WARNING, s, e);
                }
                finally {
                    aiExecutor.decrementAndGet();
                    alExecutorService.remove(msg);
                }
            }
        });
        aiExecutor.incrementAndGet();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                String msg = "serverRoot.getLastDayApplicationVersions()";
                try {
                    alExecutorService.add(msg);
                    final ApplicationVersionLastDayFilter filter = new ApplicationVersionLastDayFilter(serverRoot.getLastDayApplicationVersions()) {
                        @Override
                        public void reselect() {
                            Hub<ApplicationVersion> hub = new Hub<ApplicationVersion>(ApplicationVersion.class);
                            Object[] params = new Object[] { (new OADateTime()).addDays(-1) };
                            String query = "started  >= ?";
                            String orderBy = "";
                            hub.select(query, params, orderBy);
                            hub.loadAllData();
                        }
                    };
                    filter.getObjectCacheFilter().setServerSideOnly(true);
                    // strMins, strHours, strDayOfMonth, strMonth, strDayOfWeek
                    OACron cron = new OACron("5", "0", "*","*", "*") {
                        @Override
                        public void process(boolean bManuallyCalled) {
                            filter.refresh();
                            filter.reselect();
                        }
                    };
                    cron.setName("DataSourceController.ApplicationVersionLastDayFilter.refresh");
                    CronDelegate.add(cron);
                }
                catch (Exception e) {
                    String s = "DataSourceController error for filter ApplicationVersionLastDayFilter, exception="+e;
                    alSelectError.add(s);
                    LOG.log(Level.WARNING, s, e);
                }
                finally {
                    aiExecutor.decrementAndGet();
                    alExecutorService.remove(msg);
                }
            }
        });

        aiExecutor.incrementAndGet();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    final AppUserLoginConnectedFilter filter = new AppUserLoginConnectedFilter(serverRoot.getConnectedAppUserLogins()) {
                        @Override
                        public void reselect() {
                            Hub<AppUserLogin> hub = new Hub<AppUserLogin>(AppUserLogin.class);
                            hub.select("disconnected == null");
                            hub.loadAllData();
                        }
                    };
                    filter.getObjectCacheFilter().setServerSideOnly(true);
                }
                catch (Exception e) {
                    String s = "DataSourceController error for filter AppUserLoginConnectedFilter, exception="+e;
                    alSelectError.add(s);
                    LOG.log(Level.WARNING, s, e);
                }
                finally {
                    aiExecutor.decrementAndGet();
                }
            }
        });
/*$$End: DatasourceController.loadServerRoot $$*/

        int max = Resource.getInt(Resource.DB_MaxWaitForSelects, 300);
        for (int i = 0; i < max; i++) {
            if (aiExecutor.get() == 0) {
                break;
            }
            Thread.sleep(1000);
            LOG.fine(i + ") waiting for data to be selected, remaining=" + aiExecutor.get());
            if (i >= 15) {
                String s = "Open queries";
                for (String s2 : alExecutorService) {
                    s += "\n" + s2;
                }
                LOG.fine(s);
            }
        }

        for (String s : alSelectError) {
            LOG.warning("Error during select: " + s);
        }

        // dont need to have these Hubs as selectAll in objectCache
        OAObjectCacheDelegate.removeSelectAllHub(serverRoot.getAppUsers());

        LOG.info("completed selecting startup data");
        executorService.close();

        return (alSelectError.size() == 0);
	}

    private void select(final Hub hub) {
        select(hub, null, null);
    }

    private void select(final Hub hub, final String query, final String orderBy) {
        if (hub == null) {
            return;
        }
        aiExecutor.incrementAndGet();
        String s = "selecting " + hub.getObjectClass().getSimpleName() + ", query=" + query + ", orderBy=" + orderBy;
        LOG.fine(s);
        try {
            alExecutorService.add(s);
            hub.select(query, orderBy);
            hub.loadAllData();
            alExecutorService.remove(s);
        }
        finally {
            aiExecutor.decrementAndGet();
        }
    }
	
    public void saveData() throws Exception {
        OATransaction trans = new OATransaction();
        trans.setUseBatch(true);
        trans.start();

        long ms = System.currentTimeMillis();
        try {
            OACascade cascade = new OACascade();
            OAObjectSaveDelegate.save(serverRoot, OAObject.CASCADE_ALL_LINKS, cascade);

            HubSaveDelegate.saveAll(hubClientRoot, OAObject.CASCADE_ALL_LINKS, cascade);

            OASyncServer ss = OASyncDelegate.getSyncServer();
            if (ss != null) {
                OASyncDelegate.getSyncServer().saveCache(cascade, OAObject.CASCADE_ALL_LINKS);
                OASyncDelegate.getSyncServer().performDGC();
            }
        }
        finally {
            trans.commit();
        }
        long ms2 = System.currentTimeMillis();
        LOG.fine(String.format("all changes saved to DataSource in %,d ms", (ms2 - ms)));

        try {
            if (Resource.getBoolean(Resource.INI_SaveDataToFile, false)) {
                LOG.fine("Saving data to serialized file");
                writeSerializeFile();
            }
        }
        catch (Throwable e) {
            LOG.log(Level.WARNING, "Error while saving data to json file", e);
        }

        try {
            if (Resource.getBoolean(Resource.INI_SaveDataToJsonFile, false)) {
                LOG.fine("Saving data as JSON object file");
                writeToJsonFile();
            }
        }
        catch (Throwable e) {
            LOG.log(Level.WARNING, "Error while saving data to json file", e);
        }

        try {
            if (Resource.getBoolean(Resource.INI_SaveDataToXmlFile, false)) {
                LOG.fine("Saving data as XML object file");
                writeToXmlFile();
            }
        }
        catch (Throwable e) {
            LOG.log(Level.WARNING, "Error while saving data to json file", e);
        }
    }

    protected boolean readFromSerializeFile() throws Exception {
        if (getObjectCacheDataSource() == null) return false;
        final String dirName = Resource.getDataDirectory();
        LOG.log(Level.CONFIG, "Reading from file " + dirName + "/data.bin");
        File file = new File(OAFile.convertFileName(dirName + "/data.bin"));
        if (!file.exists()) {
            LOG.log(Level.CONFIG, "file " + dirName + "/data.bin does not exist");
            return false;
        }

        getObjectCacheDataSource().loadFromStorageFile(file);
        LOG.log(Level.CONFIG, "reading from serialized file data.bin completed");
        return true;
    }
    
    protected void writeSerializeFile() throws Exception {
        final String dirName = Resource.getDataDirectory();
        File f1 = new File(dirName);
        if (f1.exists()) {
            if (!f1.isDirectory()) {
                File f2;
                for (int i = 0;; i++) {
                    f2 = new File(dirName + "_" + i);
                    if (!f2.exists()) {
                        break;
                    }
                }
                f1.renameTo(f2);
            }
        }
        else {
            f1.mkdir();
        }

        LOG.fine("Saving data to file temp.bin, will rename when done");

        File fileTemp = new File(OAString.convertFileName(dirName + "/temp.bin"));

        getObjectCacheDataSource().saveToStorageFile(fileTemp, serverRoot);

        File dataFile = new File(OAString.convertFileName(dirName + "/data.bin"));

        if (dataFile.exists()) {
            String backupName = null;

            // save to daily/hourly/5minute
            OADate d = new OADate();
            if (dateLastSerialize == null || !d.equals(dateLastSerialize)) {
                dateLastSerialize = d;
                backupName = "data_daily_" + (d.toString("yyMMdd")) + ".bin";
                File f = new File(OAFile.convertFileName(dirName + "/" + backupName));
                if (f.exists()) {
                    backupName = null;
                }
            }

            if (backupName == null || backupName.length() == 0) {
                OATime t = new OATime();
                if (timeLastSerialize == null || t.getHour() != timeLastSerialize.getHour()) {
                    timeLastSerialize = t;
                    backupName = "data_hourly_" + (t.toString("HH")) + ".bin";
                }
                else {
                    // save to nearest 5 minutes, otherwise it could have 60 files over time.
                    int m = t.getMinute();
                    m = m == 0 ? 0 : ((m / 5) * 5);
                    backupName = "data_min_" + OAString.format(m, "00") + ".bin";
                }
            }

            backupName = OAFile.convertFileName(dirName + "/" + backupName);
            File backupFile = new File(backupName);
            if (backupFile.exists()) {
                backupFile.delete();
            }
            dataFile.renameTo(backupFile);
            dataFile = new File(OAString.convertFileName(dirName + "/data.bin"));
        }

        fileTemp.renameTo(dataFile);
        LOG.fine("Saved data to serialized file " + dataFile);
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close(); // this will call JavaDB shutdown, and remove datasource from list of available datasources
        }
    }

	public boolean verifyDataSource() throws Exception {
		LOG.config("Verifying database structure");
		if (dataSource == null) {
			return true;
		}
		dataSource.getOADataSource().verify();
		return true;
	}

	public String getInfo() {
		Vector vec = new Vector();

		OADataSourceJDBC oads = getOADataSourceJDBC();
		vec.addElement("DataSource ============================");
		oads.getInfo(vec);

		StringBuffer sb = new StringBuffer(4096);
		int x = vec.size();
		for (int i = 0; i < x; i++) {
			String s = (String) vec.elementAt(i);
			sb.append(s + "\r\n");
		}
		return new String(sb);
	}

	public boolean isDataSourceReady() {
		if (dataSource == null) {
			return true;
		}
		return dataSource.getOADataSource().isAvailable();
	}

	public void disconnect() {
		LOG.fine("called");
		if (dataSource != null) {
			dataSource.close();
		}
	}

	public void reconnect() {
		LOG.fine("called");
		if (dataSource != null) {
			dataSource.getOADataSource().reopen(0);
		}
	}

	public void updateDataSource() {
		// todo
	}

	private int cacheCnt;

	public void insertAllObjectsToDatabase() throws Exception {
		LOG.config("Loading database");

		final HashMap<Class, Integer> hash = new HashMap<Class, Integer>();
		OAObjectCacheDelegate.callback(new OACallback() {
			@Override
			public boolean updateObject(Object obj) {
				cacheCnt++;
				OAObjectDelegate.setNew((OAObject) obj, true);
				Object objx = ((OAObject) obj).getProperty("id");
				if (objx != null && objx instanceof Number) {
					int id = ((Number) objx).intValue();
					Integer n = hash.get(objx.getClass());
					if (n == null || id > n) {
						hash.put(obj.getClass(), id);
					}
				}
				return true;
			}
		});

		OADataSourceJDBC oads = getOADataSourceJDBC();
		for (Entry<Class, Integer> entry : hash.entrySet()) {
			oads.setNextNumber(entry.getKey(), entry.getValue() + 1);
		}
		System.out.println(cacheCnt + " objects updated from Cache");

		OATransaction tran = new OATransaction(java.sql.Connection.TRANSACTION_READ_UNCOMMITTED);
		try {
			tran.start();
			serverRoot.save(OAObject.CASCADE_ALL_LINKS);
			tran.commit();
		} catch (Exception e) {
			System.out.println("Exception: " + e);
			e.printStackTrace();
			tran.rollback();
			throw e;
		}
	}

	/**
	 * Calls verifyDatabaseTables to check for table/index/file corruption.
	 *
	 * @return true if database is good, else false if there is corruption.
	 * @see #forwardRestoreBackupDatabase() to restore from backup and include log files for zero data loss.
	 * @see #backupDatabase(String) to perform database files, and include log files - which will enable forward restores.
	 */
	public boolean isDataSourceCorrupted() {
		boolean b;
		try {
			getOADataSourceJDBC().checkForCorruption();
			b = false;
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Error while checking datasource, will return false", e);
			b = true;
		}
		return b;
	}

	public boolean restoreDataSource(String backupDirectory) {
		boolean b;
		try {
			getOADataSourceJDBC().restore(backupDirectory);
			b = false;
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Error while restoring datasource, will return false", e);
			b = true;
		}
		return b;
	}

	public boolean backupDataSource(String backupDirectory) {
		if (getOADataSourceJDBC() == null) {
			return false;
		}
		boolean b;
		try {
			getOADataSourceJDBC().backup(backupDirectory);
			b = false;
		} catch (Exception e) {
			LOG.log(Level.WARNING, "Error while backing up datasource, will return false", e);
			b = true;
		}
		return b;
	}

	public boolean readFromXmlFile() throws Exception {
        final String dirName = Resource.getDataDirectory();
		LOG.log(Level.CONFIG, "Reading from file " + dirName + "/data.xml");
		File file = new File(OAFile.convertFileName(dirName + "/data.xml"));
		if (!file.exists()) {
			LOG.log(Level.CONFIG, "file " + dirName + "/data.xml does not exist");
			return false;
		}

		OAXMLReader r = new OAXMLReader();
		r.read(file);

		LOG.log(Level.CONFIG, "reading from file data.xml completed");
		return true;
	}

	public void writeToXmlFile() throws Exception {
        final String dirName = Resource.getDataDirectory();
		File f1 = new File(dirName);
		if (f1.exists()) {
			if (!f1.isDirectory()) {
				File f2;
				for (int i = 0;; i++) {
					f2 = new File(dirName + "_" + i);
					if (!f2.exists()) {
						break;
					}
				}
				f1.renameTo(f2);
			}
		} else {
			f1.mkdir();
		}

		LOG.fine("Saving data to file temp.xml, will rename when done");

		File fileTemp = new File(OAString.convertFileName(dirName + "/temp.xml"));
		_writeToXmlFile(fileTemp);

		File dataFile = new File(OAString.convertFileName(dirName + "/data.xml"));

		if (dataFile.exists()) {
			String backupName = null;

			// save to daily/hourly/5minute
			OADate d = new OADate();
			if (dateLastXml == null || !d.equals(dateLastXml)) {
				dateLastXml = d;
				backupName = "data_daily_" + (d.toString("yyMMdd")) + ".xml";
				File f = new File(OAFile.convertFileName(dirName + "/" + backupName));
				if (f.exists()) {
					backupName = null;
				}
			}

			if (backupName == null || backupName.length() == 0) {
				OATime t = new OATime();
				if (timeLastXml == null || t.getHour() != timeLastXml.getHour()) {
					timeLastXml = t;
					backupName = "data_hourly_" + (t.toString("HH")) + ".xml";
				} else {
					// save to nearest 5 minutes, otherwise it could have 60 files over time.
					int m = t.getMinute();
					m = m == 0 ? 0 : ((m / 5) * 5);
					backupName = "data_min_" + OAString.format(m, "00") + ".xml";
				}
			}

			backupName = OAFile.convertFileName(dirName + "/" + backupName);
			File backupFile = new File(backupName);
			if (backupFile.exists()) {
				backupFile.delete();
			}
			dataFile.renameTo(backupFile);
			dataFile = new File(OAString.convertFileName(dirName + "/data.xml"));
		}

		fileTemp.renameTo(dataFile);
		LOG.fine("Saved data to file " + dataFile);
	}

	protected void _writeToXmlFile(final File file) throws Exception {
		OAXMLWriter w = new OAXMLWriter(file.getPath());

		w.setIndentAmount(2);
		w.write(serverRoot);
		w.close();
	}

    public boolean readFromJsonFile() throws Exception {
        final String dirName = Resource.getDataDirectory();
        LOG.log(Level.CONFIG, "Reading from file " + dirName + "/data.json");
        File file = new File(OAFile.convertFileName(dirName + "/data.json"));
        if (!file.exists()) {
            LOG.log(Level.CONFIG, "file " + dirName + "/data.json does not exist");
            return false;
        }

        OAJson oaj = new OAJson();
        oaj.readObject(file, ServerRoot.class, false);

        LOG.log(Level.CONFIG, "reading from file data.json completed");

        return true;
    }

	public void writeToJsonFile() throws Exception {
		final String dirName = Resource.getDataDirectory();
		File f1 = new File(dirName);
		if (f1.exists()) {
			if (!f1.isDirectory()) {
				File f2;
				for (int i = 0;; i++) {
					f2 = new File(dirName + "_" + i);
					if (!f2.exists()) {
						break;
					}
				}
				f1.renameTo(f2);
			}
		} else {
			f1.mkdir();
		}

		LOG.fine("Saving data to file temp.json, will rename when done");

		File fileTemp = new File(OAString.convertFileName(dirName + "/temp.json"));
		_writeToJsonFile(fileTemp);

		File dataFile = new File(OAString.convertFileName(dirName + "/data.json"));

		if (dataFile.exists()) {
			String backupName = null;

			// save to daily/hourly/5minute
			OADate d = new OADate();
			if (dateLastJson == null || !d.equals(dateLastJson)) {
				dateLastJson = d;
				backupName = "data_daily_" + (d.toString("yyMMdd")) + ".json";
				File f = new File(OAFile.convertFileName(dirName + "/" + backupName));
				if (f.exists()) {
					backupName = null;
				}
			}

			if (backupName == null || backupName.length() == 0) {
				OATime t = new OATime();
				if (timeLastJson == null || t.getHour() != timeLastJson.getHour()) {
					timeLastJson = t;
					backupName = "data_hourly_" + (t.toString("HH")) + ".json";
				} else {
					// save to nearest 5 minutes, otherwise it could have 60 files over time.
					int m = t.getMinute();
					m = m == 0 ? 0 : ((m / 5) * 5);
					backupName = "data_min_" + OAString.format(m, "00") + ".json";
				}
			}

			backupName = OAFile.convertFileName(dirName + "/" + backupName);
			File backupFile = new File(backupName);
			if (backupFile.exists()) {
				backupFile.delete();
			}
			dataFile.renameTo(backupFile);
			dataFile = new File(OAString.convertFileName(dirName + "/data.json"));
		}

		fileTemp.renameTo(dataFile);
		LOG.fine("Saved data to file " + dataFile);
	}

	protected void _writeToJsonFile(final File file) throws Exception {
		OAJson oaj = new OAJson() {
			OAObjectInfo oi;
			@Override
			public boolean getUsePropertyCallback(Object obj, String propertyName) {
				if (obj == serverRoot) {
					if (oi == null) {
						oi = OAObjectInfoDelegate.getOAObjectInfo(serverRoot);
					}
					OALinkInfo li = oi.getLinkInfo(propertyName);
					if (li != null) {
						OADataSource ds = OADataSource.getDataSource(li.getToClass());
						if (ds != null && ds.supportsStorage()) {
							return false;
						}
					}
				}
				return true;
			}
		};
		oaj.setIncludeAll(true);
		oaj.write(serverRoot, file);
	}


	/*
	public static void main(String[] args) throws Exception {
	    DataSourceController dsc = new DataSourceController();

	    dsc.loadServerRoot();

	    // dsc.backupDatabase("c:\\temp\\dbBackDerby");
	    dsc.isDataSourceReady();
	    dsc.isDatabaseCorrupted();

	    System.out.println("Done");
	}
	*/
}
