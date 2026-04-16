import java.util.ArrayList;
import java.util.List;

public abstract class Item extends Entity {
    protected String name;
    protected double startingPrice;
    protected double currentHighestBid;
    protected String highestBidderId;
    
    // Danh sách các client đang theo dõi sản phẩm này
    private transient List<AuctionObserver> observers = new ArrayList<>();

    public Item(String id, String name, double startingPrice) {
        super(id);
        this.name = name;
        this.startingPrice = startingPrice;
        this.currentHighestBid = startingPrice;
    }

    public abstract void printInfo();

    // Observer Pattern: Đăng ký theo dõi
    public void addObserver(AuctionObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    // Observer Pattern: Thông báo cho tất cả client
    protected void notifyObservers(String message) {
        for (AuctionObserver obs : observers) {
            obs.update(message);
        }
    }

    // Đã được đồng bộ hóa để tránh Race Condition ở cấp độ Item
    public synchronized boolean updateHighestBid(double newBid, String bidderId) {
        if (newBid > currentHighestBid) {
            this.currentHighestBid = newBid;
            this.highestBidderId = bidderId;
            notifyObservers("New highest bid for " + name + ": $" + newBid + " by " + bidderId);
            return true;
        }
        return false;
    }
    
    public double getCurrentHighestBid() { return currentHighestBid; }
}
