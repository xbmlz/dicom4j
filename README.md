# dicom4j
Dicom tools based on dcm4che3

### Examples

#### File Operations

```java
DicomFile file = DicomFile.open("E:/test.dcm");
String patientName = file.dataset.getString(Tag.PatientName);
```
