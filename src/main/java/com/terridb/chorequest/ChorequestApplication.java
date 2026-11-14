package com.terridb.chorequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class ChorequestApplication {

    static void main(String[] args) {
        // Timestamps (createdAt, completedAt, ...) are stored as UTC regardless of where the server runs
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(ChorequestApplication.class, args);
    }

}
