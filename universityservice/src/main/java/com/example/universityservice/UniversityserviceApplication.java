package com.example.universityservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@EnableDiscoveryClient
@SpringBootApplication
public class UniversityserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UniversityserviceApplication.class, args);
	}
}
