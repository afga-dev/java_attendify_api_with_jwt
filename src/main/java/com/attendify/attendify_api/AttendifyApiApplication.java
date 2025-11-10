package com.attendify.attendify_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AttendifyApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(AttendifyApiApplication.class, args);
	}

}
