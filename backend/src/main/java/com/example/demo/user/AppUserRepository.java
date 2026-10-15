
package com.example.demo.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmailIgnoreCase(String email);

    Optional<AppUser> findByEmailIgnoreCaseAndOrganization_Id(
            String email, Long organizationId);

    List<AppUser> findByOrganization_Id(Long organizationId);

    boolean existsByEmailIgnoreCaseAndOrganization_Id(
            String email, Long organizationId);
}
