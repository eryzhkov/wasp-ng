package ru.vsu.uic.wasp.ng.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.event.EventListener;
import ru.vsu.uic.wasp.ng.core.config.GitInfoProperties;

@SpringBootApplication
@ConfigurationPropertiesScan
@Slf4j
public class WaspCoreApplication {

	private final GitInfoProperties gitInfoProperties;

	public static void main(String[] args) {
		SpringApplication.run(WaspCoreApplication.class, args);
	}

	public WaspCoreApplication(GitInfoProperties gitInfoProperties) {
		this.gitInfoProperties = gitInfoProperties;
	}

	@EventListener
	public void onStartup(ApplicationReadyEvent event) {
		log.info("Git build: {}", gitInfoProperties.getBuildVersion());
		log.info("Git build time: {}", gitInfoProperties.getBuildTime());
		log.info("Git branch: {}", gitInfoProperties.getBranch());
		log.info("Git commit: {}", gitInfoProperties.getCommitIdAbbrev());
		log.info("Git tags: {}", gitInfoProperties.getTags());
	}



}
