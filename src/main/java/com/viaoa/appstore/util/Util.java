// Copied from OATemplate project by OABuilder 03/12/24 01:50 PM
package com.viaoa.appstore.util;

import java.awt.Desktop;
import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

import com.viaoa.appstore.resource.Resource;
import com.viaoa.comm.multiplexer.OAMultiplexerClient;
import com.viaoa.comm.multiplexer.OAMultiplexerServer;
import com.viaoa.datasource.OADataSource;
import com.viaoa.datasource.jdbc.OADataSourceJDBC;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.remote.multiplexer.OARemoteMultiplexerClient;
import com.viaoa.remote.multiplexer.OARemoteMultiplexerServer;
import com.viaoa.sync.OASyncClient;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.sync.OASyncServer;

public class Util {
	private static Logger LOG = Logger.getLogger(Util.class.getName());

	public static void showLookAndFeels() {
		UIManager.LookAndFeelInfo[] lfs = UIManager.getInstalledLookAndFeels();
		for (int i = 0; lfs != null && i < lfs.length; i++) {
			System.out.println(i + ") " + lfs[i].getName());
		}
	}

	public static void showLookAndFeelDefaults() {
		UIDefaults uid = UIManager.getLookAndFeel().getDefaults();
		Enumeration keys = uid.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			Object value = uid.get(key);
			System.out.println(key + " = " + value);
		}
	}

	public static void showSystemInfo() {
		Properties props = System.getProperties();
		Enumeration en = props.keys();
		for (int i = 0; en.hasMoreElements(); i++) {
			Object key = en.nextElement();
			if (key instanceof String) {
				System.out.println(i + ") " + key + " = " + props.getProperty((String) key));
			}
			;
		}
	}

	public static void lauchBrowser(String url) throws Exception {
		Desktop.getDesktop().browse(new URI(url));
	}

	public static Vector<String> getInfo() {
		Vector<String> vecAll = new Vector<String>();

		System.gc();
		vecAll.addElement("Memory ============================");
		vecAll.addElement(" Total: " + String.format("%,d", Runtime.getRuntime().totalMemory(), "#,###"));
		vecAll.addElement("  Free: " + String.format("%,d", Runtime.getRuntime().freeMemory(), "#,###"));
		vecAll.addElement("   Max: " + String.format("%,d", Runtime.getRuntime().maxMemory(), "#,###"));

		vecAll.addElement("Object Cache =======================");
		OAObjectCacheDelegate.getInfo(vecAll);

		vecAll.addElement("Triggers =======================");
		vecAll.addElement("total: " + OAObjectInfo.getTotalTriggers());

		vecAll.addElement("DataSource =========================");
		OADataSource[] oadss = OADataSource.getDataSources();
		for (int i = 0; oadss != null && i < oadss.length; i++) {
			OADataSource oads = oadss[i];
			OADataSourceJDBC ds = null;
			if (oads instanceof OADataSourceJDBC) {
				ds = (OADataSourceJDBC) oads;
				ds.getInfo(vecAll);
			}
		}

		OASyncClient sc = OASyncDelegate.getSyncClient();
		if (sc != null) {
			vecAll.addElement("OASync Client ======================");
			OARemoteMultiplexerClient rmc = sc.getRemoteMultiplexerClient();
			vecAll.addElement(" remote methods called: " + String.format("%,d", rmc.getMethodCallCount(), "#,###"));
			vecAll.addElement("   received: " + String.format("%,d", rmc.getReceivedMethodCount(), "#,###"));

			OAMultiplexerClient mc = rmc.getMultiplexerClient();
			vecAll.addElement(" vsockets live: " + String.format("%,d", mc.getLiveSocketCount(), "#,###"));
			vecAll.addElement("   created: " + String.format("%,d", mc.getCreatedSocketCount(), "#,###"));
			vecAll.addElement(" read count: " + String.format("%,d", mc.getReadCount(), "#,###"));
			vecAll.addElement("   size: " + String.format("%,d", mc.getReadSize(), "#,###"));
			vecAll.addElement(" write count: " + String.format("%,d", mc.getWriteCount(), "#,###"));
			vecAll.addElement("   size: " + String.format("%,d", mc.getWriteSize(), "#,###"));

		}

		OASyncServer ss = OASyncDelegate.getSyncServer();
		if (ss != null) {
			vecAll.addElement("OASync Server ======================");

			OARemoteMultiplexerServer rms = ss.getRemoteMultiplexerServer();
			vecAll.addElement(" remote methods called: " + String.format("%,d", rms.getMethodCallCount(), "#,###"));
			vecAll.addElement("   received: " + String.format("%,d", rms.getReceivedMethodCount(), "#,###"));

			vecAll.addElement(" queue position: " + String.format("%,d", rms.getQueueHeadPos(), "#,###"));

			OAMultiplexerServer ms = rms.getMultiplexerServer();
			vecAll.addElement(" connections live: " + String.format("%,d", ms.getLiveConnectionCount(), "#,###"));
			vecAll.addElement("   created: " + String.format("%,d", ms.getCreatedConnectionCount(), "#,###"));

			vecAll.addElement(" read count: " + String.format("%,d", ms.getReadCount(), "#,###"));
			vecAll.addElement("   size: " + String.format("%,d", ms.getReadSize(), "#,###"));
			vecAll.addElement(" write count: " + String.format("%,d", ms.getWriteCount(), "#,###"));
			vecAll.addElement("   size: " + String.format("%,d", ms.getWriteSize(), "#,###"));
		}
		vecAll.addElement(" ");

		Vector vec;
		Enumeration enumx;

		vecAll.add("================== Resource properties ==================");
		vec = new Vector();
		enumx = Resource.getBundleProperties().keys();
		for (; enumx.hasMoreElements();) {
			String key = (String) enumx.nextElement();
			vec.addElement(key + " = " + convertValue(key, Resource.getValue(key)));
		}
		Collections.sort(vec);
		vecAll.addAll(vec);

		vecAll.add("================== Runtime arguments ==================");
		vec = new Vector();
		enumx = Resource.getRuntimeProperties().keys();
		for (; enumx.hasMoreElements();) {
			String key = (String) enumx.nextElement();
			vec.addElement(key + " = " + convertValue(key, Resource.getValue(key)));
		}
		Collections.sort(vec);
		vecAll.addAll(vec);

		vecAll.add("================== server.ini properties ==================");
		vec = new Vector();
		enumx = Resource.getServerProperties().keys();
		for (; enumx.hasMoreElements();) {
			String key = (String) enumx.nextElement();
			vec.addElement(key + " = " + convertValue(key, Resource.getValue(key)));
		}
		Collections.sort(vec);
		vecAll.addAll(vec);

		vecAll.add("================== client.ini properties ==================");
		vec = new Vector();
		enumx = Resource.getClientProperties().keys();
		for (; enumx.hasMoreElements();) {
			String key = (String) enumx.nextElement();
			vec.addElement(key + " = " + convertValue(key, Resource.getValue(key)));
		}
		Collections.sort(vec);
		vecAll.addAll(vec);

		vecAll.add("================== single.ini properties ==================");
		vec = new Vector();
		enumx = Resource.getSingleProperties().keys();
		for (; enumx.hasMoreElements();) {
			String key = (String) enumx.nextElement();
			vec.addElement(key + " = " + convertValue(key, Resource.getValue(key)));
		}
		Collections.sort(vec);
		vecAll.addAll(vec);

		vecAll.add("================== System properties ==================");
		vec = new Vector();
		Properties props = System.getProperties();
		enumx = props.keys();
		for (; enumx.hasMoreElements();) {
			String key = (String) enumx.nextElement();
			vec.addElement(key + ": " + props.getProperty(key));
		}
		Collections.sort(vec);
		vecAll.addAll(vec);

		return vecAll;
	}

	protected static String convertValue(String key, String val) {
		if (key == null || val == null) {
			return "";
		}
		key = key.toLowerCase();
		if (key.indexOf("password") >= 0) {
			val = "********";
		} else if (key.indexOf("pw") >= 0) {
			val = "*****";
		} else if (key.indexOf("secret") >= 0) {
			val = "*****";
		}
		return val;
	}

	/**
	 * Allows leading spaces for padding.
	 */
	public static String convertToValidPhoneNumber(String phone) {
		if (phone == null) {
			return null;
		}
		int x = phone.length();
		if (x == 0) {
			return phone;
		}
		StringBuilder sb = new StringBuilder(x);
		boolean b = false;
		for (int i = 0; i < x; i++) {
			char ch = phone.charAt(i);
			if (!Character.isDigit(ch)) {
				if (ch != ' ') {
					b = true;
					continue;
				}
			}
			sb.append(ch);

		}
		x = sb.length();
		for (int i = x; i < 10; i++) {
			b = true;
			sb.insert(0, ' ');
		}
		if (b) {
			phone = sb.toString();
		}
		return phone;
	}
}
