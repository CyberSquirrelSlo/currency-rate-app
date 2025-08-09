package com.example.currency;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class CurrencyApplication {

    public static void main(String[] args) {
        SpringApplication.run(CurrencyApplication.class, args);
    }

    @Bean
    CommandLineRunner importRatesOnStartup() {
        return args -> {
            try {
                RestTemplate restTemplate = new RestTemplate();
                String url = "http://localhost:8081/api/rates/import";
                restTemplate.getForObject(url, String.class);
                System.out.println("✅ Rates imported successfully on startup!");
            } catch (Exception e) {
                System.err.println("❌ Failed to import rates on startup: " + e.getMessage());
            }
        };
    }
}
