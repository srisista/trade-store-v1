package com.tradestore.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestContainersConfig {

    static final MongoDBContainer mongoDBContainer;
    static final PostgreSQLContainer<?> postgreSQLContainer;
    static final KafkaContainer kafkaContainer;

    static {
        mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.0.21"))
                .withReuse(true);
        postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:13-alpine"))
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);
        kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.0"))
                .withReuse(true);

        mongoDBContainer.start();
        postgreSQLContainer.start();
        kafkaContainer.start();
    }

    @Bean
    @ServiceConnection
    public MongoDBContainer mongoDBContainer() {
        return mongoDBContainer;
    }

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgreSQLContainer() {
        return postgreSQLContainer;
    }

    @Bean
    @ServiceConnection
    public KafkaContainer kafkaContainer() {
        return kafkaContainer;
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }
} 