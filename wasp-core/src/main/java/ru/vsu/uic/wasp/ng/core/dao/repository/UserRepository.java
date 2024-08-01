package ru.vsu.uic.wasp.ng.core.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vsu.uic.wasp.ng.core.dao.entity.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    User findByLogin(String login);

}
