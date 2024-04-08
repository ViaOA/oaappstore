// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.view;

import java.awt.Window;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import com.viaoa.appstore.control.client.FileDialogController;
import com.viaoa.appstore.delegate.JfcDelegate;
import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.util.OADownloadCsv;

public class DownloadDialog<F extends OAObject> {
    private Hub<F> hub;
    private Window window;
    private File directory;
    private JFileChooser fileChooser; 
    private final OADownloadCsv<F> downloadCsv;
    private volatile PrintWriter writer;
    
    
    public DownloadDialog(Hub<F> hub) {
        this.window = JfcDelegate.getMainWindow();
        this.hub = hub;
        this.downloadCsv = new OADownloadCsv<F>(hub) {
            @Override
            protected void onWriteLine(String txt) {
                if (writer != null) writer.println(txt);
            }
        };
    }
    public void setDirectory(File file) {
        this.directory = file;
    }
    
    public void addProperty(String title, String propPath) {
        downloadCsv.addProperty(title, propPath);
    }

    public void download() {
        if (fileChooser == null) {
            fileChooser = new FileDialogController().getCsvFileChooser();
            if (directory != null) {
                fileChooser.setCurrentDirectory(directory);
            }
        }
        
        int x = fileChooser.showSaveDialog(window);
        if (x != JFileChooser.APPROVE_OPTION) return;
        final File file = fileChooser.getSelectedFile();
        final String fileName = file.getPath();

        try {
            file.createNewFile();
        }
        catch (Exception e) {
            String s = "";
            for (int i = 0; i < fileName.length(); i++) {
                char ch = fileName.charAt(i);
                if (Character.isDigit(ch) || Character.isLetter(ch)) continue;
                if ("\\ _-.".indexOf(ch) >= 0) continue;
                if (ch == ':' && i == 1) continue;
                if (s.length() == 0) s = "\nThe following could be the bad characters: ";
                s += ch + " ";
            }
            JOptionPane.showMessageDialog(window, "Invalid/bad file name: " + fileName
                    + "\nPlease remove any characters that are not valid,\nand try again." + s, "Save as CSV", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingWorker<Boolean, String> sw = new SwingWorker<Boolean, String>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                FileOutputStream fos = new FileOutputStream(file);
                OutputStreamWriter os = new OutputStreamWriter(fos);
                DownloadDialog.this.writer = new PrintWriter(os);
                
                downloadCsv.download();

                DownloadDialog.this.writer.close();
                DownloadDialog.this.writer = null;
                return true;
            }

            @Override
            protected void done() {
                try {
                    Object objx = get();
                    JOptionPane.showMessageDialog(window, "CSV file " + fileName + " created", "Save as CSV", JOptionPane.INFORMATION_MESSAGE);
                }
                catch (Exception e) {
                    JOptionPane.showMessageDialog(window, "Error while creating csv file " + fileName, "Save as CSV\n"+e.getMessage(), JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        sw.execute();
    }
        
    
}
