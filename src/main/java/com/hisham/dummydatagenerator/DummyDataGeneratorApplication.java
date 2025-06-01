/**
 * DummyDataGenerator is a Spring Boot application that generates synthetic data for various database systems.
 * It provides a flexible and extensible framework for creating realistic test data across different database platforms.
 *
 * Key Features:
 * - Support for multiple database types through pluggable connectors
 * - Configurable data generation rules
 * - Optional Kafka integration for data streaming
 * - RESTful API for data generation operations
 * - JPA repository support for data persistence
 *
 * @author Hisham
 * @version 0.1
 */
package com.hisham.dummydatagenerator;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.LocalDateTime;

/**
 * Enables JPA repositories scanning in the specified base package.
 * This allows Spring Data JPA to automatically create repository implementations.
 */
@EnableJpaRepositories(basePackages = {"com.hisham.dummydatagenerator"})

/**
 * Main Spring Boot application class.
 * Kafka auto-configuration is excluded to prevent automatic Kafka initialization
 * unless explicitly configured.
 */
@SpringBootApplication(exclude = {KafkaAutoConfiguration.class})
public class DummyDataGeneratorApplication {

	/**
	 * Prints the application version and startup timestamp.
	 * This method is called after the application context is initialized.
	 */
	@PostConstruct
	public static void printStartupVersion() {
		System.out.println("=== DummyDataGenerator v0.1 Booted at " + LocalDateTime.now() + " ===");
	}

	/**
	 * Main entry point for the application.
	 * Initializes the Spring Boot application context and starts the application.
	 *
	 * @param args Command line arguments passed to the application
	 */
	public static void main(String[] args) {
		printStartupVersion();
		SpringApplication.run(DummyDataGeneratorApplication.class, args);
	}

}
