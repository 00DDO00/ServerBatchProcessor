# ServerBatchProcessor

Takes in XML files, converts them to JSON, groups them according to their Data Types and stores them in batches of 100 respectively.

1. Run XMLToJSONServer.java -> Listens to incoming requests made to 9091.
2. Run BatchProcessor_v2.java -> Groups records according to Data Types and fills up batches of 100 respectively.
3. Run DummyXMLClient.java -> Generates dummy XML files and send them to 9091.

Logs folder includes JSON records grouped into Data Types.
Batches folder includes the JSON records in groups of 100.
When a batch gets to 100 records, a new batch gets created to then write the next 100 records into.
