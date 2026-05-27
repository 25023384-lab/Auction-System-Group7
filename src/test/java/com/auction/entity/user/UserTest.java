package com.auction.entity.user;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    void testBidderCreation() {
        Bidder bidder = new Bidder("U1", "bidder1", 1000.0);
        assertEquals("U1", bidder.getId());
        assertEquals("bidder1", bidder.getUsername());
        assertEquals(1000.0, bidder.getBalance());
        assertEquals("BIDDER", bidder.getRole());
    }

    @Test
    void testBidderBalanceUpdate() {
        Bidder bidder = new Bidder("U2", "bidder2", 500.0);
        bidder.setBalance(1500.0);
        assertEquals(1500.0, bidder.getBalance());
    }

    @Test
    void testSellerCreation() {
        Seller seller = new Seller("U3", "seller1");
        assertEquals("U3", seller.getId());
        assertEquals("seller1", seller.getUsername());
        assertEquals("SELLER", seller.getRole());
    }
}
