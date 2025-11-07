package com.seashade.api_seashade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class ApiSeashadeApplication {

	public static void main(String[] args) {


		System.out.println("--- PROVA DE NOVO DEPLOY v1.0 --- TESTE HARDCODED ---");
		
		SpringApplication.run(ApiSeashadeApplication.class, args);
	}

}
