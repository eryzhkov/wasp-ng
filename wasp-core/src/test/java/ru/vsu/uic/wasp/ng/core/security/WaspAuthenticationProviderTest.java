package ru.vsu.uic.wasp.ng.core.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.vsu.uic.wasp.ng.core.dao.entity.User;
import ru.vsu.uic.wasp.ng.core.dao.repository.UserRepository;
import ru.vsu.uic.wasp.ng.test.WaspPostgreSQLContainer;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {"wasp.security.max.failed.logins=3"})
@Testcontainers
class WaspAuthenticationProviderTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<WaspPostgreSQLContainer> postgreSQLContainer = WaspPostgreSQLContainer.getInstance();

    @Autowired
    WaspAuthenticationProvider authenticationProvider;

    @Value("${wasp.security.max.failed.logins}")
    int maxFailedLogins;

    @Autowired
    UserRepository userRepository;

    @Test
    @Sql(scripts = "/load-test-blocked-user.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/unload-test-blocked-user.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void whenAuthenticateBlockedUser_DisabledExceptionIsThrown() throws Exception {
        WaspAuthentication waspAuthentication = new WaspAuthentication("test-blocked-user", "password");
        assertThrowsExactly(DisabledException.class, () -> {
            authenticationProvider.authenticate(waspAuthentication);
        });
    }

    @Test
    @Sql(scripts = "/load-test-active-user.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/unload-test-active-user.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void whenFailedLoginAttemptsCounterEqualsToThreshold_AccountIsLocked() throws Exception {
        WaspAuthentication waspAuthentication = new WaspAuthentication("test-active-user", "password");

        // Try to log in using wrong credentials.
        // It leads to the increasing of the failed login counter for the user.
        for (int loginAttempt = 1; loginAttempt < maxFailedLogins; loginAttempt++) {
            assertThrowsExactly(BadCredentialsException.class, () -> {
                authenticationProvider.authenticate(waspAuthentication);
            });
        }

        // The last login after that the account should be locked.
        assertThrowsExactly(LockedException.class, () -> {
            authenticationProvider.authenticate(waspAuthentication);
        });

        User testUser = userRepository.findByLogin(waspAuthentication.getName());

        assertEquals(3, testUser.getFailedLoginsCounter());
        assertTrue(testUser.isAccountLocked());
        assertNotNull(testUser.getLockedAt());
    }

    @Test
    @Sql(scripts = "/load-test-locked-user-expired.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/unload-test-locked-user.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void whenLockedInThePastUserLogsIn_AccountIsUnlocked() throws Exception {
        WaspAuthentication waspAuthentication = new WaspAuthentication("test-locked-user", "password");
        assertThrowsExactly(BadCredentialsException.class, () -> {
            authenticationProvider.authenticate(waspAuthentication);
        });
        User testUser = userRepository.findByLogin(waspAuthentication.getName());
        assertEquals(1, testUser.getFailedLoginsCounter());
        assertFalse(testUser.isAccountLocked());
        assertNull(testUser.getLockedAt());
    }

    @Test
    @Sql(scripts = "/load-test-locked-user-recently.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/unload-test-locked-user.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
    void whenRecentlyLockedUserLogsIn_LockedExceptionIsThrown() throws Exception {
        WaspAuthentication waspAuthentication = new WaspAuthentication("test-locked-user", "password");
        assertThrowsExactly(LockedException.class, () -> authenticationProvider.authenticate(waspAuthentication));
        User testUser = userRepository.findByLogin(waspAuthentication.getName());
        assertEquals(3, testUser.getFailedLoginsCounter());
        assertTrue(testUser.isAccountLocked());
        assertNotNull(testUser.getLockedAt());
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