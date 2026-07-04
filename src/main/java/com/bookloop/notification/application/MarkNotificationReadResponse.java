package com.bookloop.notification.application;

import java.time.Instant;
import java.util.UUID;

public record MarkNotificationReadResponse(UUID id, boolean read, Instant readAt) {}
