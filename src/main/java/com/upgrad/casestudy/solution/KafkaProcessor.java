package com.upgrad.casestudy.solution;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

public class KafkaProcessor {
	
	private static KafkaProcessor kafkaProcessor;
	private Map props = new HashMap();
	Collection<String> topics;
public static	KafkaProcessor getInstance(){
	       if(kafkaProcessor == null)
		      kafkaProcessor = new KafkaProcessor();
		return kafkaProcessor;
	}
	
	public  KafkaProcessor(){
/*		props.put("bootstrap.servers", "52.55.237.11:9092");
		props.put("group.id", "test-18");
		props.put("enable.auto.commit", "true");
		props.put("auto.offset.reset", "latest");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		topics = Arrays.asList("stockData");*/
	}

	public void configWithParam(String kafkaBrokerId,String topicName,String groupID) {
		props.put("bootstrap.servers", kafkaBrokerId+":9092");
		props.put("group.id", groupID);
		props.put("enable.auto.commit", "true");
		props.put("auto.offset.reset", "latest");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		topics = Arrays.asList(topicName);
		System.out.println("Configuration is done");
	}
	
	public JavaInputDStream<ConsumerRecord<String, String>> createInputStream(JavaStreamingContext streamingContext){
		return KafkaUtils.createDirectStream(streamingContext,
				LocationStrategies.PreferConsistent(), ConsumerStrategies.<String, String>Subscribe(topics, props));
	}
}
