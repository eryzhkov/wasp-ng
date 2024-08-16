package ru.vsu.uic.wasp.ng.core.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "auth_types")
@Getter
@Setter
@ToString
public class AuthenticationType {

    @Id
    private UUID id;
    @Column(name = "auth_type_code", unique = true, updatable = false)
    private String code;
    @Column(name = "auth_type_name", updatable = false)
    private String label;

    public AuthenticationType() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        AuthenticationType that = (AuthenticationType) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
