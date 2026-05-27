package com.auction.exception;

/**
 * Exception thrown when authentication fails (e.g., invalid username or password).
 */
public class AuthenticationException extends AuctionException {
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
