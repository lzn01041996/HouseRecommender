package com.rent.foodie.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import redis.clients.jedis.Jedis;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/9/2

*/
@Configuration
public class Config {
    private String mongoHost;
    private int mongoPort;
    private String esClusterName;
    private String esHost;
    private int esPort;
    private String redisHost;

    public Config(){
        try {
            Properties properties = new Properties();
            Resource resource = new ClassPathResource("recommend.properties");
            properties.load(new FileInputStream(resource.getFile()));
            this.mongoHost = properties.getProperty("mongo.host");
            this.mongoPort = Integer.parseInt(properties.getProperty("mongo.port"));
            this.esClusterName = properties.getProperty("es.cluster.name");
            this.esHost = properties.getProperty("es.host");
            this.esPort = Integer.parseInt(properties.getProperty("es.port"));
            this.redisHost = properties.getProperty("redis.host");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Bean(name = "jedis")
    public Jedis getRedisClient() {
        Jedis jedis = new Jedis(redisHost);
        return jedis;
    }
}
