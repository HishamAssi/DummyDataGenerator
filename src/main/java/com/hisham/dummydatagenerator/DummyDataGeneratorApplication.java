package com.hisham.dummydatagenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = {"com.hisham.dummydatagenerator"})

@SpringBootApplication
public class DummyDataGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(DummyDataGeneratorApplication.class, args);
	}

}
