package com.rent.kafkastream;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.processor.TopologyBuilder;
import org.apache.log4j.Logger;

import java.util.Properties;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/9/1

*/
public class Application {
    public static void main(String[] args) {
        Logger log = Logger.getLogger(Application.class);
        String brokers = "localhost:9092";
        String zookeepers = "localhost:2181";

        //输入和输出的topic
        String from = "house";
        String to = "houserec";

        //定义kafka streaming的配置
        Properties settings = new Properties();
        settings.put(StreamsConfig.APPLICATION_ID_CONFIG,"logFilter");
        settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,brokers);
        settings.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG,zookeepers);

        //创建kafka stream配置对象
        StreamsConfig config = new StreamsConfig(settings);

        //创建一个拓扑构建器
        TopologyBuilder builder = new TopologyBuilder();

        //定义流处理的拓扑结构
        builder.addSource("SOURCE",from)
                .addProcessor("PROCESSOR",()->new LogProcessor(),"SOURCE")
                .addSink("SINK",to,"PROCESSOR");
        KafkaStreams streams = new KafkaStreams(builder,config);

        streams.start();

        log.info("Kafka stream started!>>>>>>>>>>>>>>>>>>>");
    }
}
