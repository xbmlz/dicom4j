package org.dicom4j;

import org.dcm4che3.image.BufferedImageUtils;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReadParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * DICOM converter
 *
 * @author chenxc
 * @date 2020/08/04.
 */
public class DicomConvert {

    private final static Logger log = LoggerFactory.getLogger(DicomConvert.class);

    /**
     * source file
     */
    private File srcFile;

    public DicomConvert(String fileName) {
        this.srcFile = new File(fileName);
    }

    /**
     * transfer to image
     *
     * @param fileName dest file name
     */
    public void toImage(String fileName) {
        ImageInputStream iis = null;
        ImageOutputStream ios = null;
        try {
            File destFile = new File(fileName);
            ImageReader imageReader = ImageIO.getImageReadersByFormatName("DICOM").next();
            iis = ImageIO.createImageInputStream(srcFile);
            imageReader.setInput(iis);
            DicomImageReadParam param = (DicomImageReadParam) imageReader.getDefaultReadParam();
            // TODO edit tag ...
            BufferedImage bi = imageReader.read(0, param);
            ColorModel cm = bi.getColorModel();
            if (cm.getNumComponents() == 3) {
                BufferedImageUtils.convertToIntRGB(bi);
            }
            destFile.delete();
            ios = ImageIO.createImageOutputStream(destFile);
            String formatName = fileName.substring(fileName.lastIndexOf(".") + 1);
            Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByFormatName(formatName);
            ImageWriter imageWriter = imageWriters.next();
            ImageWriteParam imageWriteParam = imageWriter.getDefaultWriteParam();
            // TODO width height ...
            imageWriter.setOutput(ios);
            imageWriter.write(null, new IIOImage(bi, null, null), imageWriteParam);
        } catch (IOException ex) {
            ex.printStackTrace();
            log.error("convert error [{}]", ex);
        } finally {
            try {
                ios.close();
                iis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
