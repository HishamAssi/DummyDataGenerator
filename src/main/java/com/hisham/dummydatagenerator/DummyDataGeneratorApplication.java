package com.hisham.dummydatagenerator;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.LocalDateTime;

@EnableJpaRepositories(basePackages = {"com.hisham.dummydatagenerator"})

@SpringBootApplication
public class DummyDataGeneratorApplication {

	@PostConstruct
	public static void printStartupVersion() {
		System.out.println("=== DummyDataGenerator v0.1 Booted at " + LocalDateTime.now() + " ===");
	}


	public static void main(String[] args) {
		printStartupVersion();
		SpringApplication.run(DummyDataGeneratorApplication.class, args);
	}

}
