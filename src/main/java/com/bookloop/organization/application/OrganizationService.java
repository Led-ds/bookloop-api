package com.bookloop.organization.application;

import com.bookloop.book.application.CreateBookRequest;
import com.bookloop.book.domain.Book;
import com.bookloop.book.domain.BookRepository;
import com.bookloop.organization.domain.*;
import com.bookloop.shared.exception.ResourceNotFoundException;
import com.bookloop.user.domain.User;
import com.bookloop.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final MembershipRepository membershipRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * Cria uma comunidade e, na MESMA transação: torna o criador OWNER e cadastra
     * o primeiro livro (regra: comunidade não nasce vazia).
     */
    @Transactional
    public OrganizationResponse create(UUID creatorId, CreateOrganizationRequest req) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", creatorId));

        String code = generateUniqueCode(req.name());
        Organization org = Organization.create(code, req.name(), req.description(), creator.getId());
        organizationRepository.save(org);

        membershipRepository.save(Membership.owner(creator.getId(), org.getId()));

        // primeiro livro, já dentro da comunidade
        CreateBookRequest b = req.firstBook();
        Book firstBook = Book.createIn(org.getId(), b.title(), b.author(), b.isbn(), b.genre(),
                b.description(), b.condition(), b.coverUrl(), b.isPublic(), creator);
        bookRepository.save(firstBook);

        log.info("Comunidade criada: orgId={} code={} ownerId={} firstBookId={}",
                org.getId(), code, creator.getId(), firstBook.getId());

        return toResponse(org, Role.OWNER.name());
    }

    /** Lista as comunidades ativas das quais o usuário é membro. */
    @Transactional(readOnly = true)
    public List<OrganizationResponse> listMine(UUID userId) {
        List<Membership> memberships = membershipRepository.findByUserIdAndStatus(userId, MembershipStatus.ACTIVE);
        return memberships.stream()
                .map(m -> {
                    Organization org = organizationRepository.findById(m.getOrganizationId())
                            .orElseThrow(() -> new ResourceNotFoundException("Comunidade", m.getOrganizationId()));
                    return toResponse(org, m.getRole().name());
                })
                .toList();
    }

    /** Detalhe da comunidade ATIVA no contexto da requisição (rota /orgs/{orgId}). */
    @Transactional(readOnly = true)
    public OrganizationResponse getForCurrentMember() {
        UUID orgId = OrganizationContext.id();
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Comunidade", orgId));
        return toResponse(org, OrganizationContext.role().name());
    }

    private OrganizationResponse toResponse(Organization o, String myRole) {
        return new OrganizationResponse(
                o.getId(), o.getCode(), o.getName(), o.getDescription(), o.getAvatarUrl(),
                o.getPlan().name(), o.getMemberLimit(), o.getStatus().name(), myRole, o.getCreatedAt());
    }

    /** Gera um código legível e único: prefixo do nome + sufixo aleatório. */
    private String generateUniqueCode(String name) {
        String base = name.toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (base.length() > 8) base = base.substring(0, 8);
        if (base.isEmpty()) base = "ORG";
        for (int attempt = 0; attempt < 10; attempt++) {
            String code = base + "-" + randomSuffix();
            if (!organizationRepository.existsByCode(code)) return code;
        }
        // fallback improvável: usa UUID curto
        return base + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String randomSuffix() {
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        return sb.toString();
    }
}
