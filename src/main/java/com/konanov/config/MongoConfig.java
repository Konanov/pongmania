//package com.konanov.config;
//
//import com.mongodb.MongoClient;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.MainConfig;
//
//@MainConfig
//public class MongoConfig {
//
//    @Value("${spring.data.mongodb.host}")
//    private String mongoHost;
//
//    @Value("${spring.data.mongodb.port}")
//    private String mongoPort;
//
//    @Bean
//    public MongoClient mongoClient() {
//        return new MongoClient(mongoHost + ":" + mongoPort);
//    }
//}
