package ru.vsu.uic.wasp.ng.core.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vsu.uic.wasp.ng.core.dao.entity.Role;

import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

}
