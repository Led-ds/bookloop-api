package com.bookloop.book.domain;

import com.bookloop.shared.exception.BusinessException;
import com.bookloop.user.domain.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Transições de status do livro: AVAILABLE -> RESERVED -> RENTED -> AVAILABLE. */
class BookStatusTest {

    private Book newBook() {
        User owner = User.register("Dono", "dono@bookloop.dev", "hash");
        return Book.create("Clean Code", "Robert Martin", "9780132350884",
                Genre.TECNICO, "desc", BookCondition.BOM, "https://img/x.png", true, owner);
    }

    @Test
    void startsAvailable() {
        assertEquals(BookStatus.AVAILABLE, newBook().getStatus());
    }

    @Test
    void approveReservesThenPickupRents() {
        Book b = newBook();
        b.markReserved();
        assertEquals(BookStatus.RESERVED, b.getStatus());
        b.markRented();
        assertEquals(BookStatus.RENTED, b.getStatus());
        assertTrue(b.isRented());
    }

    @Test
    void returnMakesAvailableAgain() {
        Book b = newBook();
        b.markReserved();
        b.markRented();
        b.markReturned();
        assertEquals(BookStatus.AVAILABLE, b.getStatus());
    }

    @Test
    void cannotReserveTwice() {
        Book b = newBook();
        b.markReserved();
        assertThrows(BusinessException.class, b::markReserved);
    }

    @Test
    void hidingTogglesVisibilityNotStatus() {
        Book b = newBook();
        b.changeVisibility(true);
        assertFalse(b.isPublic());
        assertEquals(BookStatus.AVAILABLE, b.getStatus());
    }
}
