package com.wyc;

import com.github.alturkovic.lock.jdbc.configuration.EnableJdbcDistributedLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Hello world!
 */
@SpringBootApplication
@EnableScheduling
@EnableJdbcDistributedLock
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
