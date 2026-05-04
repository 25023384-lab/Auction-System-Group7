package src;

public interface BidObserver {
    void update(String itemId, double newBid);
}