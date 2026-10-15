
package com.example.demo.user;

import com.example.demo.organization.Organization;
import com.example.demo.organization.OrganizationRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final OrganizationRepository organizationRepository;

    public AppUserDetailsService(
            AppUserRepository appUserRepository,
            OrganizationRepository organizationRepository) {
        this.appUserRepository = appUserRepository;
        this.organizationRepository = organizationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        String[] parts = username.split("\\|", 2);

        if (parts.length != 2) {
            throw new UsernameNotFoundException("Invalid credentials");
        }

        Organization organization = organizationRepository
                .findBySlug(parts[0])
                .orElseThrow(() ->
                        new UsernameNotFoundException("Invalid credentials"));

        AppUser appUser = appUserRepository
                .findByEmailIgnoreCaseAndOrganization_Id(
                        parts[1], organization.getId())
                .orElseThrow(() ->
                        new UsernameNotFoundException("Invalid credentials"));

        return User.withUsername(
                        organization.getSlug() + "|" + appUser.getEmail())
                .password(appUser.getPassword())
                .roles(appUser.getRole().name())
                .build();
    }
}