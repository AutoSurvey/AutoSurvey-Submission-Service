package com.revature.autosurvey.submissions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class SubmissionsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SubmissionsApplication.class, args);
	}

}
