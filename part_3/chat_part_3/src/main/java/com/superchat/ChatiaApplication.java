package com.superchat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ChatiaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatiaApplication.class, args);
	}

}
