
package com.example.demo.user;

import com.example.demo.organization.Organization;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "app_users",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_user_org_email",
            columnNames = {"organization_id", "email"}
        )
    }
)
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected AppUser() {
    }

    public AppUser(
            String name,
            String email,
            String password,
            UserRole role,
            Organization organization) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.organization = organization;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public UserRole getRole() {
        return role;
    }

    public Organization getOrganization() {
        return organization;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
}
