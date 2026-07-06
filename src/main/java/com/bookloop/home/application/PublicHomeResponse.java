package com.bookloop.home.application;

import com.bookloop.book.application.BookSummaryResponse;
import com.bookloop.review.application.ReviewResponse;
import com.bookloop.review.application.TopReaderResponse;

import java.util.List;

public record PublicHomeResponse(
        HomeStats stats,
        List<BookSummaryResponse> featuredBooks,
        List<BookSummaryResponse> communityBooks,
        List<RecentActivityResponse> recentActivities,
        List<ReviewResponse> reviews,
        List<TopReaderResponse> topReaders,
        BookSummaryResponse bookOfTheWeek
) {}
