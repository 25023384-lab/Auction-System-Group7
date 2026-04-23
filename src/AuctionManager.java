package src;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuctionManager {
    // Double-Checked Locking Singleton kết hợp volatile để an toàn đa luồng
    private static volatile AuctionManager instance;

    // Sử dụng ConcurrentHashMap để nhiều luồng có thể truy cập các phiên đấu giá khác nhau cùng lúc
    private Map<String, Item> activeAuctions;

    // Sử dụng CopyOnWriteArrayList để an toàn khi vừa duyệt vừa thêm/xóa Observer
    private List<BidObserver> observers;
    private BiddingStrategy strategy;

    private AuctionManager() {
        activeAuctions = new ConcurrentHashMap<>();
        observers = new CopyOnWriteArrayList<>();
        strategy = new DefaultBiddingStrategy();
    }

    public static AuctionManager getInstance() {
        if (instance == null) {
            synchronized (AuctionManager.class) {
                if (instance == null) {
                    instance = new AuctionManager();
                }
            }
        }
        return instance;
    }

    public void addObserver(BidObserver observer) {
        observers.add(observer);
    }

    private void notifyObservers(String itemId, double price) {
        for (BidObserver obs : observers) {
            obs.update(itemId, price);
        }
    }

    public void addItem(Item item) {
        activeAuctions.put(item.getId(), item);
    }

    public Item getItem(String itemId) {
        return activeAuctions.get(itemId);
    }

    /**
     * Xử lý đấu giá: Kết hợp logic an toàn của Nhật và tính năng của Tuấn
     */
    public boolean placeBid(String itemId, String bidderId, double bidAmount) {
        Item item = activeAuctions.get(itemId);

        if (item == null) {
            System.out.println("Item not found: " + itemId);
            return false;
        }

        // TỐI ƯU: Chỉ lock trên đốI tượng Item cụ thể thay vì lock toàn bộ phương thức.
        // Điều này giúp nhiều người có thể đấu giá các món đồ KHÁC NHAU cùng lúc mà không phải chờ nhau.
        synchronized (item) {
            if (strategy.isValidBid(bidAmount, item.getCurrentHighestBid())) {
                boolean success = item.updateHighestBid(bidAmount, bidderId);
                if (success) {
                    System.out.println("Bid SUCCESS: " + bidderId + " -> $" + bidAmount + " on " + itemId);

                    // Kích hoạt thông báo cho những người đang quan tâm (Observer)
                    notifyObservers(itemId, bidAmount);

                    // Chống sniping: Tự động gia hạn thời gian nếu thầu sát giờ cuối
                    checkAndExtendAuctionTime(itemId);
                    return true;
                }
            }
        }

        System.out.println("Bid FAILED for " + bidderId + ": Amount too low or invalid.");
        return false;
    }

    private void checkAndExtendAuctionTime(String itemId) {
        // TODO: Logic cộng thêm thời gian đấu giá nếu cần
    }
}