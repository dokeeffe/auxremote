package com.bobs;

import jssc.SerialPort;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Configuration
@EnableAsync
@EnableScheduling
public class NexmodApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexmodApplication.class, args);
    }

}
