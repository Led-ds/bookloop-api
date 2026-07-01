package com.bookloop.user.application;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 120) String name,
        @Size(max = 500) String bio,
        @Size(max = 120) String location,
        String avatarUrl
) {}
