package ru.vsu.uic.wasp.ng.core.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vsu.uic.wasp.ng.core.dao.entity.AuthenticationType;

import java.util.Optional;
import java.util.UUID;

public interface AuthenticationTypeRepository extends JpaRepository<AuthenticationType, UUID> {

    Optional<AuthenticationType> findAuthenticationTypeByCode(String code);

}
