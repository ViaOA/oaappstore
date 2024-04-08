// Copied from OATemplate project by OABuilder 03/03/24 06:30 AM
package com.viaoa.appstore.control.client;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import com.viaoa.jfc.print.OAPrintable;

public class PdfController {
    private static Logger LOG = Logger.getLogger(PdfController.class.getName());

    private Document document;
    private PdfWriter writer;
    private PdfContentByte contentByte;
    private static DefaultFontMapper mapper;
    private PageFormat pageFormat;
    private OutputStream outputStream;
    private float width, height;

    
    public void saveToPdf(Printable printable, PageFormat pageFormat, String fileName, String title, String userName) throws Exception {
        FileOutputStream fos = new FileOutputStream(fileName);
        saveToPdf(printable, pageFormat, fos, title, userName);
    }

    public void saveToPdf(Printable printable, PageFormat pageFormat, OutputStream outputStream, String title, String userName) throws Exception {
        setup(pageFormat, outputStream, title, userName);
        print(printable);
    }
    
    
    public void setup(PageFormat pageFormat, OutputStream outputStream, String title, String userName) throws Exception {
        this.pageFormat = pageFormat;
        this.outputStream = outputStream;
        
        this.width = (float) pageFormat.getWidth();
        this.height = (float) pageFormat.getHeight();
        
        document = new Document();
        document.setPageSize(new com.lowagie.text.Rectangle(width, height)); // float margin L,R,TB  qqqqqqq might need to add the margins using PageFormat

        writer = PdfWriter.getInstance(document, outputStream);
        
        // must be done before document.open()
        document.addAuthor(userName);
        document.addCreator("ViaOA|Builder");
        document.addCreationDate();
        document.addTitle(title);         
        document.addSubject("");
        document.addProducer(); // itext + version
        document.open();

        contentByte = writer.getDirectContent();
      
    }
    
    public DefaultFontMapper getFontMapper() {
        if (mapper == null) {
            mapper = new DefaultFontMapper() {
                @Override
                public BaseFontParameters getBaseFontParameters(String name) {
                    BaseFontParameters bfp = super.getBaseFontParameters(name);
                    if (bfp != null) bfp.encoding = BaseFont.IDENTITY_H; // utf
                    return bfp;
                }
            };
            mapper.insertDirectory("c:/windows/fonts/");
        }
        return mapper;
    }
    
    
    /** set the background image to use.  It will be scaled to fit the page width/height*/
    public void setBackgroundImage(byte[] bs) throws Exception {
        // add image  http://thinktibits.blogspot.com/2011/04/java-itext-pdf-add-watermark-example.html#.UJcTmMXA8WE
        Image backgroundImage = Image.getInstance(bs);
        //Image backgroundImage = Image.getInstance("cert150dpi.jpg");
        backgroundImage.setAbsolutePosition(0, 0);  // co-ords are diff then Java - bottom/left is 0,0
        backgroundImage.scaleToFit(width, height);
        
        contentByte.addImage(backgroundImage);
    }
    
    public void print(Printable printable) throws Exception {
        
        if (printable instanceof OAPrintable) {
            ((OAPrintable) printable).beforePrint(pageFormat);
        }
        
        BufferedImage bi = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
        Graphics gTemp = bi.getGraphics();
        
        for (int p=0; ;p++) {
            if (p > 0) {
                int x = printable.print(gTemp, pageFormat, p);
                if (x == Printable.NO_SUCH_PAGE) break;
                document.newPage();
            }
            
            Graphics2D g2 = contentByte.createGraphics(width, height, getFontMapper());
            
            int x = printable.print(g2, pageFormat, p);
            g2.dispose();
            if (x == Printable.NO_SUCH_PAGE) break;
        }
        printable.print(null, null, -1); // this is used so that printable will know that the printjob is done.

        document.close();
        outputStream.flush();
        outputStream.close();
        
        if (printable instanceof OAPrintable) {
            ((OAPrintable) printable).afterPrint();
        }
    }
}
