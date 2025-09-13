package com.fuel.nexus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class FuelNexusApplication {

	public static void main(String[] args) {
        SpringApplication.run(FuelNexusApplication.class, args);
        log.info( " :::::::: :::::: Fuel Nexus  :::::::: :::::::::: ");
	}

}
