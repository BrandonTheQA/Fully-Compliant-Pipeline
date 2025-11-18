package com.example.ecompoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class EcompocApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcompocApplication.class, args);
    }
}

