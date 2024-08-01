package ru.vsu.uic.wasp.ng.core.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.springframework.security.core.GrantedAuthority;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@ToString
public class Role implements GrantedAuthority {

    @Id
    private UUID id;
    @Column(name = "role_code", unique = true, updatable = false)
    private String code;
    @Column(name = "role_label", updatable = false)
    private String label;
    @Column(name = "role_description", updatable = false)
    private String description;

    public Role() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        Role role = (Role) o;
        return getId() != null && Objects.equals(getId(), role.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String getAuthority() {
        return "ROLE_" + getCode();
    }
}
