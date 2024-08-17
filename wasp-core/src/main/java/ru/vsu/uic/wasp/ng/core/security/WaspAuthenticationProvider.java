package ru.vsu.uic.wasp.ng.core.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
import ru.vsu.uic.wasp.ng.core.dao.repository.UserRepository;
import ru.vsu.uic.wasp.ng.core.exception.ExternalSystemException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WaspAuthenticationProvider implements AuthenticationProvider {

    // The role to be automatically added to all authenticated users.
    // The prefix 'ROLE_' here is the Spring Security requirement.
    private final static String ROLE_AUTHENTICATED_USER = "ROLE_" + WaspRole.AUTHENTICATED_USER;

    private final UserRepository userRepository;
    private final RadiusClient radiusClient;
    private PasswordEncoder passwordEncoder;

    private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Transactional
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

            if (user.getStatus().getCode().equals(AccountStatus.BLOCKED.toString())) {
                throw new DisabledException("Account '%s' is blocked.".formatted(userName));
            }

            if (user.getAuthenticationType().getCode().equals(WaspAuthType.INT_WASP.toString())) {
                // Authenticate against the database
                if (!this.passwordEncoder.matches(providedPassword, user.getPassword())) {
                    log.debug("Failed to authenticate since password does not match stored value");
                    throw new BadCredentialsException("Bad credentials for the '%s' user.".formatted(userName));
                }
            } else if (user.getAuthenticationType().getCode().equals(WaspAuthType.EXT_RADIUS.toString())) {
                // Authenticate against the RADIUS
                try {
                    if (!radiusClient.isAccepted(userName, authentication.getCredentials().toString())) {
                        log.debug("Failed to authenticate since RADIUS rejects the user '%s'".formatted(userName));
                        throw new BadCredentialsException("Bad credentials for the '%s' user.".formatted(userName));
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

}
