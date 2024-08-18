package ru.vsu.uic.wasp.ng.core.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.vsu.uic.wasp.ng.core.dao.entity.User;
import ru.vsu.uic.wasp.ng.core.dao.entity.UserStatus;
import ru.vsu.uic.wasp.ng.core.dao.repository.UserRepository;
import ru.vsu.uic.wasp.ng.core.dao.repository.UserStatusRepository;
import ru.vsu.uic.wasp.ng.core.exception.ExternalSystemException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class WaspAuthenticationProvider implements AuthenticationProvider {

    // The role to be automatically added to all authenticated users.
    // The prefix 'ROLE_' here is the Spring Security requirement.
    private final static String ROLE_AUTHENTICATED_USER = "ROLE_" + WaspRole.AUTHENTICATED_USER;

    @Value("${wasp.security.max.failed.logins:3}")
    private int maxFailedLogins;

    @Value("${wasp.security.failed.logins.interval.minutes:3}")
    private long failedLoginsIntervalMinutes;

    @Value("${wasp.security.lock.expiration.minutes:5}")
    private long lockExpirationMinutes;

    private final UserRepository userRepository;

    private final UserStatusRepository userStatusRepository;
    private final RadiusClient radiusClient;
    private PasswordEncoder passwordEncoder;

    private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Transactional(noRollbackFor = {BadCredentialsException.class, LockedException.class})
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        if (authentication.getPrincipal() == null || authentication.getPrincipal().toString().trim().isEmpty()) {
            log.debug("Failed to authenticate since no principal provided");
            throw new BadCredentialsException("No principal provided.");
        }

        if (authentication.getCredentials() == null || authentication.getCredentials().toString().trim().isEmpty()) {
            log.debug("Failed to authenticate since no credentials provided");
            throw new BadCredentialsException(
                    "No password provided for the '%s'.".formatted(authentication.getPrincipal().toString()));
        }

        String userName = authentication.getPrincipal().toString();
        String providedPassword = authentication.getCredentials().toString();

        // Try to load a user from the database
        User user = userRepository.findByLogin(userName);

        if (user != null) {

            // Check if the account was blocked.
            if (user.isBlocked()) {
                throw new DisabledException("Account '%s' is blocked.".formatted(userName));
            }

            // Check if the account was locked.
            if (user.isAccountLocked()) {
                log.debug("The account '{}' is locked.", userName);
                // Calculate the lock expiration time.
                LocalDateTime expirationDateTime = user.getLockedAt().plus(lockExpirationMinutes, ChronoUnit.MINUTES);
                log.debug("The expiration time is {}", expirationDateTime);
                log.debug("The current time is {}", LocalDateTime.now());
                if (expirationDateTime.isBefore(LocalDateTime.now())) {
                    // The expiration timeout is over - unlock the account.
                    log.debug("The expiration time is over - unlock the account...");
                    Optional<UserStatus> active = userStatusRepository.findUserStatusByCode(
                            AccountStatus.ACTIVE.toString());
                    if (active.isPresent()) {
                        user.setStatus(active.get());
                        user.setLockedAt(null);
                        user.setFailedLoginsCounter(0);
                    } else {
                        throw new RuntimeException(
                                "The status '%s' was not found in the database.".formatted(AccountStatus.ACTIVE));
                    }
                } else {
                    throw new LockedException("The account '%s' is locked.".formatted(userName));
                }
            }

            if (user.getAuthenticationType().getCode().equals(WaspAuthType.INT_WASP.toString())) {
                // Authenticate against the database
                if (!this.passwordEncoder.matches(providedPassword, user.getPassword())) {
                    log.debug("Failed to authenticate since password does not match stored value");
                    if (lockAccountIfNeeded(user)) {
                        throw new LockedException(
                                "Failed logins threshold is exceeded. The account '%s' is locked.".formatted(userName));
                    } else {
                        throw new BadCredentialsException("Bad credentials for the '%s' user.".formatted(userName));
                    }
                }
            } else if (user.getAuthenticationType().getCode().equals(WaspAuthType.EXT_RADIUS.toString())) {
                // Authenticate against the RADIUS
                try {
                    if (!radiusClient.isAccepted(userName, authentication.getCredentials().toString())) {
                        log.debug("Failed to authenticate since RADIUS rejects the user '%s'".formatted(userName));
                        if (lockAccountIfNeeded(user)) {
                            throw new LockedException(
                                    "Failed logins threshold is exceeded. The account '%s' is locked.".formatted(
                                            userName));
                        } else {
                            throw new BadCredentialsException("Bad credentials for the '%s' user.".formatted(userName));
                        }
                    }
                } catch (ExternalSystemException e) {
                    throw new AuthenticationServiceException("Unexpected error authentication!", e);
                }
            } else {
                throw new BadCredentialsException(
                        "Unknown authentication type '%s'.".formatted(user.getAuthenticationType().getCode()));
            }
            // Add the default role
            List<SimpleGrantedAuthority> enrichedGrantedAuthorities = new ArrayList<>();
            enrichedGrantedAuthorities.add(new SimpleGrantedAuthority(ROLE_AUTHENTICATED_USER));

            Collection<? extends GrantedAuthority> grantedAuthorities = user.getAuthorities();

            for (GrantedAuthority grantedAuthority : grantedAuthorities) {
                SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(
                        grantedAuthority.getAuthority());
                enrichedGrantedAuthorities.add(simpleGrantedAuthority);
            }
            UsernamePasswordAuthenticationToken token = UsernamePasswordAuthenticationToken
                    .authenticated(user.getUsername(), authentication.getCredentials(),
                            this.authoritiesMapper.mapAuthorities(enrichedGrantedAuthorities));
            token.setDetails(authentication.getDetails());

            // clean up lock expiration policy data
            user.setLastFailedLoginAt(null);
            user.setFailedLoginsCounter(0);
            userRepository.save(user);

            log.debug("Authenticated user with token: {}", token);
            return token;
        } else {
            throw new BadCredentialsException("Unknown user '%s'.".formatted(userName));
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    protected boolean lockAccountIfNeeded(User user) {
        boolean isLocked = false;
        int failedLoginCounter = user.getFailedLoginsCounter();
        log.debug("failedLoginCounter = {}", failedLoginCounter);
        LocalDateTime lastFailedLoginAt = user.getLastFailedLoginAt();
        if (lastFailedLoginAt != null && lastFailedLoginAt.plus(failedLoginsIntervalMinutes, ChronoUnit.MINUTES)
                .isBefore(LocalDateTime.now())) {
            // The last failed login was quite long ago.
            user.setFailedLoginsCounter(0);
        }
        log.debug("Increase the failed logins counter...");
        user.setFailedLoginsCounter(failedLoginCounter + 1);
        log.debug("Increase the last failed login timestamp...");
        user.setLastFailedLoginAt(LocalDateTime.now());
        if (user.getFailedLoginsCounter() == maxFailedLogins) {
            log.debug("The limit of the failed logins is reached...");
            user.setLockedAt(LocalDateTime.now());
            Optional<UserStatus> locked = userStatusRepository.findUserStatusByCode(AccountStatus.LOCKED.toString());
            if (locked.isPresent()) {
                log.debug("Lock the account...");
                user.setStatus(locked.get());
                isLocked = true;
            } else {
                throw new RuntimeException(
                        "The user status '%s' was not found in the database.".formatted(AccountStatus.LOCKED));
            }
        }
        log.debug("Save changes for the user...");
        userRepository.save(user);
        return isLocked;
    }

}
