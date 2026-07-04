package com.bookloop.user.application;

import com.bookloop.security.JwtService;
import com.bookloop.shared.exception.BusinessException;
import com.bookloop.shared.exception.ResourceNotFoundException;
import com.bookloop.user.domain.User;
import com.bookloop.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            log.info("Registro recusado (email já em uso): email={}", req.email());
            throw new BusinessException("Já existe uma conta com este email.");
        }
        User user = User.register(req.name(), req.email(), passwordEncoder.encode(req.password()));
        userRepository.save(user);
        log.info("Novo usuário registrado: email={}", user.getEmail());
        return issueTokens(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        } catch (BadCredentialsException ex) {
            // Nunca logamos a senha — apenas o identificador da tentativa.
            log.warn("Falha de login (credenciais inválidas): email={}", req.email());
            throw ex;
        }
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", req.email()));
        log.info("Login bem-sucedido: email={}", user.getEmail());
        return issueTokens(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshRequest req) {
        String token = req.refreshToken();
        if (!jwtService.isValid(token) || !jwtService.isRefreshToken(token)) {
            log.warn("Refresh recusado: token inválido ou expirado.");
            throw new BadCredentialsException("Refresh token inválido ou expirado.");
        }
        User user = userRepository.findByEmail(jwtService.extractSubject(token))
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", jwtService.extractSubject(token)));
        return issueTokens(user);
    }

    // BACKLOG (v1.2): persistir refresh tokens (tabela refresh_tokens) para permitir
    // revogação/rotação e logout server-side. Hoje o refresh é stateless (assinado por HMAC).
    private AuthResponse issueTokens(User user) {
        String access = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());
        String refresh = jwtService.generateRefreshToken(user.getEmail());
        return new AuthResponse(access, refresh, "Bearer",
                jwtService.getAccessTtlMs(), userMapper.toResponse(user));
    }
}
