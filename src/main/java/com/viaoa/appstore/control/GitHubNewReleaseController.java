package com.viaoa.appstore.control;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

import com.viaoa.appstore.resource.Resource;
import com.viaoa.util.OAFile;
import com.viaoa.util.OAProperties;
import com.viaoa.util.OAString;

/**
 * Used to get new files from github ViaOA/oaappstore-run project, which is where new install files are stored.
 *
 * @author vvia
 */
public class GitHubNewReleaseController {

	private static Logger LOG = Logger.getLogger(GitHubNewReleaseController.class.getName());

	private static final String httpDirectory = "https://github.com/ViaOA/oaappstore-run/raw/master/executable-jar";

	private OAProperties gitProps;

	/**
	 * gets property file from github/version.ini to find out what release/version is currently available.
	 */
	public OAProperties getGitProperties() throws Exception {
		if (gitProps != null) {
			return gitProps;
		}

		URL url = new URL(httpDirectory + "/version.ini");
		URLConnection conn = url.openConnection();

		gitProps = new OAProperties(conn.getInputStream());

		return gitProps;
	}

	/**
	 * Get new uber jar from github, update the config file, and download other files that are in the oabuiler.ini file (name=getFile#).
	 */
	public void updateSoftwareForJPackageInstaller() throws Exception {
		final String rootDir = Resource.getValue(Resource.APP_RootDirectory);
		final String currentRelease = Resource.getValue(Resource.APP_Release);

		getGitProperties();
		final String gitRelease = gitProps.getProperty(Resource.APP_Release);
		final String gitVersion = gitProps.getProperty(Resource.APP_Version);

		LOG.fine(String.format("currentRelease=%s, gitRelease=%s, gitVersion=%s", currentRelease, gitRelease, gitVersion));

		if (currentRelease.equals(gitRelease)) {
			return;
		}

		String fileName = OAString.field(Resource.getValue(Resource.APP_JarFileName), ".", 1);
		fileName += "-" + gitRelease + ".jar";

		File file = new File(OAFile.convertFileName(rootDir + "/" + fileName));

		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		OutputStream fos = new FileOutputStream(file);

		URL url = new URL(httpDirectory + "/oaAppStore.jar");
		URLConnection conn = url.openConnection();

		DataInputStream dis = new DataInputStream(new BufferedInputStream(conn.getInputStream()));

		final byte[] bs = new byte[8196];
		int tot = 0;
		for (int i = 0;; i++) {
			int x = dis.read(bs);
			if (x < 0) {
				break;
			}
			tot += x;
			fos.write(bs, 0, x);
		}
		LOG.fine("loaded file " + fileName + ", from " + url.toString() + ", size=" + tot);

		// the jpackage creates a *.cfg file that needs to be changed for the new jar file
		File f = new File(rootDir);
		File[] files = f.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".cfg");
			}
		});
		if (files != null && files.length > 0) {
			LOG.fine("setting " + files[0].getName() + " to app.classpath=$APPDIR/" + fileName);

			BufferedReader reader = new BufferedReader(new FileReader(files[0]));
			StringBuffer sb = new StringBuffer(2048);
			for (;;) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}

				if (line.toLowerCase().indexOf("app.classpath=".toLowerCase()) >= 0) {
					line = "app.classpath=$APPDIR/" + fileName;
				}

				sb.append(line);
				sb.append(OAString.NL);
			}
			reader.close();
			OAFile.writeTextFile(files[0], sb.toString());
		}

		LOG.fine("loading all files defined in version.ini as name=getfile#");

		// load other files
		for (int i = 1;; i++) {
			String fn = gitProps.getProperty("getfile" + i);
			LOG.fine("Checking version.ini for name=getfile" + i + ", value = " + fn);
			if (OAString.isEmpty(fn)) {
				if (i > 10) {
					break;
				}
				continue;
			}

			file = new File(OAFile.convertFileName(rootDir + "/" + fn));
			LOG.fine("file = " + file);
			if (file.exists()) {
				LOG.fine("file exists, deleting now");
				file.delete();
			}

			file.createNewFile();
			fos = new FileOutputStream(file);

			url = new URL(httpDirectory + "/" + fn);
			conn = url.openConnection();
			dis = new DataInputStream(new BufferedInputStream(conn.getInputStream()));

			tot = 0;
			for (;;) {
				int x = dis.read(bs);
				if (x < 0) {
					break;
				}
				tot += x;
				fos.write(bs, 0, x);
			}
			LOG.fine("loaded file " + fn + ", from " + url.toString() + ", size=" + tot);
		}
		LOG.fine("updating done");
	}

	public static void main(String[] args) throws Exception {
		GitHubNewReleaseController c = new GitHubNewReleaseController();
		// c.updateSoftwareForWindowsInstaller();
	}

}
