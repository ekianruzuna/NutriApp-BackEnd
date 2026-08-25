package com.NutriApp.NutriApp;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

//activamos Async para que pueda ejecutar acciones asyncronicamente (mandar mails con events and listeners)
@EnableAsync
@SpringBootApplication
public class NutriAppApplication{

	public static void main(String[] args) {
		SpringApplication.run(NutriAppApplication.class, args);
	}


}
