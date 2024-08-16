package ru.vsu.uic.wasp.ng.core.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users_roles")
@Getter
@Setter
@ToString
public class UserRole implements Serializable {

    @EmbeddedId
    private UserRoleId id = new UserRoleId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId") // points to the property in the UserRoleId class referencing to the User entity.
    @JoinColumn(name = "ref_users") // set the join column name, otherwise Hibernate will use user_id
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId") // points to the property in the UserRoleId class referencing to the Role entity.
    @JoinColumn(name = "ref_roles") // set the join column name, otherwise Hibernate will use role_id
    private Role role;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        UserRole userRole = (UserRole) o;
        return getId() != null && Objects.equals(getId(), userRole.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
