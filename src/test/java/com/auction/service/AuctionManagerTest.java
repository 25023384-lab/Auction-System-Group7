package com.auction.service;

import com.auction.service.auction.AuctionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.auction.entity.user.Bidder;
import com.auction.entity.items.Electronics;
import com.auction.entity.items.Item;
import com.auction.exception.InvalidBidException;
import com.auction.exception.AuctionClosedException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionManagerTest {
    private AuctionManager manager;

    @BeforeEach
    void setUp() {
        manager = new AuctionManager();
    }

    @Test
    void testAddItem() {
        Item item = new Electronics("I1", "Laptop", "A laptop", 1000.0, LocalDateTime.now(), LocalDateTime.now().plusDays(1), 12);
        manager.addItem(item);
        assertEquals(item, manager.getItem("I1"));
    }

    @Test
    void testPlaceBid() throws Exception {
        Item item = new Electronics("I1", "Laptop", "A laptop", 1000.0, LocalDateTime.now(), LocalDateTime.now().plusDays(1), 12);
        item.setStatus(Item.Status.RUNNING); // Đảm bảo trạng thái RUNNING
        manager.addItem(item);
        Bidder bidder = new Bidder("B1", "bidder", 2000.0);
        manager.addBidder(bidder);

        manager.startCountdown("I1", 60); // Start auction

        boolean success = manager.placeBid("I1", "B1", 1100.0);
        assertTrue(success);
        assertEquals(1100.0, item.getCurrentHighestBid());
    }

    @Test
    void testPlaceBidLowAmountThrowsException() {
        Item item = new Electronics("I1", "Laptop", "A laptop", 1000.0, LocalDateTime.now(), LocalDateTime.now().plusDays(1), 12);
        item.setStatus(Item.Status.RUNNING);
        manager.addItem(item);
        Bidder bidder = new Bidder("B1", "bidder", 2000.0);
        manager.addBidder(bidder);

        manager.startCountdown("I1", 60);

        assertThrows(InvalidBidException.class, () -> {
            manager.placeBid("I1", "B1", 900.0);
        });
    }

    @Test
    void testPlaceBidClosedAuctionThrowsException() {
        Item item = new Electronics("I1", "Laptop", "A laptop", 1000.0, LocalDateTime.now(), LocalDateTime.now().plusDays(1), 12);
        item.setStatus(Item.Status.FINISHED); // Trạng thái đấu giá đã kết thúc
        manager.addItem(item);
        Bidder bidder = new Bidder("B1", "bidder", 2000.0);
        manager.addBidder(bidder);

        assertThrows(AuctionClosedException.class, () -> {
            manager.placeBid("I1", "B1", 1100.0);
        });
    }
}
