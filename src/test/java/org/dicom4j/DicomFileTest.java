package org.dicom4j;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author JAVA
 * @date 2020/08/04.
 */
public class DicomFileTest {

    @Test
    public void testOpen() {
        Assert.assertNotNull(DicomFile.open("E:/test.dcm"));
    }
}
