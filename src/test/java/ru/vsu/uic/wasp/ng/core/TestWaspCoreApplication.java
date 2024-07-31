package ru.vsu.uic.wasp.ng.core;

import org.springframework.boot.SpringApplication;

public class TestWaspCoreApplication {

	public static void main(String[] args) {
		SpringApplication.from(WaspCoreApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
