package ru.vsu.uic.wasp.ng.core;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.vsu.uic.wasp.ng.test.WaspPostgreSQLContainer;

@SpringBootTest
@Testcontainers
class WaspCoreApplicationTests {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<WaspPostgreSQLContainer> pgContainer = WaspPostgreSQLContainer.getInstance();

	@Test
	void contextLoads() {
	}

}
