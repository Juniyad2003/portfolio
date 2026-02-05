package com.finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PortfolioApplication {

    public static void main(String[] args) {

        System.out.println("SpringBoot Will Start Now....");
        SpringApplication.run(PortfolioApplication.class, args);
        System.out.println("FinTrack Running Successfully...");
    }

}