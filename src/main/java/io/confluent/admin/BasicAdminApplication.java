package io.confluent.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;

@SpringBootApplication
public class BasicAdminApplication {

    // TODO spring.main.web-application-type: none

    public static void main(String[] args) {
        SpringApplication.run(BasicAdminApplication.class, args);
    }
}
