package com.bookloop.organization.domain;

/**
 * Planos de comunidade. Na v2.0 o limite é apenas informativo/estrutural;
 * a cobrança em si fica para depois.
 */
public enum Plan {
    STARTER,    // até ~20 membros
    COMMUNITY,  // até ~50
    CLUB,       // até ~100
    UNLIMITED   // sem limite
}
