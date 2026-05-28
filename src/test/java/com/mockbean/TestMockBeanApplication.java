package com.mockbean;

import org.springframework.boot.SpringApplication;

public class TestMockBeanApplication {

	public static void main(String[] args) {
		SpringApplication.from(MockBeanApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
