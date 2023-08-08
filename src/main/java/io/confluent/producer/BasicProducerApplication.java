package io.confluent.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@ComponentScan({"io.confluent.datasource", "io.confluent.producer"})
public class BasicProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BasicProducerApplication.class, args);
    }
}
