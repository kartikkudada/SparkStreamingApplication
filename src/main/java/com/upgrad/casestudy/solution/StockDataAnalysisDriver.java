package com.upgrad.casestudy.solution;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.log4j.Level;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import scala.Tuple2;

public class StockDataAnalysisDriver implements Serializable{


	private static final long serialVersionUID = -4143435243470101718L;


	public static void main(String[] args) throws InterruptedException {
		StockDataAnalysisDriver stockDataAnalysis = new StockDataAnalysisDriver();
		String kafkaBrokerId = null;
		String topicName = null;
		String groupID = null;
		KafkaProcessor kafkaProcessor = KafkaProcessor.getInstance();
		if(args != null && args.length == 3)
			{
			
			 kafkaBrokerId = args[0];groupID = args[1] ; topicName = args[2];
			 System.out.println(kafkaBrokerId + " " + topicName + " " + groupID);
			 kafkaProcessor.configWithParam(kafkaBrokerId.trim(), topicName.trim(),groupID.trim());
			}
		else throw new IllegalArgumentException("Arugument not passed");
		
		Map props = new HashMap();
		SparkConf conf = new SparkConf().setAppName("Stock Analylsis").setMaster("local[*]");
		JavaStreamingContext streamingContext = new JavaStreamingContext(conf, Durations.minutes(1));
		streamingContext.sparkContext().setLogLevel(Level.WARN.toString());
		streamingContext.checkpoint("solution_1_dir");
		/*JavaInputDStream<ConsumerRecord<String, String>> stream = KafkaUtils.createDirectStream(streamingContext,
				LocationStrategies.PreferConsistent(), ConsumerStrategies.<String, String>Subscribe(topics, props));*/
        
		// Received data as inputstream from topic  
		JavaInputDStream<ConsumerRecord<String, String>> stream = kafkaProcessor.createInputStream(streamingContext);
        
		// Data converted to Java Object using JSON parsing
		JavaPairDStream<String, CryptoCurrency> pairDstream =  stockDataAnalysis.getCryptoCurrencyAsDStream(stream);
		

		JavaPairDStream<String, Tuple2<StockOpenClose,Double>>	openingPriceBySymbol_pair = pairDstream.mapToPair( c -> new Tuple2(c._1,new Tuple2(new StockOpenClose(c._2.getPriceData().getOpen(),c._2.getPriceData().getClose()),1D)));
		//sample DATA WILL BE  BTC - StockOpenClose(1010 1023) 1   
		//stockOpenCloseSumDStream = openingPriceBySymbol_pair.reduceByKeyAndWindow((x, y) -> new Tuple2( new  StockOpenClose(x._1.getOpen() + y._1().getOpen(),x._1().getClose() + y._1().getClose()), x._2() + y._2()), Durations.minutes(4),Durations.minutes(2));
		// with summary and inverse summary 
		JavaPairDStream<String, Tuple2<StockOpenClose, Double>> stockOpenCloseSumDStream =  stockDataAnalysis.fetchStockOpenCloseDataSumDsStream(openingPriceBySymbol_pair);
			
		JavaPairDStream<String,Double> avgOpencloseDiffDStream = stockOpenCloseSumDStream.mapToPair(
				record -> {
					String symbol = record._1();
					Double avgOpenPrice = record._2()._1().getOpen()/record._2()._2();
					Double avgClosePrice = record._2()._1().getClose()/record._2()._2();
					return new Tuple2<String,Double>(symbol,(avgClosePrice - avgOpenPrice));
				});
		
		JavaPairDStream<String,Double> avgCloseDStream = stockOpenCloseSumDStream.mapToPair(
				record -> {
					String symbol = record._1();
					//Double avgOpen = record._2()._1().getOpen()/record._2()._2();
					Double avgClose = record._2()._1().getClose()/record._2()._2();
					return new Tuple2<String,Double>(symbol,(avgClose));
				}
				);
		
		avgCloseDStream.foreachRDD(rdd -> {
					List<Tuple2<String,Double>> list = rdd.collect();
					for(Tuple2<String,Double> tuple :list )
						System.out.println(" Symbol : " + tuple._1() + " Average close price: " + tuple._2());
				});
		
		
		avgOpencloseDiffDStream.foreachRDD(
				rdd -> {
					List<Tuple2<String,Double>> list = rdd.collect();
					Double maxProfit = Double.NEGATIVE_INFINITY;
					String mostProfitableShare = "";
					for(Tuple2<String,Double> tuple :list )
					{
						if(tuple._2() > maxProfit)
						{
							maxProfit = tuple._2();
							mostProfitableShare = tuple._1();
						}
						System.out.println(" Symbol : " + tuple._1() + "[Avg close - Avg open ]: " + String.format("%.14f",tuple._2()) );
					}
					System.out.println("Stocks giving maximum profit is " + mostProfitableShare + " : " + String.format("%.14f",maxProfit));
				}
				);
		
		/**
		 * Solution for 3rd problem question
		 */
		JavaPairDStream<String,Double>	sybolWithTotVolumeDstream =  stockDataAnalysis.fetchSymbolwithTotVol(pairDstream); 		
		sybolWithTotVolumeDstream.foreachRDD(
				rdd -> {
					double maxVol = 0; String symboleofMaxVol = "";
					List<Tuple2<String,Double>> rddList = rdd.collect();
				     for(Tuple2<String,Double> tuple:rddList)
				     {
				    	 System.out.println("Symbol: " + tuple._1() + " Absolute  volume: " + tuple._2());
				    	 if(tuple._2() >  maxVol) {
				    		 maxVol = tuple._2; symboleofMaxVol = tuple._1();
				    	 }
				     }
				     System.out.println("Stock to be purchased : " + symboleofMaxVol + "   Absolute volume: " + maxVol);	 
				}
				);	
		
		streamingContext.start();
		streamingContext.awaitTermination();
		streamingContext.close();
	}
	/**
	 * Question no -3
	 * Total volume in every 10 mins
	 * @param pairDstream
	 * @return
	 */
	private JavaPairDStream<String, Double> fetchSymbolwithTotVol(JavaPairDStream<String, CryptoCurrency> pairDstream) {
		// TODO Auto-generated method stub
		JavaPairDStream<String,Double>	symbolWithVolumeDstream =  pairDstream.mapToPair(c -> new Tuple2(c._1,c._2.getPriceData().getVolume()));
		
		JavaPairDStream<String,Double> symbolWithTotVolumeDstream  = symbolWithVolumeDstream.reduceByKeyAndWindow(
					new Function2<Double, Double, Double>(){

						private static final long serialVersionUID = 1L;

						@Override
						public Double call(Double v1, Double v2) throws Exception {
							return (Math.abs(v1)+ Math.abs(v2));
						}
					}
					,
					Durations.minutes(10),Durations.minutes(10));
		return symbolWithTotVolumeDstream;
	}



	/**
	 * Generating DSStream using reduceByKeyAndWindow using summary and inverse summary function
	 * for the Solution of 1st and 2nd problem
	 */
	
	private  JavaPairDStream<String, Tuple2<StockOpenClose, Double>> fetchStockOpenCloseDataSumDsStream(
			JavaPairDStream<String, Tuple2<StockOpenClose, Double>> openingPriceBySymbol_pair) {
		Function2< Tuple2<StockOpenClose,Double>, Tuple2<StockOpenClose,Double> , Tuple2<StockOpenClose,Double>> reduceFunc = new Function2<Tuple2<StockOpenClose,Double>, Tuple2<StockOpenClose,Double>,Tuple2<StockOpenClose,Double>>(){
			@Override
			public Tuple2<StockOpenClose, Double> call(Tuple2<StockOpenClose, Double> present, Tuple2<StockOpenClose, Double> incoming) throws Exception {
				//System.out.println("summary function called");
				return new Tuple2( 
						new StockOpenClose(present._1().getOpen() + incoming._1().getOpen(), present._1().getClose() + incoming._1().getClose()), present._2() + incoming._2() );	
			}							
		};
		Function2< Tuple2<StockOpenClose,Double>, Tuple2<StockOpenClose,Double> , Tuple2<StockOpenClose,Double>> invReduceFunc = new Function2<Tuple2<StockOpenClose,Double>, Tuple2<StockOpenClose,Double>,Tuple2<StockOpenClose,Double>>(){
			@Override
			public Tuple2<StockOpenClose, Double> call(Tuple2<StockOpenClose, Double> present, Tuple2<StockOpenClose, Double> outcoming) throws Exception {
				//System.out.println("Inverse summary function called");
				return new Tuple2( 
						new StockOpenClose(present._1().getOpen() - outcoming._1().getOpen(), present._1().getClose() - outcoming._1().getClose()), present._2() - outcoming._2() );	
			}							
		};
		
		return openingPriceBySymbol_pair.reduceByKeyAndWindow(reduceFunc,invReduceFunc, Durations.minutes(10),Durations.minutes(5));	
	}


	protected JavaPairDStream<String, CryptoCurrency> getCryptoCurrencyAsDStream(JavaInputDStream<ConsumerRecord<String, String>> stream)
	{
		JavaDStream<String> message = stream.map(entry -> entry.value()); message.cache(); message.print();
		return message.mapToPair(new PairFunction<String, String,CryptoCurrency >() {
			private static final long serialVersionUID = 1L;
			@Override
			public Tuple2<String,CryptoCurrency> call(String str) throws Exception {
				// creating mapper object
				ObjectMapper mapper = new ObjectMapper();
				// defining the return type
				TypeReference<CryptoCurrency> mapType = new TypeReference<CryptoCurrency>() {
				};
				// Parsing the JSON String
				CryptoCurrency cryp = mapper.readValue(str, mapType);
				return new Tuple2<String, CryptoCurrency>(cryp.getSymbol(), cryp);
			}
		});
	}
	

}
