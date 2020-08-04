package org.dicom4j;


import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Representation of one DICOM file
 *
 * @author chenxc
 * @date 2020/08/04.
 */
public class DicomFile {

    public final static Logger log = LoggerFactory.getLogger(DicomFile.class);

    public DicomFile() {
        dataset = new Attributes();
    }

    /**
     * Gets the DICOM dataset of the file.
     */
    public Attributes dataset;

    /***
     * Open dicom file
     *
     * @param file DICOM file
     * @return DicomFile instance
     */
    public static DicomFile open(File file) {
        DicomFile df = new DicomFile();
        if (!file.exists()) {
            log.error("file is not exists");
        }
        if (!isDcm(file)) {
            log.error("File format not supported");
        }
        try {
            DicomInputStream dis = new DicomInputStream(file);
            dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
            df.dataset = dis.readDataset(-1, -1);
            dis.close();
        } catch (IOException ex) {
            log.error("reading dicom file error [{}]", ex);
        }
        return df;
    }

    /***
     * Open dicom file
     *
     * @param fileName The filename of the DICOM file
     * @return DicomFile instance
     */
    public static DicomFile open(String fileName) {
        return open(new File(fileName));
    }

    /**
     * Is it a DICOM file
     *
     * @param file DICOM file
     * @return
     */
    public static boolean isDcm(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] tag = new byte[]{'D', 'I', 'C', 'M'};
            byte[] buffer = new byte[4];
            if (128 != fis.skip(128)) {
                return false;
            }
            if (4 != fis.read(buffer)) {
                return false;
            }
            fis.close();
            for (int i = 0; i < 4; i++) {
                if (buffer[i] != tag[i]) {
                    return false;
                }
            }
        } catch (IOException ex) {
            log.warn("error reading file error [{}]", ex);
            return false;
        }
        return true;
    }
}
