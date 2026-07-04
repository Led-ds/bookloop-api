package com.bookloop.shared.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes do envelope de resposta. Puro (sem contexto Spring/DB) — garante o contrato
 * {success, message, data, timestamp} usado por todos os endpoints e pelo frontend.
 */
class ApiResponseTest {

    @Test
    void okWrapsDataWithSuccessTrueAndNoMessage() {
        ApiResponse<String> res = ApiResponse.ok("hello");
        assertTrue(res.success());
        assertEquals("hello", res.data());
        assertNull(res.message());
        assertNotNull(res.timestamp());
    }

    @Test
    void okWithMessageKeepsBothDataAndMessage() {
        ApiResponse<String> res = ApiResponse.ok("data", "Livro cadastrado.");
        assertTrue(res.success());
        assertEquals("Livro cadastrado.", res.message());
        assertEquals("data", res.data());
    }

    @Test
    void errorHasSuccessFalseMessageAndNullData() {
        ApiResponse<Void> res = ApiResponse.error("Ocorreu um erro inesperado.");
        assertFalse(res.success());
        assertEquals("Ocorreu um erro inesperado.", res.message());
        assertNull(res.data());
    }
}
