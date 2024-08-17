package ru.vsu.uic.wasp.ng.core.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.vsu.uic.wasp.ng.test.WaspPostgreSQLContainer;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

@SpringBootTest
@Testcontainers
class WaspAuthenticationProviderTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<WaspPostgreSQLContainer> postgreSQLContainer = WaspPostgreSQLContainer.getInstance();

    @Autowired
    WaspAuthenticationProvider authenticationProvider;

    @Test
    @Sql(scripts = "/load-test-blocked-user.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/unload-test-blocked-user.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void whenAuthenticateBlockedUser_DisabledExceptionIsThrown() throws Exception {
        WaspAuthentication waspAuthentication = new WaspAuthentication("test-blocked-user", "password");
        assertThrowsExactly(DisabledException.class, () -> {
            authenticationProvider.authenticate(waspAuthentication);
        });
    }

    private static class WaspAuthentication implements Authentication {

        private final String principal;
        private final String credentials;

        WaspAuthentication(String principal, String credentials) {
            this.principal = principal;
            this.credentials = credentials;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return null;
        }

        @Override
        public Object getCredentials() {
            return credentials;
        }

        @Override
        public Object getDetails() {
            return null;
        }

        @Override
        public Object getPrincipal() {
            return principal;
        }

        @Override
        public boolean isAuthenticated() {
            return false;
        }

        @Override
        public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

        }

        @Override
        public String getName() {
            return principal;
        }
    }

}