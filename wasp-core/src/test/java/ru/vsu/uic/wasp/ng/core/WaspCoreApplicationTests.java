package ru.vsu.uic.wasp.ng.core;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class WaspCoreApplicationTests {

	@Test
	void contextLoads() {
	}

}
