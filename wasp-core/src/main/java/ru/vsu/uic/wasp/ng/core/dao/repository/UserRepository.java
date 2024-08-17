package ru.vsu.uic.wasp.ng.core.dao.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.vsu.uic.wasp.ng.core.dao.entity.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @EntityGraph(
            value = "user-status-authtype-graph",
            type = EntityGraphType.FETCH)
    User findByLogin(String login);

}
