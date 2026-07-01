package com.bookloop.user.application;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String avatarUrl,
        String bio,
        String location,
        int penaltiesCount,
        String role
) {}
