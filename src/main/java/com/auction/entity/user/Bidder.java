package com.auction.entity.user;

public class Bidder extends User {
    private double balance;

    // Constructor không tham số cho Jackson
    public Bidder() {
        super();
    }

    public Bidder(String id, String username, double balance) {
        super(id, username, "BIDDER");
        this.balance = balance;
    }

    @Override
    public void displayProfile() {
        System.out.println("[Bidder] " + getUsername() + " | Balance: $" + balance);
    }

    public double getBalance() { return balance; }

    public void setBalance(double balance) { this.balance = balance; }
}
