<<<<<<< HEAD
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
=======
package src;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
>>>>>>> 2bf0db4 (Tuần 7: Update về xử lý luồng)

public class AuctionManager {
    private static volatile AuctionManager instance;

    // Sử dụng ConcurrentHashMap để thread-safe
    private Map<String, Item> activeAuctions;

    private List<BidObserver> observers;
    private BiddingStrategy strategy;

    private AuctionManager() {
<<<<<<< HEAD
        activeAuctions = new ConcurrentHashMap<>();
=======
        activeAuctions = new HashMap<>();
        observers = new CopyOnWriteArrayList<>();
        strategy = new DefaultBiddingStrategy();
>>>>>>> 2bf0db4 (Tuần 7: Update về xử lý luồng)
    }

    // Double-Checked Locking Singleton Pattern [cite: 141]
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
<<<<<<< HEAD
    /**
     * Lấy đối tượng Item đang được đấu giá dựa vào ID
     */
    public Item getItem(String itemId) {
        return activeAuctions.get(itemId);
    }
    // Xử lý đấu giá đồng thời an toàn
    public boolean placeBid(String itemId, String bidderId, double bidAmount) {
        Item item = activeAuctions.get(itemId);
        if (item == null) {
            System.out.println("Item not found!");
            return false;
        }

        // Lock trên từng sản phẩm thay vì lock toàn bộ Manager để tăng hiệu năng
        synchronized (item) {
            if (bidAmount > item.getCurrentHighestBid()) {
                boolean success = item.updateHighestBid(bidAmount, bidderId);
                if (success) {
                    System.out.println(" Bid accepted: " + bidderId + " bid $" + bidAmount + " on " + itemId);
                    checkAndExtendAuctionTime(itemId); // Logic anti-sniping
                    return true;
                }
            }
        }
        System.out.println(" Bid failed for " + bidderId + ": Amount too low.");
=======

    public synchronized boolean placeBid(String itemId, String bidderId, double bidAmount) {
        Item item = activeAuctions.get(itemId);

        if (item != null && strategy.isValidBid(bidAmount, item.getCurrentHighestBid())) {
            item.updateHighestBid(bidAmount);

            System.out.println("Bid SUCCESS: " + bidderId + " -> " + bidAmount);

            notifyObservers(itemId, bidAmount); // 🔥 Observer

            return true;
        }

        System.out.println("Bid FAILED by " + bidderId);
>>>>>>> 2bf0db4 (Tuần 7: Update về xử lý luồng)
        return false;
    }

    private void checkAndExtendAuctionTime(String itemId) {
        // TODO: Cài đặt logic kiểm tra nếu thời gian hiện tại < X giây so với giờ kết thúc
        // thì cộng thêm Y giây vào thời gian kết thúc của phiên đấu giá[cite: 90].
    }
}
