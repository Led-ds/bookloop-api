package com.bookloop.user.application;

import com.bookloop.user.domain.User;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-02T22:27:05-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.11 (Azul Systems, Inc.)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UUID id = null;
        String name = null;
        String email = null;
        String avatarUrl = null;
        String bio = null;
        String location = null;
        int penaltiesCount = 0;

        id = user.getId();
        name = user.getName();
        email = user.getEmail();
        avatarUrl = user.getAvatarUrl();
        bio = user.getBio();
        location = user.getLocation();
        penaltiesCount = user.getPenaltiesCount();

        String role = user.getRole().name();

        UserResponse userResponse = new UserResponse( id, name, email, avatarUrl, bio, location, penaltiesCount, role );

        return userResponse;
    }
}
