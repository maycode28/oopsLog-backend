package com.example.oopsLog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class OopsLogApplication {

	public static void main(String[] args) {
		SpringApplication.run(OopsLogApplication.class, args);
	}

}
