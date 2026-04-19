<<<<<<< HEAD
public class Bidder extends User {
    private double balance;

    public Bidder(String id, String username, double balance) {
        super(id, username, "BIDDER");
        this.balance = balance;
    }

    @Override
    public void displayProfile() {
        System.out.println("[Bidder] " + username + " | Balance: $" + balance);
    }

    public double getBalance() { return balance; }
=======
package src;

public class Bidder implements BidObserver {
    private String name;

    public Bidder(String name) {
        this.name = name;
    }

    @Override
    public void update(String itemId, double newBid) {
        System.out.println(name + " nhận thông báo: Item " + itemId + " có giá mới: " + newBid);
    }
>>>>>>> 2bf0db4 (Tuần 7: Update về xử lý luồng)
}