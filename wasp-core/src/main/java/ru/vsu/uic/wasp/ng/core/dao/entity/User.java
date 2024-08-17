package ru.vsu.uic.wasp.ng.core.dao.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Exclude;
import org.hibernate.Hibernate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.vsu.uic.wasp.ng.core.security.AccountStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
@NamedEntityGraph(
        name = "user-status-authtype-graph",
        attributeNodes = {
                @NamedAttributeNode("status"),
                @NamedAttributeNode("authenticationType")
        }
)
@Getter
@Setter
@ToString
public class User implements UserDetails, Serializable {

    // Set the value automatically. Otherwise, Hibernate can't set the 'userId' property in the UserRoleId object.
    // The reason is that the property 'id' is not set until the User entity is not stored in the database.
    @Id
    private UUID id = UUID.randomUUID();
    @Column(name = "login", unique = true, nullable = false)
    private String login;
    @Column(name = "password")
    private String password;
    @Column(name = "first_name", nullable = false)
    private String firstName;
    @Column(name = "last_name", nullable = false)
    private String lastName;
    @Column(name = "middle_name")
    private String middleName;
    @Column(name = "comment")
    private String comment;

    @OneToMany(
            // the entity User is referenced by the property 'user' in the UserRole entity
            mappedBy = "user",
            // do not use ALL - it leads to the "A different object with the same identifier value was already associated with the session" error
            cascade = CascadeType.MERGE,
            // Remove UserRole entities if the corresponding User is deleted
            orphanRemoval = true
    )
    @Exclude
    private Set<UserRole> roles = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "ref_user_statuses")
    private UserStatus status;

    @ManyToOne
    @JoinColumn(name = "ref_auth_types")
    private AuthenticationType authenticationType;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @Column(name = "last_failed_login_at")
    private LocalDateTime lastFailedLoginAt;

    @Column(name = "failed_logins_counter")
    private Integer failedLoginsCounter;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Adds a new role to the user.
     *
     * @param role a role to be added
     */
    public void addRole(Role role) {
        // Create a new UserRole entity.
        UserRole userRole = new UserRole();
        // Initialize the UserRole entity
        userRole.getId().setUserId(this.id);
        userRole.getId().setRoleId(role.getId());
        userRole.setUser(this);
        userRole.setRole(role);
        // Add the new UserRole entity to the collection of the User entity
        roles.add(userRole);
        // Add the new UserRole entity to the collection of the Role entity
        role.getUsers().add(userRole);
    }

    /**
     * Removes the role from the User entity
     *
     * @param role a role to be removed
     */
    public void removeRole(Role role) {
        // Loop over all roles of the user.
        for (Iterator<UserRole> iterator = roles.iterator(); iterator.hasNext(); ) {
            UserRole userRole = iterator.next();
            if (userRole.getUser().equals(this) && userRole.getRole().equals(role)) {
                // Found the role to be revoked from the user in the collection

                // Remove the found UserRole entity from the collection
                iterator.remove();

                // Remove the same entity from the collection in the Role entity.
                userRole.getRole().getUsers().remove(userRole);

                // Set all references to null.
                userRole.setRole(null);
                userRole.setUser(null);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        User user = (User) o;
        return getId() != null && Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> grantedAuthorities = new HashSet<>();
        for (UserRole userRole : this.roles) {
            grantedAuthorities.add(new SimpleGrantedAuthority(userRole.getRole().getAuthority()));
        }
        return grantedAuthorities;
    }

    @Override
    public String getUsername() {
        return getLogin();
    }

    @Override
    public boolean isAccountNonExpired() {
        // At the moment, no expiration policies implemented.
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return getStatus().getCode().equalsIgnoreCase(AccountStatus.ACTIVE.toString());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // At the moment, no expiration policies implemented.
        return true;
    }

    @Override
    public boolean isEnabled() {
        return getStatus().getCode().equalsIgnoreCase(AccountStatus.ACTIVE.toString());
    }
}
