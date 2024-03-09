package com.nico.sink;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class SaveToDBApplication {
    public static void main(String[] args) {
        SpringApplication.run(SaveToDBApplication.class, args);
    }

}
