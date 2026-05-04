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

    // === Các component mới ===
    private RealtimeNotifier realtime;
    private AntiSniping antiSniping;
    private BidAnalytics analytics;
    private List<BidTransaction> transactionHistory;

    private AuctionManager() {
        activeAuctions = new ConcurrentHashMap<>();
        observers = new CopyOnWriteArrayList<>();
        strategy = new DefaultBiddingStrategy();

        // Khởi tạo các component mới
        realtime = RealtimeNotifier.getInstance();
        antiSniping = AntiSniping.getInstance();
        analytics = BidAnalytics.getInstance();
        transactionHistory = new CopyOnWriteArrayList<>();
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

    private void notifyObservers(String itemId, double price, String bidderId) {
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

    // === Advanced: Đăng ký theo dõi item realtime ===
    public void watchItem(String itemId, Bidder bidder) {
        realtime.watchItem(itemId, bidder);
    }

    // === Advanced: Bắt đầu đếm ngược ===
    public void startCountdown(String itemId, int seconds) {
        antiSniping.startAuction(itemId, seconds);
        realtime.startCountdown(itemId, seconds, () -> {
            System.out.println("🏆 AUCTION CLOSED for " + itemId);
            Item item = activeAuctions.get(itemId);
            if (item != null) {
                System.out.println("Winner: " + item.getHighestBidderId() + " | $" + item.getCurrentHighestBid());
            }
        });
    }

    // === CORE METHOD: Xử lý đấu giá ===
    public boolean placeBid(String itemId, String bidderId, double bidAmount) {
        Item item = activeAuctions.get(itemId);

        if (item == null) {
            System.out.println("Item not found: " + itemId);
            return false;
        }

        // Kiểm tra chống sniping
        if (!antiSniping.checkAndExtend(itemId)) {
            System.out.println("Auction ended for " + itemId + "! Cannot bid.");
            return false;
        }

        synchronized (item) {
            if (strategy.isValidBid(bidAmount, item.getCurrentHighestBid())) {
                boolean success = item.updateHighestBid(bidAmount, bidderId);
                if (success) {
                    System.out.println("Bid SUCCESS: " + bidderId + " -> $" + bidAmount + " on " + itemId);

                    // Ghi analytics
                    analytics.recordBid(itemId, bidAmount);

                    // Gửi realtime notification
                    realtime.notifyRealtime(itemId, bidAmount, bidderId);

                    // Đánh dấu các transaction cũ không còn thắng
                    markPreviousTransactionsAsLost(itemId);

                    // Lưu transaction
                    BidTransaction tx = new BidTransaction(itemId, bidderId, bidAmount);
                    tx.markAsWinning();
                    transactionHistory.add(tx);


                    // Notify observers
                    notifyObservers(itemId, bidAmount, bidderId);

                    return true;
                }
            }
        }

        System.out.println("Bid FAILED for " + bidderId + ": Amount too low or invalid.");
        return false;
    }

    // Đánh dấu tất cả transaction cũ của item này là không thắng
    private void markPreviousTransactionsAsLost(String itemId) {
        for (BidTransaction tx : transactionHistory) {
            if (tx.getItemId().equals(itemId) && tx.isWinning()) {
                tx.markAsLost();  // Cần thêm method này trong BidTransaction
            }
        }
    }

    // === Advanced: Thống kê ===
    public void showAnalytics(String itemId) {
        analytics.printStats(itemId);
    }

    // === Advanced: Lấy thời gian còn lại ===
    public long getRemainingTime(String itemId) {
        return antiSniping.getRemainingSeconds(itemId);
    }

    // === Advanced: Xem lịch sử giao dịch ===
    public void printTransactionHistory(String itemId) {
        System.out.println("📜 TRANSACTION HISTORY for " + itemId + ":");
        boolean found = false;
        for (BidTransaction tx : transactionHistory) {
            if (tx.getItemId().equals(itemId)) {
                tx.printInfo();
                found = true;
            }
        }
        if (!found) {
            System.out.println("   No transactions yet.");
        }
    }

    // === Advanced: Xem tất cả lịch sử ===
    public void printAllTransactions() {
        System.out.println("📜 ALL TRANSACTIONS:");
        if (transactionHistory.isEmpty()) {
            System.out.println("   No transactions yet.");
        } else {
            for (BidTransaction tx : transactionHistory) {
                tx.printInfo();
            }
        }
    }

    // === Helper: Kiểm tra item còn đấu giá không ===
    public boolean isAuctionActive(String itemId) {
        return antiSniping.isAuctionActive(itemId);
    }
}