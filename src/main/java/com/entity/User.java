package com.entity;

import com.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class User implements UserDetails {

    // === ID ===
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", unique = true, nullable = true)
    private Integer userId;

    // === FIELD TỪ CŨ ===
    @Column(nullable = true, unique = true, length = 100)
    private String username;

    @Column(nullable = true, length = 255)
    private String password;

    @Column(nullable = true, unique = true, length = 100)
    private String email;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    private String address;

    @Column(name = "role", nullable = true)
    @Enumerated(EnumType.STRING)
    private Role role = Role.CUSTOMER;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // === FIELD TỪ MỚI ===
    @Column(nullable = true, length = 20)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, BANNED,...

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    // === RELATIONSHIPS TỪ MỚI ===
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Cart> carts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Order> orders;

    // === CONSTRUCTOR TỪ CŨ (giữ nguyên) ===
    public User(String username, String password, String email, String firstName, String lastName,
            String phoneNumber, String address, Role role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        if (role != null)
            this.role = role;
        this.status = "ACTIVE";
        this.isActive = true;
    }

    // === SPRING SECURITY ===
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !"BANNED".equals(this.status);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(this.isActive) && "ACTIVE".equals(this.status);
    }

    // === HELPER ===
    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    // === LIFECYCLE ===
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null)
            this.createdAt = now;
        if (this.createdDate == null)
            this.createdDate = now;
        if (this.status == null)
            this.status = "ACTIVE";
        if (this.isActive == null)
            this.isActive = true;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // === CẬP NHẬT LAST LOGIN ===
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }
}