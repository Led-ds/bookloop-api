package com.bookloop.book.application;

import com.bookloop.book.domain.BookCondition;
import com.bookloop.book.domain.Genre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateBookRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 160) String author,
        @Size(max = 20) String isbn,
        @NotNull Genre genre,
        @Size(max = 2000) String description,
        @NotNull BookCondition condition,
        @Pattern(regexp = "^$|^https?://.*", message = "coverUrl deve ser uma URL http(s)") String coverUrl,
        boolean isPublic
) {}
