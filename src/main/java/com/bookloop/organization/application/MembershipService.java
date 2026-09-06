package com.bookloop.organization.application;

import com.bookloop.organization.domain.*;
import com.bookloop.shared.exception.BusinessException;
import com.bookloop.shared.exception.ResourceNotFoundException;
import com.bookloop.user.domain.User;
import com.bookloop.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;

    /** Lista os membros ativos da comunidade no contexto. Qualquer membro pode ver. */
    @Transactional(readOnly = true)
    public List<MemberResponse> listMembers() {
        UUID orgId = OrganizationContext.id();
        return membershipRepository.findByOrganizationIdAndStatus(orgId, MembershipStatus.ACTIVE).stream()
                .map(m -> {
                    User u = userRepository.findById(m.getUserId())
                            .orElseThrow(() -> new ResourceNotFoundException("Usuário", m.getUserId()));
                    return new MemberResponse(m.getId(), u.getId(), u.getName(), u.getEmail(),
                            u.getAvatarUrl(), m.getRole().name(), m.getCreatedAt());
                })
                .toList();
    }

    /** Promove um membro a ADMIN. Somente OWNER. */
    @Transactional
    public MemberResponse promote(UUID membershipId) {
        OrganizationContext.require(Role.OWNER);
        Membership m = loadInOrg(membershipId);
        if (m.getRole() == Role.OWNER) {
            throw new BusinessException("O dono não pode ser rebaixado ou alterado.");
        }
        m.promoteToAdmin();
        return toResponse(m);
    }

    /** Rebaixa um ADMIN a MEMBER. Somente OWNER. */
    @Transactional
    public MemberResponse demote(UUID membershipId) {
        OrganizationContext.require(Role.OWNER);
        Membership m = loadInOrg(membershipId);
        if (m.getRole() == Role.OWNER) {
            throw new BusinessException("O dono não pode ser rebaixado.");
        }
        m.demoteToMember();
        return toResponse(m);
    }

    /** Remove um membro da comunidade. OWNER ou ADMIN. */
    @Transactional
    public void remove(UUID membershipId) {
        OrganizationContext.require(Role.OWNER, Role.ADMIN);
        Membership m = loadInOrg(membershipId);
        if (m.getRole() == Role.OWNER) {
            throw new BusinessException("O dono não pode ser removido da comunidade.");
        }
        m.remove();
    }

    /** Carrega um membership garantindo que pertence à comunidade do contexto (senão 404). */
    private Membership loadInOrg(UUID membershipId) {
        Membership m = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new ResourceNotFoundException("Membro", membershipId));
        if (!m.getOrganizationId().equals(OrganizationContext.id())
                || m.getStatus() != MembershipStatus.ACTIVE) {
            throw new ResourceNotFoundException("Membro", membershipId);
        }
        return m;
    }

    private MemberResponse toResponse(Membership m) {
        User u = userRepository.findById(m.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", m.getUserId()));
        return new MemberResponse(m.getId(), u.getId(), u.getName(), u.getEmail(),
                u.getAvatarUrl(), m.getRole().name(), m.getCreatedAt());
    }
}
