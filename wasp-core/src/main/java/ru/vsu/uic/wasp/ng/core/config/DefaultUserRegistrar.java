package ru.vsu.uic.wasp.ng.core.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.vsu.uic.wasp.ng.core.dao.entity.AuthenticationType;
import ru.vsu.uic.wasp.ng.core.dao.entity.Role;
import ru.vsu.uic.wasp.ng.core.dao.entity.User;
import ru.vsu.uic.wasp.ng.core.dao.entity.UserStatus;
import ru.vsu.uic.wasp.ng.core.dao.repository.AuthenticationTypeRepository;
import ru.vsu.uic.wasp.ng.core.dao.repository.RoleRepository;
import ru.vsu.uic.wasp.ng.core.dao.repository.UserRepository;
import ru.vsu.uic.wasp.ng.core.dao.repository.UserStatusRepository;
import ru.vsu.uic.wasp.ng.core.security.AccountStatus;
import ru.vsu.uic.wasp.ng.core.security.WaspAuthType;
import ru.vsu.uic.wasp.ng.core.security.WaspRole;

import java.util.Optional;

/**
 * The component is responsible for registering the default administrative user defined in the application.properties.
 * The registration is done only once if the user was not found in the database.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DefaultUserRegistrar implements ApplicationListener<ContextRefreshedEvent> {

    @Value("${spring.security.user.name}")
    private String defaultAdminUserName;

    @Value("${spring.security.user.password}")
    private String defaultAdminPassword;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final UserStatusRepository userStatusRepository;

    private final AuthenticationTypeRepository authenticationTypeRepository;

    private boolean defaultUserInitialised = false;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.debug("Got event: {}", event);
        if (!defaultUserInitialised) {
            log.debug("Check if the default user was already registered...");
            User defaultUser = userRepository.findByLogin(defaultAdminUserName);
            if (defaultUser == null) {
                log.debug("The default user was not registered.");
                defaultUser = new User();
                defaultUser.setLogin(defaultAdminUserName);
                defaultUser.setPassword(defaultAdminPassword);
                defaultUser.setFirstName("");
                defaultUser.setLastName("");
                Optional<UserStatus> activeUserStatus = userStatusRepository.findUserStatusByCode(
                        AccountStatus.ACTIVE.toString());
                // set the account status to the 'active'
                if (activeUserStatus.isPresent()) {
                    defaultUser.setStatus(activeUserStatus.get());
                } else {
                    throw new RuntimeException(
                            "Not found the predefined account status in the database: " + AccountStatus.ACTIVE);
                }
                // set the authentication type to the 'internal'
                Optional<AuthenticationType> internalAuthenticationType = authenticationTypeRepository.findAuthenticationTypeByCode(
                        WaspAuthType.INT_WASP.toString());
                if (internalAuthenticationType.isPresent()) {
                    defaultUser.setAuthenticationType(internalAuthenticationType.get());
                } else {
                    throw new RuntimeException(
                            "Not found the predefined authentication type in the database: " + WaspAuthType.INT_WASP);
                }
                // set all roles
                for (WaspRole waspRole : WaspRole.values()) {
                    Optional<Role> role = roleRepository.findRoleByCode(waspRole.toString());
                    if (role.isPresent()) {
                        // The AUTHENTICATED_USER role is granted dynamically for every authenticated user.
                        // There is no need to grant the role here.
                        if (!role.get().getCode().equals(WaspRole.AUTHENTICATED_USER.toString())) {
                            defaultUser.addRole(role.get());
                        }
                    } else {
                        throw new RuntimeException("Not found the predefined role in the database: " + waspRole);
                    }
                }
                log.debug("The fully initialised default user: {}", defaultUser);
                userRepository.save(defaultUser);
                defaultUserInitialised = true;
            } else {
                log.debug(
                        "The default user was found in the database. Looks like it was already initialised in the past.");
                defaultUserInitialised = true;
            }
        } else {
            log.debug("The default user was already initialised.");
        }
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}
