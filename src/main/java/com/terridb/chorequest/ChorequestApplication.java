package com.terridb.chorequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class ChorequestApplication {

    static void main(String[] args) {
        // Timestamps (createdAt, completedAt, ...) are stored as UTC regardless of where the server runs
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(ChorequestApplication.class, args);
    }

}
