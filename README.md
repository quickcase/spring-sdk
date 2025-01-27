# spring-sdk
Development kit to build QuickCase-flavoured JDK applications.

Supported JVM versions:
- 21

## Documentation

### Field path extraction

Extract the value of a metadata or field from the given record using field path notation.

```java
var record = new Record();

var extractor = new RecordExtractor(record);

// Extracting metadata
extractor.extract('[workspace]');
extractor.extract('[type]');
extractor.extract('[state]');
extractor.extract('[id]');
extractor.extract('[classification]');
extractor.extract('[created]');
extractor.extract('[modified]');

// Extracting data field
extractor.extract('field1');
extractor.extract('level1.level2.nestedField');

// Extracting from collection items
extractor.extract('collectionField[0].value'); // By item index, zero-based
extractor.extract('collectionField[id:abc123].value'); // By item ID
```