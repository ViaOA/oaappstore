// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.control;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.viaoa.appstore.resource.Resource;
import com.viaoa.util.OADate;
import com.viaoa.util.OADateTime;
import com.viaoa.util.OAFile;
import com.viaoa.util.OAString;

/*
 * Sets up Logging environment for complete application.  Root package level has Log Handlers
 * for console and log file.  An additional logger is created that will call the internal
 * abstract methods onXXX() ex: "onError(...)"
 * Also sets up OAException handler.
 * <br>
 * The messages used are stored in the Resource Bundle "values.properties".
 */
public class LogController {
	private static final Logger LOG = Logger.getLogger(LogController.class.getName());

	public static final Logger LOGSpecial = Logger.getLogger("Special");
	public static final Logger LOGException = Logger.getLogger("Exception");

	private ArrayList<FileHandler> alFileHandlers = new ArrayList<FileHandler>(15);

	private ArrayList<Logger> alLogger = new ArrayList<Logger>();

	private boolean bIsServer;
	private final String strDateTimeStamp;
	private final OADateTime dtStarted;

	// this is used to capture all Logs > WARNING
	public static final Level Level_ERROR = new LevelError();

	public static class LevelError extends Level {
		public LevelError() {
			super("Error", Level.WARNING.intValue() + 1);
		}
	}

	public LogController(boolean bServer) {
		this.bIsServer = bServer;
		dtStarted = new OADateTime();

		if (bServer) {
			strDateTimeStamp = dtStarted.toString("yyyyMMdd");
		} else {
			strDateTimeStamp = dtStarted.toString("yyyyMMdd_HHmm");
		}

		// turn off top level logger
		Logger log = Logger.getLogger("");
		log.setLevel(Level.OFF);
		Handler[] hs = log.getHandlers();
		for (int i = 0; hs != null && i < hs.length; i++) {
			hs[i].setLevel(Level.OFF);
		}

		String s = Resource.getValue(Resource.APP_ConsoleLogLevel);
		Level level = getLevel(s);

		FileHandler fh, fh2;

		// All Config+ go to console and Config.log
		log = Logger.getLogger("");
		log.setLevel(level);

		ConsoleHandler ch = new ConsoleHandler();
		ch.setLevel(level);
		log.addHandler(ch);

		fh = createFileHandler("console", level);
		log.addHandler(fh);
		log.addHandler(createStatusHandler(level)); // for UI status label

		// All Warnings+ go Warn.log 
		log = Logger.getLogger("");
		log.setLevel(Level.WARNING);
		fh = createFileHandler("warn", Level.WARNING);
		log.addHandler(fh);
		log.addHandler(createErrorHandler());
		log.config("Starting new log");

		// Special Logging
		LOGSpecial.setLevel(Level.FINER);
		LOGSpecial.addHandler(ch);
		log.finer("Starting new log");

		// [APP_LogFileName].log
		s = Resource.getValue(Resource.APP_LogLevel);
		level = getLevel(s);

		s = Resource.getValue(Resource.APP_ClassPath);
		log = Logger.getLogger(s);
		alLogger.add(log);
		log.setLevel(level);
		s = Resource.getValue(Resource.APP_LogFileName, "app").toLowerCase();
		fh = createFileHandler(s, level, true);
		log.addHandler(fh);
		log.log(level, "Starting new log");

		/*
		s = Resource.getValue(Resource.APP_ClassPath) + ".model.oa.trigger";        
		log = Logger.getLogger(s);
		alLogger.add(log);
		log.setLevel(Level.FINER);
		fh = createFileHandler("trigger", Level.FINER, true);
		log.addHandler(fh);
		log.log(level, "Starting new log");
		*/

		s = "com.viaoa";
		log = Logger.getLogger(s);
		alLogger.add(log);
		log.setLevel(level);
		fh = createFileHandler("oa", level, true);
		log.addHandler(fh);
		log.log(level, "Starting new log");

		s = "com.viaoa.object.OAPerformance";
		log = Logger.getLogger(s);
		alLogger.add(log);
		log.setLevel(Level.FINE);
		fh = createFileHandler("oaperf", Level.FINE, false);
		log.addHandler(fh);
		log.log(level, "Starting new log");

		/*
		// Exceptions Logger, using RMI
		LOGException.setLevel(Level.FINEST);
		fh = createFileHandler("exception", Level.FINEST);
		LOGException.addHandler(fh);
		LOGException.finest("Starting new log");
		*/

		if (bServer) {
			// DB
			s = "com.viaoa.datasource.jdbc";
			log = Logger.getLogger(s);
			alLogger.add(log);
			log.setLevel(Level.FINE);
			fh = createFileHandler("db", Level.FINE, true);
			log.addHandler(fh);
			log.fine("Starting new log");

			s = "com.viaoa.datasource.jdbc.delegate.DBLogDelegate";
			log = Logger.getLogger(s);
			alLogger.add(log);
			log.setLevel(Level.FINE);
			// fh = createFileHandler("dbchanges", Level.FINE);
			// log.addHandler(fh);

			// OAObject
			/*
			s = "OAObject";        
			log = Logger.getLogger(s);
			alLogger.add(log);
			log.setLevel(Level.FINEST);
			fh = createFileHandler("oaobject", Level.FINEST, true);
			log.addHandler(fh);
			log.finest("Starting new log");
			
			
			// [APP_JSP].log
			s = "oajsp";        
			//was: s = Resource.getValue(Resource.APP_ClassPath) + ".jsp";        
			log = Logger.getLogger(s);
			alLogger.add(log);
			log.setLevel(Level.FINE);
			fh = createFileHandler("jsp", Level.FINE);
			log.addHandler(fh);
			log.fine("Starting new log");
			alLogger.add(log);
			
			// second [APP_JSP].log
			s = Resource.getValue(Resource.APP_ClassPath) + ".jsp";        
			log = Logger.getLogger(s);
			alLogger.add(log);
			log.setLevel(Level.FINEST);
			fh = createFileHandler("jsp", Level.FINEST);
			log.addHandler(fh);
			log.finest("Starting new log");
			alLogger.add(log);
			*/
		}

		/*
		// Monitors for Client and Server
		log = Logger.getLogger(LogController.class.getName());
		alLogger.add(log);
		log.setLevel(Level.FINE);
		fh = createFileHandler("csmonitor", Level.FINE, true);
		log.addHandler(fh);
		log.config("Starting new log");
		*/

		/* OA Tests
		log = Logger.getLogger("com.viaoa");
		alLogger.add(log);
		log.setLevel(Level.FINE);
		Filter filter = new Filter() {
			public boolean isLoggable(LogRecord record) {
				String s = record.getSourceClassName();
				if (record.getLevel() == Level.WARNING) {
					int qqq = 4;
				}
				return (s != null && s.endsWith("Test"));
			}
		};
		log.setFilter(filter);
		ch = new ConsoleHandler();
		ch.setLevel(Level.FINE);
		log.addHandler(ch);
		ch.setFilter(filter);
		
		Formatter fmt = new Formatter() {
			@Override
			public synchronized String format(LogRecord record) {
				return record.getMessage() + com.viaoa.util.OAString.NL;
			}
		};
		ch.setFormatter(fmt);
		*/

		// Database XML/log file
		s = Resource.getLogsDirectory() + "/";
		s += strDateTimeStamp + "_DB.xml";
		s = OAString.convertFileName(s);
		// turned off, since log gets huge - cause is when OrderItems get update timestamp and they get saved to derby
		// OAObjectLogDelegate.createXMLLogFile(s);
	}

	public ArrayList dumpStackTrace() throws Exception {
		String fname = Resource.getLogsDirectory() + "/";
		String strDateTime = (new OADateTime()).toString("yyyyMMdd_HHmm");
		fname += "StackTraces_" + strDateTime;
		fname = OAString.convertFileName(fname);
		File file = new File(fname);
		PrintWriter pw = new PrintWriter(file);
		ArrayList<String> list = new ArrayList<String>();
		String s = "" + (new OADateTime());
		pw.println(s);
		list.add(s);

		Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
		Iterator it = map.entrySet().iterator();
		for (int i = 1; it.hasNext(); i++) {
			Map.Entry me = (Map.Entry) it.next();
			Thread t = (Thread) me.getKey();
			s = i + ") " + t.getName();
			pw.println(s);
			list.add(s);

			StackTraceElement[] stes = (StackTraceElement[]) me.getValue();
			if (stes == null) {
				continue;
			}
			for (StackTraceElement ste : stes) {
				s = "  " + ste.getClassName() + " " + ste.getMethodName() + " " + ste.getLineNumber();
				pw.println(s);
				list.add(s);
			}
			pw.println(" ");
			list.add(" ");
		}
		pw.close();
		return list;
	}

	private Handler createStatusHandler(Level level) {
		Handler handler = new Handler() {
			public void publish(LogRecord record) {
				if (isLoggable(record)) {
					LogController.this.onStatusMessage(record.getMessage());
				}
			}

			public void close() throws SecurityException {
			}

			public void flush() {
			}
		};
		handler.setLevel(level);
		return handler;
	}

	// all Log with Level > WARNING
	private Handler createErrorHandler() {
		Handler handler = new Handler() {
			public void publish(LogRecord record) {
				if (isLoggable(record)) {
					LogController.this.onErrorMessage(record.getMessage(), record.getThrown());
				}
			}

			public void close() throws SecurityException {
			}

			public void flush() {
			}
		};
		handler.setLevel(Level.WARNING);
		return handler;
	}

	public String createLogFileName(String prefix) {
		if (prefix == null) {
			return null;
		}

		String dirName = Resource.getLogsDirectory();
		String fileName = dirName + "/";

		if (bIsServer) {
			fileName += (new OADateTime()).toString("yyyyMMdd");
		} else {
			fileName += strDateTimeStamp;
		}
		fileName += "-" + prefix;
		fileName += ".log";
		fileName = OAString.convertFileName(fileName);
		OAFile.mkdirsForFile(fileName);
		return fileName;
	}

	private FileHandler createFileHandler(String prefix, Level level) {
		return createFileHandler(prefix, level, false);
	}

	/**
	 * Creates log files for Logger, OAExceptions and OAObject.createXMLLogFile
	 */
	private FileHandler createFileHandler(final String prefix, final Level level, boolean bCreateMax) {
		try {
			// create File message Handler
			String fileName = Resource.getLogsDirectory();
			fileName = OAString.convertFileName(fileName);
			File f = new File(fileName);
			if (f.exists()) {
				if (!f.isDirectory()) {
					f.renameTo(new File("old_logs"));
				}
			}
			if (!f.exists()) {
				f.mkdirs();
			}

			if (bCreateMax) {
				fileName = createLogFileName(prefix + "_%g");
			} else {
				fileName = createLogFileName(prefix);
			}

			// All log messages
			FileHandler fileHandler;
			if (bCreateMax) {
				fileHandler = new FileHandler(fileName, 5 * 1024 * 1024, 5, true) {
					volatile FileHandler handler; // replacement handler when date changes
					volatile long msNextDateChange; // next date change
					long msLast;
					int threadIdLast;
					String sourceLast;
					int cntThrottle;

					@Override
					public synchronized void publish(LogRecord record) {
						if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
							String source = record.getSourceMethodName();
							int tid = record.getThreadID();
							long ms = System.currentTimeMillis();

							boolean bMatch = (tid == threadIdLast) && (source == null || source.equals(sourceLast));
							boolean bIgnore = bMatch && (msLast + 100 > ms);

							msLast = ms;
							threadIdLast = tid;
							sourceLast = source;

							if (bIgnore) {
								cntThrottle++;
								return; // ignore
							}
							if (bMatch) {
								String msg = record.getMessage();
								record.setMessage(msg += " - note: throttled");
							}
						}

						if (msNextDateChange == 0) {
							msNextDateChange = ((new OADate()).addDays(1).getTime()); // next day                            
						} else if (System.currentTimeMillis() >= msNextDateChange) {
							msNextDateChange = ((new OADate()).addDays(1).getTime()); // next day                            
							FileHandler fh = handler;
							if (fh == null) {
								fh = this;
							}
							fh.close();
							handler = createFileHandler(prefix, level, true);
						}
						if (handler == null) {
							super.publish(record);
						} else {
							handler.publish(record);
						}
					}
				};
			} else {
				fileHandler = new FileHandler(fileName, true) {
					volatile FileHandler handler; // replacement handler when date changes
					volatile long msNextDateChange; // next date change
					long msLast;
					int threadIdLast;
					String sourceLast;
					int cntThrottle;

					@Override
					public synchronized void publish(LogRecord record) {
						if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
							String source = record.getSourceMethodName();
							int tid = record.getThreadID();
							long ms = System.currentTimeMillis();

							boolean bMatch = (tid == threadIdLast) && (source == null || source.equals(sourceLast));
							boolean bIgnore = bMatch && (msLast + 100 > ms);

							msLast = ms;
							threadIdLast = tid;
							sourceLast = source;

							if (bIgnore) {
								cntThrottle++;
								return; // ignore
							}
							if (bMatch) {
								String msg = record.getMessage();
								record.setMessage(msg += " - note: throttled");
							}
						}

						if (msNextDateChange == 0) {
							msNextDateChange = ((new OADate()).addDays(1).getTime()); // next day                            
						} else if (System.currentTimeMillis() >= msNextDateChange) {
							msNextDateChange = ((new OADate()).addDays(1).getTime()); // next day                            
							FileHandler fh = handler;
							if (fh == null) {
								fh = this;
							}
							fh.close();
							handler = createFileHandler(prefix, level, false);
						}
						if (handler == null) {
							super.publish(record);
						} else {
							handler.publish(record);
						}
					}
				};
			}

			fileHandler.setFormatter(new SimpleFormatter());
			fileHandler.setLevel(level);

			alFileHandlers.add(fileHandler);
			return fileHandler;
		} catch (Exception e) {
			System.out.println("Error creating fileHandler: " + e);
		}
		return null;
	}

	public void close() {
		for (FileHandler fh : alFileHandlers) {
			fh.close();
		}
		if (pwFast != null) {
			pwFast.flush();
		}
	}

	public void removeOldLogFiles(int regularDays, int errorDays) {
		final OADate today = new OADate();
		final OADate dateExpire = (OADate) today.addDays(-regularDays);
		// OADate dateError = (OADate) today.addDays(-errorDays);
		final OADate yesterday = (OADate) (new OADate()).addDays(-1);

		String s = Resource.getLogsDirectory();
		s = OAString.convertFileName(s);
		File file = new File(s);
		if (!file.exists() || !file.isDirectory()) {
			return;
		}

		String[] fnames = file.list();
		for (int i = 0; i < fnames.length; i++) {
			try {
				String fn = fnames[i];
				if (fn.toLowerCase().indexOf(".log") < 0) {
					if (fn.toLowerCase().indexOf(".lck") < 0) {
						continue;
					}
				}

				s = Resource.getLogsDirectory() + "/" + fn;
				s = OAString.convertFileName(s);
				File f = new File(s);

				OADateTime dtFile = new OADateTime(f.lastModified());

				boolean bDelete = false;
				if (f.length() == 0) {
					bDelete = dtFile.before(yesterday);
				}
				if (!bDelete && fn.toLowerCase().indexOf(".lck") > 0) {
					bDelete = dtFile.before(yesterday);
				} else {
					bDelete = dtFile.before(dateExpire);
				}

				if (bDelete) {
					LOG.fine("delete log file " + fn);
					f.delete();
				}
			} catch (Exception e) {
			}
		}
	}

	public Level getLevel(String name) {
		if (name == null) {
			return Level.OFF;
		}
		if (name.equalsIgnoreCase("FINEST")) {
			return Level.FINEST;
		}
		if (name.equalsIgnoreCase("FINER")) {
			return Level.FINER;
		}
		if (name.equalsIgnoreCase("FINE")) {
			return Level.FINE;
		}
		if (name.equalsIgnoreCase("CONFIG")) {
			return Level.CONFIG;
		}
		if (name.equalsIgnoreCase("INFO")) {
			return Level.INFO;
		}
		if (name.equalsIgnoreCase("WARNING")) {
			return Level.WARNING;
		}
		if (name.equalsIgnoreCase("SEVERE")) {
			return Level.SEVERE;
		}
		return Level.OFF;
	}

	private boolean bClientMonitorStop;

	public void enableClientMonitor(boolean b, final int sleepSeconds) {
		bClientMonitorStop = !b;
		if (b) {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					//qqqq _runClientMonitor(sleepSeconds);
				}
			}, "LogController.clientMonitor");
			t.setDaemon(true);
			t.setPriority(t.MIN_PRIORITY);
			t.start();
		}
	}

	/*
	private void _runClientMonitor(int sleepSeconds) {
		int last = 0;
		long lastTime = 0;
		int lastClientInfo = 0;
		
		for (int i=0; !bClientMonitorStop ;i++) {
	        try {
	        	Thread.sleep(sleepSeconds * 1000);
	        }
	        catch (Exception e) {}
	
	    	OAClient c = OAClient.getClient();
	    	if (c == null) continue;
	        int x = c.getMessageReceivedCount();
	    	long l = System.currentTimeMillis();
	    	
	    	int x2 = c.getSendClientInfoCount();
	    	int diff = x2 - lastClientInfo;
	    	
	    	if ((last+diff < x) && l < lastTime + (30*60*1000)) continue;
	    	
	    	last = x;
	    	lastTime = l;
	    	lastClientInfo = x2;
		
			StringBuilder sb = new StringBuilder(1024);
			sb.append("OAClient Monitor =================="+OAString.NL);
			OAClientInfo clientInfo = c.getClientInfo();
			String[] ss = clientInfo.asStrings();
			for (int j=0; j<ss.length; j++) {
				sb.append(ss[j]+OAString.NL);
			}
	    	LOG.fine(new String(sb));
	    }
	}
	*/
	/*qqqqq
	private boolean bServerMonitorStop;
	public void enableServerMonitor(boolean b, final OAServerImpl server, final int sleepSeconds) {
		bServerMonitorStop = !b;
		if (b) {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					// _runServerMonitor(server, sleepSeconds);
				}
			}, "LogController.serverMonitor");
			t.setDaemon(true);
			t.setPriority(t.MIN_PRIORITY);
			t.start();
		}
	}
	*/
	/*qqqqqq
	private void _runServerMonitor(OAServerImpl server, int sleepSeconds) {
		Vector vec = new Vector(25,25);
		int last = 0;
		long lastTime = 0;
	    for (;!bServerMonitorStop;) {
	        try {
	        	Thread.sleep(sleepSeconds * 1000);
	        }
	        catch (Exception e) {}
	
	        int x = OAClient.getClient().getMessageReceivedCount();
	    	long l = System.currentTimeMillis();
	    	if (x == last && l < lastTime + (60*60*1000)) continue;
	    	last = x;
	    	lastTime = l;
	
			StringBuilder sb = new StringBuilder(1024);
			sb.append("OAServer Monitor =================="+OAString.NL);
	    	vec.clear();
	    	server.getInfo(vec, false);
	        x = vec.size();
	        for (int i=0; i<x; i++) {
	        	String s = (String) vec.get(i);
	        	sb.append("  "+s+OAString.NL);
	        }
	    	LOG.fine(new String(sb));
	    }
	}
	*/
	protected void onStatusMessage(String msg) {
	}

	protected void onErrorMessage(String msg, Throwable thrown) {
	}

	public static void disable() {
		Logger log = Logger.getLogger("");
		log.setLevel(Level.OFF);
		Handler[] hs = log.getHandlers();
		for (int i = 0; hs != null && i < hs.length; i++) {
			hs[i].setLevel(Level.OFF);
		}
	}

	public static void consoleOnly(Level level) {
		consoleOnly(level, "");
	}

	public static void consoleOnly(Level level, String name) {
		Logger log = Logger.getLogger("");
		log.setLevel(Level.OFF);

		Handler[] hs = log.getHandlers();
		for (int i = 0; hs != null && i < hs.length; i++) {
			hs[i].setLevel(Level.OFF);
			log.removeHandler(hs[i]);
		}

		ConsoleHandler ch = new ConsoleHandler();
		ch.setLevel(level);

		log = Logger.getLogger(name);
		log.setLevel(level);
		log.addHandler(ch);
	}

	// async fast logger

	private ArrayBlockingQueue<FastLog> queFastLog = new ArrayBlockingQueue<FastLog>(100);
	private Thread threadFastLog;

	private static class FastLog {
		long ts;
		OADateTime dt;
		String msg;
		Exception ex;
	}

	public void fastLog(String logMsg) {
		try {
			_fastLog(0, null, logMsg, null);
		} catch (Exception e) {
			LOG.log(Level.WARNING, "", e);
		}
	}

	public void fastLog(String logMsg, Exception ex) {
		try {
			_fastLog(0, null, logMsg, ex);
		} catch (Exception e) {
			LOG.log(Level.WARNING, "", e);
		}
	}

	public void fastLog(OADateTime dt, String logMsg, Exception ex) {
		try {
			_fastLog(0, dt, logMsg, ex);
		} catch (Exception e) {
			LOG.log(Level.WARNING, "", e);
		}
	}

	public void fastLog(OADateTime dt, String logMsg) {
		try {
			_fastLog(0, dt, logMsg, null);
		} catch (Exception e) {
			LOG.log(Level.WARNING, "", e);
		}
	}

	public void fastLog(long ts, String logMsg, Exception ex) {
		try {
			_fastLog(ts, null, logMsg, ex);
		} catch (Exception e) {
			LOG.log(Level.WARNING, "", e);
		}
	}

	public void fastLog(long ts, String logMsg) {
		try {
			_fastLog(ts, null, logMsg, null);
		} catch (Exception e) {
			LOG.log(Level.WARNING, "", e);
		}
	}

	protected void _fastLog(long ts, OADateTime dt, String logMsg, Exception ex) throws Exception {
		if (logMsg == null) {
			return;
		}

		FastLog fl = new FastLog();
		fl.msg = logMsg;
		fl.ex = ex;
		fl.ts = ts;
		fl.dt = dt;
		queFastLog.offer(fl, 5, TimeUnit.MILLISECONDS);

		if (threadFastLog != null) {
			return;
		}

		String tname = "LogController.fastlogger";
		LOG.fine("starting thread that writes fast logs, threadName=" + tname);
		threadFastLog = new Thread(new Runnable() {
			@Override
			public void run() {
				for (;;) {
					try {
						FastLog fl = queFastLog.take();
						_fastLog2(fl);
					} catch (Exception e) {
						LOG.log(Level.WARNING, "Exception while logging command", e);
					}
				}
			}
		}, tname);
		threadFastLog.setDaemon(true);
		threadFastLog.setPriority(Thread.MIN_PRIORITY);
		threadFastLog.start();
	}

	protected void _fastLog2(FastLog fl) throws Exception {
		if (fl == null) {
			return;
		}

		PrintWriter pw = getFastPrintWriter();

		OADateTime dt = fl.dt;
		if (dt == null) {
			if (fl.ts != 0) {
				dt = new OADateTime(fl.ts);
			} else {
				dt = new OADateTime();
			}
		}

		pw.println(dt.toString("HH:mm:ss.SSS") + " " + fl.msg);

		if (fl.ex != null) {
			fl.ex.printStackTrace(pw);
		}
		// pw.flush();
	}

	private PrintWriter pwFast;
	private long msNextLogDateChange;

	public PrintWriter getFastPrintWriter() throws Exception {
		if (pwFast != null) {
			if (System.currentTimeMillis() < msNextLogDateChange) {
				return pwFast;
			}
		}
		OADate date = new OADate();
		msNextLogDateChange = date.addDays(1).getTime();
		if (pwFast != null) {
			pwFast.close();
			pwFast = null;
		}

		String fileName = Resource.getLogsDirectory() + "/";
		fileName += (new OADateTime()).toString("yyyyMMdd");
		fileName += "-fast";
		fileName += ".log";
		fileName = OAString.convertFileName(fileName);

		LOG.config("fastlog file is " + fileName);
		FileOutputStream fout = new FileOutputStream(fileName, true);
		BufferedOutputStream bout = new BufferedOutputStream(fout);
		pwFast = new PrintWriter(bout);

		return pwFast;
	}

	/**
	 * Create a custom one-line formatter.
	 */
	protected Formatter createFormatter() {
		java.util.logging.Formatter formatter = new Formatter() {
			@Override
			public String format(LogRecord record) {
				String line = LogController.this.format(record);
				return line;
			}
		};
		return formatter;
	}

	protected String format(LogRecord record) {
		//usage:  fileHandler.setFormatter(createFormatter());

		// 2019-10-06 00:39:53,716 DEBUG [HikariPool-1 housekeeper] com.zaxxer.hikari.pool.HikariPool: HikariPool-1 - Before cleanup stats (total=5, active=0, idle=5, waiting=0)

		OADateTime dt = new OADateTime(record.getMillis());

		Thread thread = Thread.currentThread();

		String mname = record.getSourceClassName();
		if (OAString.isEmpty(mname)) {
			mname = record.getLoggerName();
		}
		int dcnt = OAString.dcount(mname, ".");
		String mname2 = "";
		for (int i = 0; i < dcnt; i++) {
			String s = OAString.field(mname, ".", i + 1);
			if (i < 3) {
				s = s.charAt(0) + "";
			}
			if (i > 0) {
				mname2 += ".";
			}
			mname2 += s;
		}

		String s = record.getSourceMethodName();
		if (OAString.isNotEmpty(s)) {
			mname += "." + s;
		}

		Throwable t = record.getThrown();
		String sException;
		if (t == null) {
			sException = "";
		} else {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			pw.println();
			t.printStackTrace(pw);
			pw.close();
			sException = pw.toString();
		}

		String line = String.format("%s %s [%s] %s: %s%s\n",
									dt.toString("yyyyMMdd HHmmss.SSS"),
									record.getLevel().getName(),
									thread.getName(),
									mname2,
									record.getMessage(),
									sException);

		return line;
	}

}
