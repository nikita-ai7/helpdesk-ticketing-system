package com.example.demo.user;

public record CurrentUserResponse(
    Long id,
    String name,
    String email,
    String role,
    Long organizationId,
    String organizationSlug
) {
    public static CurrentUserResponse from(AppUser user, String organizationSlug) {
        return new CurrentUserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole().name(),
            user.getOrganization().getId(),
            organizationSlug
        );
    }
}