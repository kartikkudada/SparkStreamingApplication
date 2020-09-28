# Spark Streaming Application
Spark Streaming Application integration with Kafka

In this Spark Streaming Application , Stock data has been fetched from kafka topic and aggregation is donw on Java Streaming RDDs .
I used spark's Window operations on this also .

output of this applicatioin is captured in output.txt, every 10 mins window is used for aggregation and batch interval is 1 mins for creating JavaDsStream
