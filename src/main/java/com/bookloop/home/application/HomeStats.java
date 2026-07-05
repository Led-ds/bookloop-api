package com.bookloop.home.application;

public record HomeStats(
        long totalBooks,
        long totalUsers,
        long totalRentals,
        long availableBooks,
        Double averageRating   // null enquanto não houver domínio de avaliação
) {}
