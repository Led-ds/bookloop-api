package com.bookloop.shared.domain;

import org.springframework.data.domain.AbstractAggregateRoot;

/**
 * Marker base that lets rich aggregates register domain events
 * (e.g. BookRented, BookReturned) which Spring publishes after save.
 */
public abstract class AggregateRoot<T extends AggregateRoot<T>> extends AbstractAggregateRoot<T> {
}
