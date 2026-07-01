package com.bookloop.book.application;

import java.util.UUID;

/** Lightweight owner projection embedded in book responses. */
public record BookOwnerView(UUID id, String name, String avatarUrl, String location, int penaltiesCount) {}
