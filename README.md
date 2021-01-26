# Spark Streaming Application for finding avg closing price of stocks and total purchase/sale volume in 10 mins 

Spark Streaming Application integration with Kafka

In this Spark Streaming Application , Stock data has been fetched in JSON format from kafka topic and average closing price for each stock is calcualted on Java pair Dsstream.

Below are stock anaylysis cases 
1. I used reduceByKeyAndWindow function to with window duration of 10 mins and sliding window of 5 mins to calculate average closing price and  difference of average closing price and average opening price. This is calculated on per stock basis .  

2. I used reduceByKeyAndWindow function on Java pair Dstream with window duration of 10 mins and sliding window of 10 mins to calcualted total purchase/sale volume of stocks on every 10 mins.

output of this applicatioin is captured in output.txt batch interval is 1 mins.
