package com.univault.gateway;

import com.univault.gateway.config.ServiceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ServiceConfig.class)
public class ApiGateway {
	public static void main(String[] args) {
		SpringApplication.run(ApiGateway.class, args);
	}

}
