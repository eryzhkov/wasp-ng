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
@Table(name = "user_statuses")
@Getter
@Setter
@ToString
public class UserStatus {

    @Id
    private UUID id;
    @Column(name = "user_status_code", nullable = false, unique = true, updatable = false)
    private String code;
    @Column(name = "user_status_name", nullable = false, updatable = false)
    private String label;

    public UserStatus() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        UserStatus that = (UserStatus) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
