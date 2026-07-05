package com.bookloop.home.application;

import com.bookloop.book.application.BookSummaryResponse;

import java.util.List;

public record PublicHomeResponse(
        HomeStats stats,
        List<BookSummaryResponse> featuredBooks,
        List<BookSummaryResponse> communityBooks,
        List<RecentActivityResponse> recentActivities,
        List<Object> reviews,        // vazio: sem domínio de avaliação ainda
        List<Object> topReaders,     // vazio: sem ranking ainda
        BookSummaryResponse bookOfTheWeek
) {}
