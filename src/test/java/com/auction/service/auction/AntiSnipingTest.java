package com.auction.service.auction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

public class AntiSnipingTest {
    private AntiSniping antiSniping;

    @BeforeEach
    void setUp() {
        antiSniping = AntiSniping.getInstance();
        // Cần clear dữ liệu cũ nếu chạy nhiều test do đây là Singleton
        antiSniping.clearData();
    }

    @Test
    void testSyncItemAndGetRemainingSeconds() {
        LocalDateTime end = LocalDateTime.now().plusSeconds(30);
        antiSniping.syncItem("Item1", end);
        
        long remaining = antiSniping.getRemainingSeconds("Item1");
        assertTrue(remaining > 0 && remaining <= 30);
    }

    @Test
    void testCheckAndExtendNotTriggered() {
        // End time is far away (30s) -> should return 0 (no extension needed)
        LocalDateTime end = LocalDateTime.now().plusSeconds(30);
        antiSniping.syncItem("Item2", end);
        
        int result = antiSniping.checkAndExtend("Item2");
        assertEquals(0, result);
    }

    @Test
    void testCheckAndExtendTriggered() {
        // End time is very close (5s) -> should return 1 (extended)
        LocalDateTime end = LocalDateTime.now().plusSeconds(5);
        antiSniping.syncItem("Item3", end);
        
        int result = antiSniping.checkAndExtend("Item3");
        assertEquals(1, result);
        
        // After extension, remaining time should be around 25s
        long remaining = antiSniping.getRemainingSeconds("Item3");
        assertTrue(remaining >= 20);
    }

    @Test
    void testCheckAndExtendAlreadyEnded() {
        // End time is in the past
        LocalDateTime end = LocalDateTime.now().minusSeconds(10);
        antiSniping.syncItem("Item4", end);
        
        int result = antiSniping.checkAndExtend("Item4");
        assertEquals(-1, result); // Means ended
    }
}
