// Copied from OATemplate project by OABuilder 03/12/24 01:50 PM
package com.viaoa.appstore.control.client;

import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.viaoa.appstore.resource.Resource;
import com.viaoa.util.OAFile;

public class FileDialogController {
    
    private JFileChooser fileChooserPdf;
    private JFileChooser fileChooserCsv;

    public FileDialogController() {
        
    }
    
    public void init() {
        getPdfFileChooser();  
    }
    
    public JFileChooser getPdfFileChooser() {
        if (fileChooserPdf == null) {
            fileChooserPdf = new JFileChooser() {
                public @Override void approveSelection() {
                    File f = getSelectedFile();
                    String fileName = f.getPath();
                    if (!fileName.toLowerCase().endsWith(".pdf")) {
                        if (fileName.indexOf('.') < 0) {
                            fileName += ".pdf";
                            f = new File(fileName);
                            setSelectedFile(f);
                        }
                        else {
                            JOptionPane.showMessageDialog(getTopLevelAncestor(),
                                    "File name must end in \".pdf\"",
                                    Resource.getValue(Resource.APP_ClientApplicationName),
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
            
            fileChooserPdf.setFileFilter(filter);
            fileChooserPdf.setDialogTitle(Resource.getValue(Resource.APP_ClientApplicationName));
            fileChooserPdf.setDialogType(JFileChooser.SAVE_DIALOG);
            fileChooserPdf.setFileHidingEnabled(false);
            // fileChooserPdf.setFileSelectionMode(JfileChooserPdf.FILES_AND_DIRECTORIES);
            fileChooserPdf.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooserPdf.setMultiSelectionEnabled(false);
            fileChooserPdf.setAcceptAllFileFilterUsed(false);
        }
        return fileChooserPdf;
    }

    public JFileChooser getCsvFileChooser() {
        if (fileChooserCsv != null) return fileChooserCsv;
            
        fileChooserCsv = new JFileChooser(OAFile.convertFileName(Resource.getRootDirectory())) {
            public @Override void approveSelection() {
                File f = getSelectedFile();
                String fileName = f.getPath();
                if (!fileName.toLowerCase().endsWith(".csv")) {
                    if (fileName.indexOf('.') < 0) {
                        fileName += ".csv";
                        f = new File(fileName);
                        setSelectedFile(f);
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
                super.approveSelection();
            }
        };
            
        javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileFilter() {  // controls files that can be selected
            public boolean accept(File f) {
                if ( f.getName().toLowerCase().endsWith(".csv") ) {
                    return true;
                }
                if ( f.isDirectory() ) return true;
                return false;
            }
            public String getDescription() {
                return "*.csv";
            }
        };

        fileChooserCsv.setFileFilter(filter);
        
        fileChooserCsv.setDialogTitle("CSV file");
        fileChooserCsv.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooserCsv.setFileHidingEnabled(false);
        // fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooserCsv.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooserCsv.setMultiSelectionEnabled(false);
        fileChooserCsv.setAcceptAllFileFilterUsed(false);

        return fileChooserCsv;
    }
    
    
    
    public static void main(String[] args) {
        String[] fileNameExtensions = ImageIO.getReaderFileSuffixes();
        fileNameExtensions = ImageIO.getWriterFileSuffixes();
        JFrame frm = new JFrame();
        frm.setVisible(true);
        FileDialogController fdc = new FileDialogController();
        JFileChooser fc = fdc.getCsvFileChooser();
        
        int x = fc.showSaveDialog(frm);
        if (x != JFileChooser.APPROVE_OPTION) return;
        File file = fc.getSelectedFile();
        String fileName = file.getPath();
        
        System.out.println("==> "+fileName);
    }
}
