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
}