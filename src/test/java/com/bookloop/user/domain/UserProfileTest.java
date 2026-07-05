package com.bookloop.user.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserProfileTest {

    private User newUser() {
        return User.register("Ana", "ana@bookloop.dev", "hash");
    }

    @Test
    void updatesProfileFields() {
        User u = newUser();
        u.updateProfile("Ana Paula", "leitora voraz", "São Paulo", "SP",
                "Rua X, 100", "Centro", "01000-000", "https://img/a.png");
        assertEquals("Ana Paula", u.getName());
        assertEquals("São Paulo", u.getCity());
        assertEquals("SP", u.getState());
        assertEquals("Centro", u.getNeighborhood());
    }

    @Test
    void profileCompletedRequiresBioCityState() {
        User u = newUser();
        u.updateProfile("Ana", "bio", "São Paulo", "SP", null, null, null, null);
        assertTrue(u.isProfileCompleted());
    }

    @Test
    void profileIncompleteWhenMissingState() {
        User u = newUser();
        u.updateProfile("Ana", "bio", "São Paulo", null, null, null, null, null);
        assertFalse(u.isProfileCompleted());
    }
}
