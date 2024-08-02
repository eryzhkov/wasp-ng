package ru.vsu.uic.wasp.ng.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.vsu.uic.wasp.ng.core.dao.entity.User;
import ru.vsu.uic.wasp.ng.core.dao.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    @Value("${spring.security.user.name}")
    private String adminUserName;

    @Value("${spring.security.user.password}")
    private String adminPassword;

    @Value("${spring.security.user.roles}")
    private String adminRole;

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Load user {}", username);
        // Check for the default admin user.
        if (adminUserName.equals(username)) {
            log.info("Returns the default user...");
            String[] roles = adminRole.split(",");
            UserDetails defaultUserDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(adminUserName)
                    .password(adminPassword)
                    .roles(roles)
                    .build();
            log.info("user = {}", defaultUserDetails);
            return defaultUserDetails;
        }
        User user = userRepository.findByLogin(username);
        if (user == null) {
            throw new UsernameNotFoundException("User name '%s' was not found!".formatted(username));
        }
        return user;
    }
}
