package ru.vsu.uic.wasp.ng.core.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
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

    private final static String ROLE_AUTHENTICATED_USER = "ROLE_AUTHENTICATED_USER";

    @Value("${spring.security.user.name}")
    private String embeddedAdminUserName;

    @Value("${spring.security.user.password}")
    private String embeddedAdminPassword;

    @Value("${spring.security.user.roles}")
    private String embeddedAdminRoles;

    private final UserRepository userRepository;

    private final RadiusClient radiusClient;
    private PasswordEncoder passwordEncoder;

    private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        if (authentication.getPrincipal() == null) {
            log.debug("Failed to authenticate since no principal provided");
            throw new BadCredentialsException("No principal provided.");
        }

        if (authentication.getCredentials() == null) {
            log.debug("Failed to authenticate since no credentials provided");
            throw new BadCredentialsException("No password provided for the '%s'.".formatted(authentication.getPrincipal().toString()));
        }

        String userName = authentication.getPrincipal().toString();
        String providedPassword = authentication.getCredentials().toString();

        // Authenticate the embedded user.
        if (embeddedAdminUserName.equals(userName)) {
            // Check the password
            if (!this.passwordEncoder.matches(providedPassword, embeddedAdminPassword)) {
                log.debug("Failed to authenticate since password does not match stored value");
                throw new BadCredentialsException("Bad credentials for the '%s' user.".formatted(userName));
            }
            // Prepare roles list
            String[] defaultRoles = embeddedAdminRoles.split(",");
            UserDetails embeddedUserDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(embeddedAdminUserName)
                    .password(embeddedAdminPassword)
                    .roles(defaultRoles)
                    .build();

            log.debug("Authenticated embedded user");
            return createSuccessAuthentication(embeddedUserDetails.getUsername(), authentication, embeddedUserDetails);
        }

        // Try to load a user from the database
        User user = userRepository.findByLogin(userName);

        if (user != null) {
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
                throw new BadCredentialsException("Unknown authentication type '%s'.".formatted(user.getAuthenticationType().getCode()));
            }
            // Add the default role
            List<SimpleGrantedAuthority> enrichedGrantedAuthorities = new ArrayList<>();
            enrichedGrantedAuthorities.add(new SimpleGrantedAuthority(ROLE_AUTHENTICATED_USER));
            Collection<? extends GrantedAuthority> grantedAuthorities = user.getAuthorities();
            for (GrantedAuthority ga : grantedAuthorities) {
                SimpleGrantedAuthority sga = (SimpleGrantedAuthority) ga;
                enrichedGrantedAuthorities.add(sga);
            }
            UsernamePasswordAuthenticationToken token = UsernamePasswordAuthenticationToken
                    .authenticated(user, authentication.getCredentials(), this.authoritiesMapper.mapAuthorities(enrichedGrantedAuthorities));
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

    protected Authentication createSuccessAuthentication(Object principal, Authentication authentication, UserDetails user) {
        UsernamePasswordAuthenticationToken token = UsernamePasswordAuthenticationToken
                .authenticated(principal, authentication.getCredentials(), this.authoritiesMapper.mapAuthorities(user.getAuthorities()));
        token.setDetails(authentication.getDetails());
        return token;
    }
}
