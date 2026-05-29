package com.auction.event;

public interface BidObserver {
    void update(String itemId, double newBid, String bidderId);
}
