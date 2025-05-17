package com.example.vpp;

import org.springframework.boot.SpringApplication;

public class TestVppApplication {

	public static void main(String[] args) {
		SpringApplication.from(VppWebApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
