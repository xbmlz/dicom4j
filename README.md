# dicom4j
Dicom tools based on dcm4che3

### Examples

#### File Operations

```java
// read
DicomFile file = DicomFile.open("test.dcm");
String patientName = file.dataset.getString(Tag.PatientName);

// write
file.dataset.setString(Tag.PatientName, VR.PN, "DOE^JOHN");
file.save("test2.dcm");
```

#### File Convert

```java
// convert to image
DicomConvert convert = new DicomConvert("E:/test.dcm");
convert.toImage("E:/test.jpg");
convert.toImage("E:/test.png");
convert.toImage("E:/test.bmp");
```
