package com.leviathan.bidpass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BidpassApplication {

	public static void main(String[] args) {
		SpringApplication.run(BidpassApplication.class, args);
	}

}
