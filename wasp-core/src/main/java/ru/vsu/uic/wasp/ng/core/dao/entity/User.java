package ru.vsu.uic.wasp.ng.core.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.vsu.uic.wasp.ng.core.security.AccountStatus;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
public class User implements UserDetails {

    @Id
    private UUID id;
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
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "users_roles",
               joinColumns = @JoinColumn(name = "ref_users", referencedColumnName = "id"),
               inverseJoinColumns = @JoinColumn(name = "ref_roles", referencedColumnName = "id"))
    private Set<Role> roles;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ref_user_statuses")
    private UserStatus status;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ref_auth_types")
    private AuthenticationType authenticationType;

    public User() {}

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
        return getRoles();
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
