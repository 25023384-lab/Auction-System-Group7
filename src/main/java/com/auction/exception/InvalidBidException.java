package com.auction.exception;

/**
 * Exception thrown when a bid amount is invalid (e.g., lower than starting/current price,
 * insufficient bidder balance, etc.).
 */
public class InvalidBidException extends AuctionException {
    public InvalidBidException(String message) {
        super(message);
    }

    public InvalidBidException(String message, Throwable cause) {
        super(message, cause);
    }
}
