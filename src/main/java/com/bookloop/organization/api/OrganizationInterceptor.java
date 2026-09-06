package com.bookloop.organization.api;

import com.bookloop.organization.application.OrganizationContext;
import com.bookloop.organization.domain.Membership;
import com.bookloop.organization.domain.MembershipRepository;
import com.bookloop.organization.domain.MembershipStatus;
import com.bookloop.security.CurrentUser;
import com.bookloop.shared.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.UUID;

/**
 * Intercepta rotas /api/v1/orgs/{orgId}/** : extrai o orgId, confirma que o
 * usuário autenticado é membro ATIVO daquela comunidade e popula o
 * OrganizationContext. Se não for membro (ou a comunidade não existir), responde
 * 404 — não revela a existência de comunidades das quais o usuário não participa.
 */
@Component
@RequiredArgsConstructor
public class OrganizationInterceptor implements HandlerInterceptor {

    private final MembershipRepository membershipRepository;

    @Override
    @SuppressWarnings("unchecked")
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Map<String, String> vars = (Map<String, String>)
                request.getAttribute(org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (vars == null || !vars.containsKey("orgId")) {
            return true; // rota sem orgId — não é escopada por comunidade
        }

        UUID orgId = parseOrThrow(vars.get("orgId"));
        UUID userId = CurrentUser.id();

        Membership m = membershipRepository
                .findByUserIdAndOrganizationId(userId, orgId)
                .filter(mem -> mem.getStatus() == MembershipStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Comunidade", orgId));

        OrganizationContext.set(orgId, m.getRole());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        OrganizationContext.clear();
    }

    private UUID parseOrThrow(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Comunidade", raw);
        }
    }
}
