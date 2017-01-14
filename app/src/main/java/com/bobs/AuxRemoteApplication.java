package com.bobs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Configuration
@EnableAsync
@EnableScheduling
public class AuxRemoteApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuxRemoteApplication.class, args);
    }

}
