package com.bookloop.user.application;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 120) String name,
        @Size(max = 500) String bio,
        @Size(max = 80) String city,
        @Size(max = 40) String state,
        @Size(max = 160) String addressLine,
        @Size(max = 80) String neighborhood,
        @Size(max = 20) String postalCode,
        @Size(max = 300)
        @Pattern(regexp = "^$|^https?://.*", message = "avatarUrl deve ser uma URL http(s)")
        String avatarUrl
) {}
