//package com.mmtext.supplierpollingservice.config;
//import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
//import org.springframework.boot.ssl.SslBundles;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.config.TopicBuilder;
//import org.springframework.kafka.core.KafkaAdmin;
//import org.apache.kafka.clients.admin.NewTopic;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Configuration
//public class KafkaTopicConfig {
//
//    @Bean
//    public KafkaAdmin kafkaAdmin(KafkaProperties kafkaProperties, SslBundles sslBundles) {
//        Map<String, Object> configs = new HashMap<>(kafkaProperties.buildAdminProperties(sslBundles));
//        return new KafkaAdmin(configs);
//    }
//
//    @Bean
//    public NewTopic inventoryNormalizedTopic() {
//        return TopicBuilder.name("normalize.hotel_info")
//                .partitions(10)        // **set number of partitions**
//                .replicas(3)           // **set replication factor**
//                .build();
//    }
//
//    @Bean
//    public NewTopic supplierUpdatesTopic() {
//        return TopicBuilder.name("supplier.updates")
//                .partitions(5)
//                .replicas(2)
//                .build();
//    }
//}
