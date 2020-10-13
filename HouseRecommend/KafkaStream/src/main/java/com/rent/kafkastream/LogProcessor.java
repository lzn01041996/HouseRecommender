package com.rent.kafkastream;


import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/9/1

*/
public class LogProcessor implements Processor<byte[],byte[]> {

    private ProcessorContext context;
    @Override
    public void init(ProcessorContext processorContext) {
        this.context = processorContext;
    }

    @Override
    public void process(byte[] dummy, byte[] line) {
        //把收集到的日志信息用String表示
        String input = new String(line);
        //根据前缀HOUSE_BROWSE_PREFIX:从日志信息中提取浏览数据
        if (input.contains("HOUSE_BROWSE_PREFIX:")){
            System.out.println("house rating data coming!>>>>>>>>>>>>>>>>>>>>>" + input);
            input = input.split("HOUSE_BROWSE_PREFIX:")[1].trim();
            context.forward( "logProcessor".getBytes(), input.getBytes());
        }
    }

    @Override
    public void punctuate(long l) {

    }

    @Override
    public void close() {

    }
}
