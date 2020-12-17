package com.rent.foodie.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/12/16

*/

@Configuration
public class WebSocketConfiguration {
    @Bean
    public ServerEndpointExporter ServerEndpointExporter(){
        return new ServerEndpointExporter();
    }


}
