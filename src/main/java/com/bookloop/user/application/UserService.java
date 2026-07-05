package com.bookloop.user.application;

import com.bookloop.shared.exception.ResourceNotFoundException;
import com.bookloop.user.domain.User;
import com.bookloop.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        return userMapper.toResponse(load(id));
    }

    @Transactional
    public UserResponse updateProfile(UUID id, UpdateProfileRequest req) {
        User user = load(id);
        user.updateProfile(req.name(), req.bio(), req.city(), req.state(),
                req.addressLine(), req.neighborhood(), req.postalCode(), req.avatarUrl());
        log.info("Perfil atualizado: userId={} profileCompleted={}", id, user.isProfileCompleted());
        return userMapper.toResponse(user);
    }

    private User load(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
    }
}
