package org.dicom4j;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.image.BufferedImageUtils;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
     * convert to image
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
            log.error("convert to image error [{}]", ex);
        } finally {
            try {
                ios.close();
                iis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * convert to json
     *
     * @param fileName dest json file name
     */
    public void toJson(String fileName) {
        OutputStream os = null;
        DicomInputStream dis = null;
        Map<String, ?> conf = new HashMap<>(2);
        conf.put(JsonGenerator.PRETTY_PRINTING, null);
        try {
            os = new FileOutputStream(fileName);
            dis = new DicomInputStream(srcFile);
            // TODO
            dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
            JsonGenerator jsonGen = Json.createGeneratorFactory(conf).createGenerator(os);
            JSONWriter jsonWriter = new JSONWriter(jsonGen);
            dis.setDicomInputHandler(jsonWriter);
            dis.readDataset(-1, -1);
            jsonGen.flush();
        } catch (IOException ex) {
            log.error("convert to json error [{}]", ex);
        } finally {
            try {
                dis.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * convert to pdf
     *
     * @param fileName dest pdf file name
     */
    public void toPdf(String fileName) {
        File destFile = new File(fileName);
        try {
            DicomInputStream dis = new DicomInputStream(srcFile);
            Attributes attributes = dis.readDataset(-1, -1);
            String sopCUID = attributes.getString(Tag.SOPClassUID);
            if (!sopCUID.equals(UID.EncapsulatedPDFStorage)) {
                log.info("DICOM file {} with {} SOP Class cannot be converted to file type PDF",
                        srcFile, UID.nameOf(sopCUID));
                return;
            }
            FileOutputStream fos = new FileOutputStream(destFile);
            byte[] value = (byte[]) attributes.getValue(Tag.EncapsulatedDocument);
            fos.write(value);
        } catch (IOException ex) {
            log.error("convert to PDF error [{}]", ex);
        }
    }
}
