package com.bookloop.organization.application;

import com.bookloop.organization.domain.*;
import com.bookloop.shared.exception.BusinessException;
import com.bookloop.shared.exception.ResourceNotFoundException;
import com.bookloop.user.application.AuthResponse;
import com.bookloop.user.application.AuthService;
import com.bookloop.user.domain.User;
import com.bookloop.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final MembershipRepository membershipRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    private static final SecureRandom RANDOM = new SecureRandom();

    /** Cria um convite na comunidade do contexto. OWNER ou ADMIN. */
    @Transactional
    public InvitationResponse invite(CreateInvitationRequest req) {
        OrganizationContext.require(Role.OWNER, Role.ADMIN);
        UUID orgId = OrganizationContext.id();
        Invitation inv = Invitation.create(orgId, req.email(), generateToken(), Role.MEMBER, com.bookloop.security.CurrentUser.id());
        invitationRepository.save(inv);
        log.info("Convite criado: orgId={} token={} email={}", orgId, inv.getToken(), inv.getEmail());
        return toResponse(inv);
    }

    /** Lista convites pendentes da comunidade. OWNER ou ADMIN. */
    @Transactional(readOnly = true)
    public List<InvitationResponse> listPending() {
        OrganizationContext.require(Role.OWNER, Role.ADMIN);
        return invitationRepository
                .findByOrganizationIdAndStatus(OrganizationContext.id(), InvitationStatus.PENDING)
                .stream().map(this::toResponse).toList();
    }

    /** Cancela um convite pendente. OWNER ou ADMIN. */
    @Transactional
    public void cancel(UUID invitationId) {
        OrganizationContext.require(Role.OWNER, Role.ADMIN);
        Invitation inv = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Convite", invitationId));
        if (!inv.getOrganizationId().equals(OrganizationContext.id())) {
            throw new ResourceNotFoundException("Convite", invitationId);
        }
        inv.cancel();
    }

    /** Preview público do convite (o convidado vê antes de aceitar). Não exige login. */
    @Transactional(readOnly = true)
    public InvitationPreview preview(String token) {
        Invitation inv = invitationRepository.findByToken(token).orElse(null);
        if (inv == null) {
            return new InvitationPreview(null, null, null, false, "Convite não encontrado.");
        }
        if (!inv.isPending()) {
            return new InvitationPreview(null, null, null, false, "Este convite não está mais disponível.");
        }
        Organization org = organizationRepository.findById(inv.getOrganizationId()).orElse(null);
        User inviter = userRepository.findById(inv.getInvitedBy()).orElse(null);
        String orgName = org != null ? org.getName() : null;
        String orgDesc = org != null ? org.getDescription() : null;
        String byName  = inviter != null ? inviter.getName() : null;
        return new InvitationPreview(orgName, orgDesc, byName, true, null);
    }

    /**
     * Aceita um convite. Fluxo público (sem contexto de organização).
     * - Usuário autenticado (currentUserId != null): só cria a membership.
     * - Usuário novo: cria a conta (name+password) e a membership.
     * Retorna tokens para o convidado já entrar logado.
     */
    @Transactional
    public AuthResponse accept(String token, AcceptInvitationRequest req, UUID currentUserId) {
        Invitation inv = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Convite", token));

        User user = resolveUser(inv, req, currentUserId);

        // já é membro? então só confirma o convite e devolve tokens
        var existing = membershipRepository.findByUserIdAndOrganizationId(user.getId(), inv.getOrganizationId());
        if (existing.isEmpty()) {
            membershipRepository.save(Membership.member(user.getId(), inv.getOrganizationId()));
        }
        inv.accept();
        log.info("Convite aceito: token={} userId={} orgId={}", token, user.getId(), inv.getOrganizationId());
        return authService.issueTokensFor(user);
    }

    private User resolveUser(Invitation inv, AcceptInvitationRequest req, UUID currentUserId) {
        if (currentUserId != null) {
            return userRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário", currentUserId));
        }
        // usuário novo: precisa de nome + senha
        if (req == null || req.name() == null || req.name().isBlank()
                || req.password() == null || req.password().isBlank()) {
            throw new BusinessException("Para aceitar, informe nome e senha (ou faça login antes).");
        }
        String email = inv.getEmail();
        if (email == null) {
            throw new BusinessException("Este convite é por código: faça login ou cadastre-se antes de aceitar.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Já existe uma conta com este e-mail. Faça login e aceite novamente.");
        }
        User user = User.register(req.name(), email, passwordEncoder.encode(req.password()));
        return userRepository.save(user);
    }

    private String generateToken() {
        byte[] b = new byte[32];
        RANDOM.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    private InvitationResponse toResponse(Invitation i) {
        return new InvitationResponse(i.getId(), i.getEmail(), i.getToken(), i.getRole().name(),
                i.getStatus().name(), i.getExpiresAt(), i.getCreatedAt());
    }
}
