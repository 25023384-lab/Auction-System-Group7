package com.auction.entity.items;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

public class ItemTest {

    @Test
    void testElectronicsCreation() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);
        Electronics item = new Electronics("E1", "Laptop", "Gaming Laptop", 1000.0, start, end, "Seller1", 12);
        
        assertEquals("E1", item.getId());
        assertEquals("Laptop", item.getName());
        assertEquals(1000.0, item.getStartingPrice());
        assertEquals("ELECTRONICS", item.getType());
        assertEquals(12, item.getWarrantyMonths());
        assertEquals(Item.Status.OPEN, item.getStatus());
    }

    @Test
    void testUpdateHighestBid() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);
        Art item = new Art("A1", "Painting", "Mona Lisa", 5000.0, start, end, "Seller2", "Da Vinci");

        boolean success = item.updateHighestBid(6000.0, "Bidder1");
        assertTrue(success);
        assertEquals(6000.0, item.getCurrentHighestBid());
        assertEquals("Bidder1", item.getHighestBidderId());

        // Bid lower than current highest should fail
        boolean fail = item.updateHighestBid(5500.0, "Bidder2");
        assertFalse(fail);
        assertEquals(6000.0, item.getCurrentHighestBid());
        assertEquals("Bidder1", item.getHighestBidderId());
    }
}
