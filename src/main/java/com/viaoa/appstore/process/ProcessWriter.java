// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.process;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.logging.Logger;

import com.viaoa.appstore.resource.Resource;
import com.viaoa.object.OAObject;
import com.viaoa.util.*;


/**
 * used to write objects to a log/text file.  A form a "fast" logger.
 * @author vvia
 *
 */
public class ProcessWriter {
    private static Logger LOG = Logger.getLogger(ProcessWriter.class.getName());

    private PrintWriter pw;
    private long msNextDateChange;

    // custom code here
    public String convert(OAObject obj) {
        return null;
    }

    public void write(OAObject obj) throws Exception {
        if (obj == null) return;
        String txt = convert(obj);
        if (txt == null) return;
        getWriter().println(txt);
        getWriter().flush();
    }

    public synchronized PrintWriter getWriter() throws Exception {
        if (pw != null) {
            if (System.currentTimeMillis() < msNextDateChange) {
                return pw;
            }
            pw.close();
            pw = null;
        }

        OADate date = new OADate();
        msNextDateChange = date.addDays(1).getTime();
        
        String fileName = Resource.getLogsDirectory() + "/" + date.toString("yyyyMMdd") + "_Status.csv";
        fileName = OAString.convertFileName(fileName);
        LOG.config("New status file is " + fileName);
        
        FileOutputStream fout = new FileOutputStream(fileName, true);
        BufferedOutputStream bout = new BufferedOutputStream(fout);
        pw = new PrintWriter(bout);
        String s = getHeader();
        if (s != null) pw.println(getHeader());
        pw.flush();
        return pw;
    }
    
    public String getHeader() {
        return null;
    }
}
