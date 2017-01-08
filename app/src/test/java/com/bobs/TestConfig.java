package com.bobs;

import jssc.SerialPort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.Executor;

/**
 * Created by dokeeffe on 26/12/16.
 */
@TestConfiguration
@ComponentScan
@EnableAsync
@EnableScheduling
public class TestConfig {

    @Bean
    public SerialPort serialPort() {
        return new SerialPort("/dev/ttyUSB0");
    }

    @Bean
    public Executor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }
}
