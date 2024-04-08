// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.control.client;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.appstore.resource.Resource;
import com.viaoa.util.*;

/**
 * Support for:
 * storing PageFormats
 * loading html files
 */
public class ReportController {
    private static Logger LOG = Logger.getLogger(ReportController.class.getName());

    private ConcurrentHashMap<String, PageFormat> hmPageFormat = new ConcurrentHashMap<String, PageFormat>(7);
    
    public ReportController() {
    }
    
        
    public PageFormat getPageFormat(String reportName) {
        if (reportName == null) reportName = "";
        PageFormat pageFormat = hmPageFormat.get(reportName);
        if (pageFormat == null) {
            pageFormat = loadPageFormat(reportName);
            setPageFormat(reportName, pageFormat);
        }
        return pageFormat;
    }
    
    private PageFormat loadPageFormat(String reportName) {
        PageFormat pageFormat = new PageFormat();
        Paper paper = pageFormat.getPaper();  // this creates a copy of paper
        
        if (OAString.isEmpty(reportName)) reportName = "";
        else reportName += ".";

        double w = Resource.getDouble(reportName+Resource.RPT_PaperWidth, 0.0);
        double h = Resource.getDouble(reportName+Resource.RPT_PaperHeight, 0.0);
        if (w > 1.0 && h > 1.0) {
            paper.setSize(w,h);
        }
        else {
            w = paper.getWidth();
            h = paper.getHeight();
        }

        double x = Resource.getDouble(reportName+Resource.RPT_X, 18);
        double y = Resource.getDouble(reportName+Resource.RPT_Y, 18);
        double w2 = Resource.getDouble(reportName+Resource.RPT_Width);
        double h2 = Resource.getDouble(reportName+Resource.RPT_Height);
        if (w2 == 0.0) w2 = w - (x * 2);
        if (h2 == 0.0) h2 = h - (y * 2);
        paper.setImageableArea(x,y,w,h);
        pageFormat.setPaper(paper);
            
        String s = Resource.getValue(Resource.RPT_Orientation);
        if (s != null) {
            if (s.equalsIgnoreCase("Landscape")) pageFormat.setOrientation(PageFormat.LANDSCAPE);
            else pageFormat.setOrientation(PageFormat.PORTRAIT);
        }
        return pageFormat;
    }

    public void setPageFormat(String reportName, PageFormat pageFormat) {
        hmPageFormat.put(reportName, pageFormat);
    }

    
    public void savePageFormat(String reportName, PageFormat pageFormat) {
        Paper paper = pageFormat.getPaper();
        
        Resource.setValue(Resource.TYPE_Client, reportName+Resource.RPT_PaperWidth, paper.getWidth()+"");
        Resource.setValue(Resource.TYPE_Client, reportName+Resource.RPT_PaperHeight, paper.getHeight()+"");
        
        Resource.setValue(Resource.TYPE_Client, reportName+Resource.RPT_X, paper.getImageableX()+"");
        Resource.setValue(Resource.TYPE_Client, reportName+Resource.RPT_Y, paper.getImageableY()+"");
        Resource.setValue(Resource.TYPE_Client, reportName+Resource.RPT_Width, paper.getImageableWidth()+"");
        Resource.setValue(Resource.TYPE_Client, reportName+Resource.RPT_Height, paper.getImageableHeight()+"");

        int x = pageFormat.getOrientation();
        String s;
        if (x == PageFormat.LANDSCAPE) s = "LANDSCAPE";
        else s = "PORTRAIT";
        Resource.setValue(Resource.TYPE_Client, reportName+Resource.RPT_Orientation, s);
        Resource.save();
    }

    protected String loadJarHtmlFile(String fname) {
        fname = Resource.getValue(Resource.APP_ReportDirectory) + "/" + fname;
        String doc = null;
        try {
            doc = OAFile.readTextFile(this.getClass(), fname, 1024 * 2);
            if (doc == null) {
                LOG.warning("Cant read jar html file, fname="+fname);
                doc = "";
            }
            BufferedReader reader = new BufferedReader(new StringReader(doc));
            StringBuilder sb = new StringBuilder(doc.length());
            for (;;) {
                String s = reader.readLine();
                if (s == null) break;
                sb.append(s.trim());
                sb.append(OAString.NL);
            }
            doc = new String(sb);
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "cant read jar html file for report, fname="+fname, e);
        }
        return doc;
    }
    
}    


