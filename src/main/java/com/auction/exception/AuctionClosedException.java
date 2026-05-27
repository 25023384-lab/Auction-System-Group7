package com.auction.exception;

/**
 * Exception thrown when a bid is placed on an auction that is not running (e.g. pending, finished, paid).
 */
public class AuctionClosedException extends AuctionException {
    public AuctionClosedException(String message) {
        super(message);
    }

    public AuctionClosedException(String message, Throwable cause) {
        super(message, cause);
    }
}
