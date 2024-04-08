// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.report;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.io.File;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;

import com.viaoa.appstore.control.client.PdfController;
import com.viaoa.appstore.delegate.JfcDelegate;
import com.viaoa.appstore.resource.Resource;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubChangeListener;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubListenerAdapter;
import com.viaoa.jfc.OAButton;
import com.viaoa.jfc.OAComboBox;
import com.viaoa.jfc.OAScroller;
import com.viaoa.jfc.control.OAJfcController;
import com.viaoa.jfc.editor.html.OAHTMLTextPane;
import com.viaoa.jfc.editor.html.control.OAHTMLTextPaneController;
import com.viaoa.jfc.editor.html.view.HtmlDebug;
import com.viaoa.jfc.print.PrintController;
import com.viaoa.jfc.report.OAHTMLConverter;
import com.viaoa.jfc.report.OAHTMLReport;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAFile;
import com.viaoa.util.OAProperties;
import com.viaoa.util.OAString;

public class ApplicationReport<F extends OAObject> extends OAHTMLReport<F> {
    private static Logger LOG = Logger.getLogger(ApplicationReport.class.getName());;
    
    private final static String PrefixName = "Report_";
    private String name;

    private JPanel panHtml; 

    private PrintController controlPrint;
    private PdfController controlPdf;
    private JFileChooser fileChooser;
    
    private JToolBar toolBar;
    private JButton cmdPdf;
    private boolean debug = false;
    private boolean bAutoRefresh;
    private boolean bEnabled = true;

    private OAJfcController jfcController;
    
    private Hub<F> hubOriginal;
    
    public ApplicationReport(final Hub<F> hub, String name, boolean bAutoRefresh) {
        this.hubOriginal = hub;
        Hub hubx = hub.createSharedHub();
        super.setHub(hubx);
        this.name = name;
        this.bAutoRefresh = bAutoRefresh;
        init();
    }
    public String getPrefixName() {
        return name;
    }
    
    @Override
    public OAProperties getProperties() {
        super.getProperties();
        if (properties == null) {
            properties = new OAProperties();
        }
        return properties;
    }

    public OAJfcController getJfcController() {
        if (jfcController != null) return jfcController;
        if (!bAutoRefresh || getHub() == null) return null;

        jfcController = new OAJfcController(getHub(), null, null, getHTMLTextPanel(), HubChangeListener.Type.HubValid, false, false) {
            @Override
            public void update(JComponent comp, Object object, boolean bIncudeToolTip) {
                super.update(comp, object, bIncudeToolTip);
                if (!getEnabled()) {
                    return;
                }
                refreshDetail();
            }
        };
        // This will only enable the report if/when it is visible
        jfcController.enableVisibleListener(true);
        
        return jfcController;
    }
    
    
    protected void init() {
        OAHTMLTextPane html = new OAHTMLTextPane();
        html.setSpellChecker(Resource.getSpellChecker());
        html.setPreferredSize(10, 4);
        
        // qqqq        
        // html.setEditable(false);
        
        // html.setImageLoader(getClass(), "/com/cdi/report");
        setDetailTextPane(html);
        
        getProperties().put("Heading", name + " Report");
        getProperties().put("Footing", "");

        loadPageFormat(getPageFormat());
        getPrintController().setPageFormat(getPageFormat());
        loadDefaultHtmlFiles();
        
        hubOriginal.onChangeAO( event -> {
            if (isOnHold()) return;
            else getHub().setAO(hubOriginal.getAO());
        });
        
        getHub().setAO(hubOriginal.getAO());
        
        if (bAutoRefresh && getHub() != null) {        
            refreshDetail();
        }
        getJfcController();
    }

    protected void loadDefaultHtmlFiles() {
        String titleHeader = null;
        String header = null;
        String footer = null;
        String detail = null;
        try {
            titleHeader = OAFile.readTextFile(this.getClass(), "/com/viaoa/appstore/report/html/titleHeader.html", 1024);
            header = OAFile.readTextFile(this.getClass(), "/com/viaoa/appstore/report/html/header.html", 1024);
            footer = OAFile.readTextFile(this.getClass(), "/com/viaoa/appstore/report/html/footer.html", 1024);
            detail = OAFile.readTextFile(this.getClass(), "/com/viaoa/appstore/report/html/oa/"+name+".html", 1024 * 3);
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "error while reading html files", e);
        }
        setTitle(name + " Report");
        setTitleHeaderHTML(titleHeader);
        setHeaderHTML(header);
        setFooterHTML(footer);
        setDetailHTML(detail);
    }
    
    protected void loadPageFormat(PageFormat pageFormat) {
        Paper paper = pageFormat.getPaper();  // this creates a copy of paper

        double w = Resource.getDouble(PrefixName+name+"_PaperWidth", 612.0);
        double h = Resource.getDouble(PrefixName+name+"_PaperHeight", 792.0);
        paper.setSize(w, h);

        double x = Resource.getDouble(PrefixName+name+"_X", 18);
        double y = Resource.getDouble(PrefixName+name+"_Y", 18);
        double w2 = Resource.getDouble(PrefixName+name+"_Width");
        double h2 = Resource.getDouble(PrefixName+name+"_Height");
        if (w2 == 0.0) w2 = w - (x * 2);
        if (h2 == 0.0) h2 = h - (y * 2);
        paper.setImageableArea(x, y, w2, h2);
        pageFormat.setPaper(paper);
            
        String s = Resource.getValue(PrefixName+name+"_Orientation");
        if (s != null) {
            if (s.equalsIgnoreCase("Landscape")) pageFormat.setOrientation(PageFormat.LANDSCAPE);
            else pageFormat.setOrientation(PageFormat.PORTRAIT);
        }
    }

    protected void savePageFormat() {
        Paper paper = getPrintController().getPageFormat().getPaper();
        
        Resource.setValue(Resource.TYPE_Client, PrefixName+name+"_PaperWidth", paper.getWidth()+"");
        Resource.setValue(Resource.TYPE_Client, PrefixName+name+"_PaperHeight", paper.getHeight()+"");
        
        Resource.setValue(Resource.TYPE_Client, PrefixName+name+"_X", paper.getImageableX()+"");
        Resource.setValue(Resource.TYPE_Client, PrefixName+name+"_Y", paper.getImageableY()+"");
        Resource.setValue(Resource.TYPE_Client, PrefixName+name+"_Width", paper.getImageableWidth()+"");
        Resource.setValue(Resource.TYPE_Client, PrefixName+name+"_Height", paper.getImageableHeight()+"");

        int x = getPrintController().getPageFormat().getOrientation();
        String s;
        if (x == PageFormat.LANDSCAPE) s = "LANDSCAPE";
        else s = "PORTRAIT";
        Resource.setValue(Resource.TYPE_Client, PrefixName+name+"_Orientation", s);
        Resource.save();
    }
    
    public JPanel getHTMLTextPanel() {
        if (panHtml != null) return panHtml;
        panHtml = new JPanel(new BorderLayout());
        panHtml.add(new OAScroller(getToolBar()), BorderLayout.NORTH);
        
        HtmlDebug x = new HtmlDebug(getDetailTextPane());

        if (debug) {
            JScrollPane spx = new JScrollPane(getDetailTextPane());    
            JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, x.getPanel(), spx);
            sp.setOneTouchExpandable(true);
            sp.setDividerSize(12);
            
            panHtml.add(sp, BorderLayout.CENTER);
        
            sp.setDividerLocation(0);
        }
        else {
            panHtml.add(new JScrollPane(getDetailTextPane()), BorderLayout.CENTER);
        }
        return panHtml;
    }

    
    public JToolBar getToolBar() {
        if (toolBar != null) return toolBar;

        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        PrintController controlPrint = getPrintController();
        
        toolBar.add(Box.createHorizontalStrut(4));
        
        toolBar.add(controlPrint.getPageSetupButton());
        toolBar.add(controlPrint.getPrintButton());
        toolBar.add(controlPrint.getPrintPreviewButton());
      
        toolBar.add(getPdfCommand());

        toolBar.addSeparator();
        toolBar.add(Box.createHorizontalStrut(4));
        
        OAHTMLTextPaneController controlEditor = getDetailTextPane().getController();
        toolBar.add(controlEditor.getFontNameComboBox());
        toolBar.add(controlEditor.getFontSizeComboBox());
        toolBar.add(Box.createHorizontalStrut(4));
        toolBar.add(controlEditor.getBoldToggleButton());
        toolBar.add(controlEditor.getItalicToggleButton());
        toolBar.add(controlEditor.getUnderlineToggleButton());
        toolBar.add(Box.createHorizontalStrut(4));
        toolBar.add(controlEditor.getFontColorButton());
        toolBar.add(controlEditor.getBackgroundColorButton());
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(controlEditor.getEditSourceButton());

        return toolBar;
    }

    public PrintController getPrintController() {
        if (controlPrint != null) return controlPrint;
        controlPrint = new PrintController(JfcDelegate.getMainWindow()) {
            @Override
            public void setPrintable(Printable printable) {
                super.setPrintable(printable);
                boolean b = (printable != null);
                
                getPdfCommand().setEnabled(b);
                getPrintController().getPrintButton().setEnabled(b);
                getPrintController().getPrintMenuItem().setEnabled(b);
                getPrintController().getPrintPreviewButton().setEnabled(b);
                getPrintController().getPrintPreviewMenuItem().setEnabled(b);
                getPrintController().getPageSetupMenuItem().setEnabled(b);
                getPrintController().getSaveAsPdfMenuItem().setEnabled(b);
            }
            @Override
            public void setPageFormat(PageFormat pf) {
                if (pf != getPageFormat()) {
                    super.setPageFormat(pf);
                }
                if (pf !=  ApplicationReport.this.getPageFormat()) {
                    ApplicationReport.this.setPageFormat(pf);
                    ApplicationReport.this.savePageFormat();
                }
            }
            @Override
            protected void beforePreview() {
                setOnHold(true);
                super.beforePreview();
            }
            @Override
            protected void afterPreview() {
                setOnHold(false);
                super.afterPreview();
            }
            @Override
            protected void beforePrint() {
                setOnHold(true);
                super.beforePrint();
            }
            @Override
            protected void afterPrint() {
                setOnHold(false);
                super.afterPrint();
            }
        };
        controlPrint.setPrintable(this);
        return controlPrint;
    }
    
    protected JFileChooser getFileChooser() {
        if (fileChooser != null) return fileChooser;
        
        fileChooser = new JFileChooser() {
            public @Override void approveSelection() {
                File f = getSelectedFile();
                String fileName = f.getPath();
                if (!fileName.endsWith(".pdf")) {
                    if (fileName.indexOf('.') < 0) {
                        fileName += ".pdf";
                        f = new File(fileName);
                        setSelectedFile(f);
                    }
                    else {
                        JOptionPane.showMessageDialog(getTopLevelAncestor(),
                                "File name must end in \".pdf\"",
                                Resource.getValue(Resource.APP_ApplicationName),
                                JOptionPane.ERROR_MESSAGE);                                    
                        return;
                    }
                }

                
                if (f.exists() && getDialogType() == SAVE_DIALOG) {
                    int result = JOptionPane.showConfirmDialog(getTopLevelAncestor(),
                            "The selected file already exists. " +
                            "Do you want to overwrite it?",
                            "The file already exists",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    switch(result)  {
                    case JOptionPane.YES_OPTION:
                        break;
                    case JOptionPane.NO_OPTION:
                        return;
                    case JOptionPane.CANCEL_OPTION:
                        cancelSelection();
                        return;
                    }
                }
                Resource.setValue(Resource.TYPE_Client, PrefixName+"_PdfFileName", fileName);
                super.approveSelection();
            }
        };
        
        javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileFilter() {  // controls files that can be selected
            public boolean accept(File f) {
                if ( f.getName().toLowerCase().endsWith(".pdf") ) {
                    return true;
                }
                if ( f.isDirectory() ) return true;
                return false;
            }
            public String getDescription() {
                return "*.pdf";
            }
        };
        
        String fname = Resource.getValue(PrefixName+"PdfFileName", PrefixName+"1.pdf");
        if (!OAString.isEmpty(fname)) {
            fileChooser.setSelectedFile(new File(fname));
        }
        
        fileChooser.setFileFilter(filter);
        fileChooser.setDialogTitle(Resource.getValue(Resource.APP_ApplicationName));
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setFileHidingEnabled(false);
        // fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setAcceptAllFileFilterUsed(false);

        return fileChooser;
    }

    public JButton getPdfCommand() {
        if (cmdPdf != null) return cmdPdf;
        cmdPdf = new JButton("Save as Pdf ...");
        cmdPdf.setIcon(Resource.getJarIcon(Resource.getValue(Resource.IMG_Pdf)));
        cmdPdf.setToolTipText("Save as a Pdf a file.");
        //cmdPdf.setActionCommand(CMD_SaveAsPdf);
        cmdPdf.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSaveAsPdf();
            }
        });
        OAButton.setup(cmdPdf);
        return cmdPdf;
    }

    private final AtomicInteger aiOnHold = new AtomicInteger();
    /**
     * Called while report is outputting, so that changes (ex: active object) can wait.
     */
    protected void setOnHold(boolean b) {
        if (!b) {
            if (aiOnHold.decrementAndGet() == 0) {
                getHub().setAO(hubOriginal.getAO());
            }
        }
        else aiOnHold.incrementAndGet();
    }
    public boolean isOnHold() {
        return aiOnHold.get() > 0;
    }
    
    public void onSaveAsPdf() {
        setOnHold(true);
        _onSaveAsPdf();
    }
    
    protected void _onSaveAsPdf() {
        /*qqqqqq
        final User user = hub.getAO();
        if (user == null) return;
        final String userName = user == null ? "" : user.getFullName();
        */
        
        int x = getFileChooser().showSaveDialog(JfcDelegate.getMainWindow());
        if (x != JFileChooser.APPROVE_OPTION) return;
        final File file = getFileChooser().getSelectedFile();
        final String fileName = file.getPath();

        try {
            file.createNewFile();
        }
        catch (Exception e) {
            setOnHold(false);
            String s = "";
            for (int i=0; i<fileName.length(); i++) {
                char ch = fileName.charAt(i);
                if (Character.isDigit(ch) || Character.isLetter(ch)) continue;
                if ("\\ _-.".indexOf(ch) >= 0) continue;
                if (ch == ':' && i == 1) continue;
                if (s.length() == 0) s = "\nThe following could be the bad characters: ";
                s += ch + " ";
            }
            JOptionPane.showMessageDialog(JfcDelegate.getMainWindow(), 
                    "Invalid/bad file name: "+fileName+"\nPlease remove any characters that are not valid,\nand try again."+s, 
                    "Save as Pdf", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        
        SwingWorker<Boolean, String> sw = new SwingWorker<Boolean, String>() {
            boolean bValid;
            Exception ex;
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    getPdfController().saveToPdf(getPrintController().getPrintable(), getPrintController().getPageFormat(), fileName, "OAAppStore", "");
                    bValid = true;
                }
                catch (Exception e) {
                    bValid = false;
                    ex = e;
                    LOG.log(Level.WARNING, "Exception calling saveToPdf", e);
                }
                return true;
            }
           
            @Override
            protected void done() {
                try {
                    _done();
                }
                finally {
                    setOnHold(false);
                }
            }
            protected void _done() {
                setStatus("");
                setProcessing(false);
                if (!bValid) {
                    JOptionPane.showMessageDialog(JfcDelegate.getMainWindow(), 
                            "Could not save file\n"+ex.getMessage()+"\nPlease verify that a valid file name is used.", 
                            "Save as Pdf", JOptionPane.ERROR_MESSAGE);
                }
                else {
                    try {
                        URL url = getClass().getResource(Resource.getJarImageDirectory() + "/" + Resource.getValue(Resource.IMG_PdfBig));
                        ImageIcon icon = new ImageIcon(url);
                        int x = JOptionPane.showConfirmDialog(JfcDelegate.getMainWindow(), " saved as Pdf file \""+ file.getName()+"\"\nWould you like to view the Pdf document?", Resource.getValue(Resource.APP_ApplicationName), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, icon);
                        if (x == JOptionPane.YES_OPTION) {
                            Desktop.getDesktop().open(file);
                        }
                    }
                    catch (Exception e) {
                        LOG.log(Level.WARNING, "Error saving Order as Pdf file", e);
                }
                }
            }
        };
        setStatus("Saving as Pdf file \""+ file.getName()+"\" ...");
        setProcessing(true, "Saving ...");
        sw.execute();
    }

    public void setStatus(String msg) {
    }
    public void setProcessing(boolean b) {
        setProcessing(b, null);
    }
    public void setProcessing(boolean b, String msg) {
    }
    public PdfController getPdfController() {
        if (controlPdf != null) return controlPdf;
        controlPdf = new PdfController();
        return controlPdf;
    }

    private long msLastReload;
    @Override
    public void refreshDetail() {
        if (isOnHold()) return;
        if (getDetailTemplate().getHasParseError()) {
            long ms = System.currentTimeMillis();
            if (msLastReload == 0 || msLastReload+5000 < ms) {
                loadDefaultHtmlFiles();
                msLastReload = ms;
            }
        }
        getDetailTemplate().stopProcessing();

        F oaObj = getHub().getAO();
        setObject(oaObj);
        super.refreshDetail();
    }

    public void setEnabled(boolean b) {
        boolean bHold = bEnabled;
        bEnabled = b;
        if (b && !bHold && !isOnHold()) {
            if (jfcController == null || jfcController.isVisibleOnScreen()) {
                refreshDetail();
            }
        }
    }
    public boolean getEnabled() {
        return this.bEnabled;
    }
    
    @Override
    public void setPageFormat(PageFormat pf) {
        super.setPageFormat(pf);
        getPrintController().setPageFormat(pf);
    }
}
