package ru.vsu.uic.wasp.ng.core.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.vsu.uic.wasp.ng.core.exception.ExternalSystemException;
import ru.vsu.uic.wasp.ng.test.WaspPostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@Testcontainers
class RadiusClientTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<WaspPostgreSQLContainer> postgreSQLContainer = WaspPostgreSQLContainer.getInstance();

    @Autowired
    RadiusClient radiusClient;

    @Test
    void isAccepted() throws ExternalSystemException {
        final String user = "user";
        final String password = "password";
        assertFalse(radiusClient.isAccepted(user, password));
    }
}