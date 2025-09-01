package com.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.gateway.config.GatewayServicesProperties;

@SpringBootApplication
@EnableConfigurationProperties(GatewayServicesProperties.class)
public class Gateway {
	public static void main(String[] args) {
		SpringApplication.run(Gateway.class, args);
	}

}
