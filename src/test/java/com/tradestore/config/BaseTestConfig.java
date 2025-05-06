package com.tradestore.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.TestSocketUtils;

@TestConfiguration
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.main.allow-bean-definition-overriding=true",
    "spring.jpa.show-sql=true",
    "spring.jpa.properties.hibernate.format_sql=true",
    "springdoc.api-docs.enabled=false",
    "springdoc.swagger-ui.enabled=false"
})
public class BaseTestConfig {

    protected static int findAvailablePort() {
        return TestSocketUtils.findAvailableTcpPort();
    }
} 