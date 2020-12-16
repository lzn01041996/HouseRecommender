package com.rent.faqweb;

import com.rent.faqweb.ws.WebSocketServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class FaqwebApplication {

    public static void main(String[] args) {
       ConfigurableApplicationContext applicationContext = SpringApplication.run(FaqwebApplication.class, args);
        WebSocketServlet.setApplicationContext(applicationContext);
    }

}
