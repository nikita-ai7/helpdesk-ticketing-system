
package com.example.demo.user;

import com.example.demo.organization.Organization;
import com.example.demo.organization.OrganizationRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AppUserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    public UserController(
            AppUserRepository userRepository,
            OrganizationRepository organizationRepository) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
    }

    @GetMapping("/me")
    public CurrentUserResponse currentUser(Authentication authentication) {
        AppUser user = getAuthenticatedUser(authentication);

        String organizationSlug = authentication.getName().split("\\|", 2)[0];

        return CurrentUserResponse.from(user, organizationSlug);
    }

    @GetMapping("/agents")
    public List<AgentResponse> getAgents(Authentication authentication) {
        AppUser currentUser = getAuthenticatedUser(authentication);

        return userRepository
                .findByOrganization_Id(currentUser.getOrganization().getId())
                .stream()
                .filter(user -> user.getRole() == UserRole.AGENT)
                .map(user -> new AgentResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail()
                ))
                .toList();
    }

    private AppUser getAuthenticatedUser(Authentication authentication) {
        String[] parts = authentication.getName().split("\\|", 2);

        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid authenticated user");
        }

        Organization organization = organizationRepository
                .findBySlug(parts[0])
                .orElseThrow(() ->
                        new IllegalArgumentException("Organization not found"));

        return userRepository
                .findByEmailIgnoreCaseAndOrganization_Id(
                        parts[1], organization.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));
    }

    public record AgentResponse(Long id, String name, String email) {}
}