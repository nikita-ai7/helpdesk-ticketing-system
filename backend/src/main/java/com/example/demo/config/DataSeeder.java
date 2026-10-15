package com.example.demo.config;

import com.example.demo.organization.Organization;
import com.example.demo.organization.OrganizationRepository;
import com.example.demo.user.AppUser;
import com.example.demo.user.AppUserRepository;
import com.example.demo.user.UserRole;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedDemoData(
            OrganizationRepository organizationRepository,
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            Organization acme = getOrCreateOrganization(
                    organizationRepository, "Acme Support", "acme");

            Organization globex = getOrCreateOrganization(
                    organizationRepository, "Globex Support", "globex");

            seedUser(appUserRepository, passwordEncoder, acme,
                    "Acme Admin", "admin@acme.test", UserRole.ORG_ADMIN);

            seedUser(appUserRepository, passwordEncoder, acme,
                    "Acme Agent", "agent@acme.test", UserRole.AGENT);

            seedUser(appUserRepository, passwordEncoder, acme,
                    "Acme Customer", "customer@acme.test", UserRole.CUSTOMER);

            seedUser(appUserRepository, passwordEncoder, globex,
                    "Globex Admin", "admin@globex.test", UserRole.ORG_ADMIN);

            seedUser(appUserRepository, passwordEncoder, globex,
                    "Globex Agent", "agent@globex.test", UserRole.AGENT);

            seedUser(appUserRepository, passwordEncoder, globex,
                    "Globex Customer", "customer@globex.test", UserRole.CUSTOMER);
        };
    }

    private Organization getOrCreateOrganization(
            OrganizationRepository repository,
            String name,
            String slug
    ) {
        return repository.findBySlug(slug)
                .orElseGet(() -> repository.save(
                        new Organization(name, slug)));
    }

    private void seedUser(
            AppUserRepository repository,
            PasswordEncoder passwordEncoder,
            Organization organization,
            String name,
            String email,
            UserRole role
    ) {
        if (!repository.existsByEmailIgnoreCaseAndOrganization_Id(
                email, organization.getId())) {

            AppUser user = new AppUser(
                    name,
                    email,
                    passwordEncoder.encode("Password@123"),
                    role,
                    organization
            );

            repository.save(user);
        }
    }
}