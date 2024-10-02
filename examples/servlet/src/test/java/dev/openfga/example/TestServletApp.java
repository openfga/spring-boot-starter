package dev.openfga.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.openfga.OpenFGAContainer;

public class TestServletApp {

    public static void main(String[] args) {
        SpringApplication.from(ServletApp::main)
                .with(TestConfig.class)
                .run(args);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestConfig {

        @Bean
        OpenFGAContainer container(DynamicPropertyRegistry registry) {
            OpenFGAContainer container = new OpenFGAContainer("openfga/openfga:v1.4.3");
            registry.add("openfga.api-url", container::getHttpEndpoint);
            return container;
        }

    }
}
