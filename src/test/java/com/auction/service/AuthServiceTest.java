package com.auction.service;

import com.auction.service.auth.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.auction.entity.user.User;
import com.auction.exception.AuthenticationException;

public class AuthServiceTest {
    private AuthService authService;

    @org.junit.jupiter.api.BeforeAll
    static void initDB() {
        com.auction.util.DBHelper.initializeDatabase();
    }

    @BeforeEach
    void setUp() {
        authService = new AuthService();
    }

    @Test
    void testRegisterUser() {
        String uniqueUser = "testuser_" + System.currentTimeMillis();
        boolean result = authService.register(uniqueUser, uniqueUser + "@email.com", "password", "BIDDER");
        assertTrue(result);
    }

    @Test
    void testLoginUser() throws Exception {
        String uniqueUser = "loginuser_" + System.currentTimeMillis();
        authService.register(uniqueUser, uniqueUser + "@email.com", "password", "BIDDER");
        User result = authService.login(uniqueUser, "password");
        assertNotNull(result);
        assertEquals(uniqueUser, result.getUsername());
    }

    @Test
    void testLoginIncorrectPasswordThrowsException() {
        String uniqueUser = "loginwrongpw_" + System.currentTimeMillis();
        authService.register(uniqueUser, uniqueUser + "@email.com", "password", "BIDDER");
        
        assertThrows(AuthenticationException.class, () -> {
            authService.login(uniqueUser, "wrongpassword");
        });
    }

    @Test
    void testLoginNonExistentUserThrowsException() {
        assertThrows(AuthenticationException.class, () -> {
            authService.login("nonexistentuser_123", "password");
        });
    }
}