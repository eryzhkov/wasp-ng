package ru.vsu.uic.wasp.ng.core.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

/**
 * The class defines the composite primary key for the 'users_roles' table. The class is needed because the
 * 'users_roles' table has additional columns and the usual @ManyToMany is not enough here. We need an additional entity
 * (see UserRole) to handle the link between users and roles to keep the additional attributes.
 */
@Embeddable
@Getter
@Setter
@EqualsAndHashCode
public class UserRoleId implements Serializable {

    @Column(name = "ref_users")
    private UUID userId;
    @Column(name = "ref_roles")
    private UUID roleId;


}
