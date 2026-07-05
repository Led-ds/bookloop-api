package com.bookloop.home.application;

import java.time.Instant;

/** Forma estável para a vitrine; hoje derivada de livros recém-cadastrados. */
public record RecentActivityResponse(String type, String message, Instant at) {}
