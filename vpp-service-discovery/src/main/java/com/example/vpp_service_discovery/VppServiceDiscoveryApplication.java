package com.example.vpp_service_discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class VppServiceDiscoveryApplication {

	public static void main(String[] args) {
		SpringApplication.run(VppServiceDiscoveryApplication.class, args);
	}

}
