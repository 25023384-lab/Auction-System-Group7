package src;

public interface BiddingStrategy {
    boolean isValidBid(double newBid, double currentBid);
}