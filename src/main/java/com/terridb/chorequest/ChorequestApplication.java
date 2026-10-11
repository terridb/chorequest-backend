package com.terridb.chorequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChorequestApplication {

    static void main(String[] args) {
        SpringApplication.run(ChorequestApplication.class, args);
    }

}
