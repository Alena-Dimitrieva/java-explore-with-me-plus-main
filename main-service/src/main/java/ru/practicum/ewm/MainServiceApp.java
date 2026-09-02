package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(value = {"ru.practicum.ewm", "ru.practicum.stat.client"})
public class MainServiceApp {
	public static void main(String[] args) {
		SpringApplication.run(MainServiceApp.class, args);
	}
}
