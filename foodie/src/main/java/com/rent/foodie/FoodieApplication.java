package com.rent.foodie;

import com.rent.foodie.wsChat.WebSocketServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;


@SpringBootApplication
//@ComponentScan(basePackages = "redis.clients.jedis")
public class FoodieApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(FoodieApplication.class);
        WebSocketServlet.setApplicationContext(applicationContext);
    }

}
