package com.tr.autos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AutosApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutosApplication.class, args);
	}

}
