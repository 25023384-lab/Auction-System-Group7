package com.auction.service.bidding;

import java.sql.SQLException;
import com.auction.dao.BidTransactionDAO;
import com.auction.dao.ItemDAO;
import com.auction.dao.UserDAO;

import com.auction.exception.InvalidBidException;
import com.auction.exception.AuctionClosedException;

import java.util.List;
import java.util.Map;

import com.auction.entity.bid.BidTransaction;
import com.auction.entity.items.Item;
import com.auction.entity.user.Bidder;
import com.auction.service.auction.AntiSniping;
import com.auction.service.strategy.BiddingStrategy;
import com.auction.service.notification.NotificationService;
import com.auction.service.analytics.AnalyticsService;

public class BiddingService {
    private Map<String, Item> activeAuctions;
    private Map<String, Bidder> bidders;
    private BiddingStrategy strategy;
    private AntiSniping antiSniping;
    private AnalyticsService analyticsService;
    private NotificationService notificationService;
    private AutoBidder autoBidder;
    private List<BidTransaction> transactionHistory;
    private BidTransactionDAO bidDAO = new BidTransactionDAO();
    private ItemDAO itemDAO = new ItemDAO();
    private UserDAO userDAO = new UserDAO();

    public BiddingService(Map<String, Item> activeAuctions, Map<String, Bidder> bidders,
                          BiddingStrategy strategy, AntiSniping antiSniping,
                          AnalyticsService analyticsService, NotificationService notificationService,
                          AutoBidder autoBidder, List<BidTransaction> transactionHistory) {
        this.activeAuctions = activeAuctions;
        this.bidders = bidders;
        this.strategy = strategy;
        this.antiSniping = antiSniping;
        this.analyticsService = analyticsService;
        this.notificationService = notificationService;
        this.autoBidder = autoBidder;
        this.transactionHistory = transactionHistory;
    }

    public boolean placeBid(String itemId, String bidderId, double bidAmount)
            throws InvalidBidException, AuctionClosedException {
        Item item = findActiveItem(itemId);
        checkAntiSniping(itemId, item);

        synchronized (item) {
            Bidder bidder = resolveBidder(bidderId);
            validateAndExecuteBid(item, bidder, itemId, bidAmount, bidderId);
            return true;
        }
    }

    /** Tìm item đang chạy trong activeAuctions; ném AuctionClosedException nếu không hợp lệ. */
    private Item findActiveItem(String itemId) throws AuctionClosedException {
        Item item = activeAuctions.get(itemId);
        if (item == null) {
            throw new AuctionClosedException("Item not found: " + itemId);
        }
        if (item.getStatus() != Item.Status.RUNNING) {
            throw new AuctionClosedException("Auction is not active (status: " + item.getStatus() + ").");
        }
        return item;
    }

    /** Kiểm tra và gia hạn thời gian nếu bid xuất hiện trong 10 giây cuối (Anti-Sniping). */
    private void checkAntiSniping(String itemId, Item item) throws AuctionClosedException {
        if (antiSniping.getRemainingSeconds(itemId) == -1) {
            antiSniping.syncItem(itemId, item.getEndTime());
        }

        int snipingResult = antiSniping.checkAndExtend(itemId);
        if (snipingResult == -1) {
            throw new AuctionClosedException("Auction ended for \"" + item.getName() + "\"! Cannot bid.");
        } else if (snipingResult == 1) {
            long rem = antiSniping.getRemainingSeconds(itemId);
            java.time.LocalDateTime newEnd = java.time.LocalDateTime.now().plusSeconds(rem);
            item.setEndTime(newEnd);
            try {
                itemDAO.save(item);
                notificationService.notifyExtension(itemId, rem);
            } catch (SQLException ignored) {}
        }
    }

    /** Tìm Bidder trong RAM; fallback xuống DB nếu chưa được cache. */
    private Bidder resolveBidder(String bidderId) throws InvalidBidException {
        Bidder bidder = bidders.get(bidderId);
        if (bidder == null) {
            try {
                UserDAO.UserRecord rec = userDAO.findById(bidderId);
                if (rec != null && "BIDDER".equals(rec.role)) {
                    bidder = new Bidder(rec.id, rec.username, rec.balance);
                    bidders.put(bidderId, bidder);
                }
            } catch (SQLException e) {
                System.out.println("DB lookup bidder failed: " + e.getMessage());
            }
        }
        if (bidder == null) {
            throw new InvalidBidException("Bidder not found: " + bidderId);
        }
        return bidder;
    }

    /** Lưu bid thành công vào DB (mark lost → save tx → update item price). */
    private void saveBidToDatabase(String itemId, String bidderId, double bidAmount, BidTransaction tx) {
        bidDAO.markAllLost(itemId);
        try {
            bidDAO.saveBid(tx);
        } catch (Exception e) {
            System.out.println("DB save bid failed: " + e.getMessage());
        }
        try {
            itemDAO.updateCurrentBid(itemId, bidAmount, bidderId);
        } catch (java.sql.SQLException e) {
            System.out.println("⚠️ DB update item failed: " + e.getMessage());
        }
    }

    /** Validate bid và thực thi toàn bộ logic sau khi bid thành công. */
    private void validateAndExecuteBid(Item item, Bidder bidder, String itemId,
                                       double bidAmount, String bidderId)
            throws InvalidBidException {
        if (!strategy.isValidBid(item.getCurrentHighestBid(), bidAmount)) {
            throw new InvalidBidException("Bid amount too low or invalid. Current highest bid: $"
                    + String.format("%.2f", item.getCurrentHighestBid())
                    + ", your bid: $" + String.format("%.2f", bidAmount));
        }

        boolean success = item.updateHighestBid(bidAmount, bidderId);
        if (!success) {
            throw new InvalidBidException("Bid could not update highest bid. Amount too low or invalid.");
        }

        System.out.println("Bid SUCCESS: " + bidderId + " -> $" + bidAmount + " on " + itemId);

        // Ghi analytics
        analyticsService.recordBid(itemId, bidAmount);

        // Tạo transaction và lưu DB TRƯỚC KHI trigger auto-bid
        BidTransaction tx = new BidTransaction(itemId, bidderId, bidAmount);
        tx.markAsWinning();
        saveBidToDatabase(itemId, bidderId, bidAmount, tx);

        // Lưu vào lịch sử RAM
        markPreviousTransactionsAsLost(itemId);
        transactionHistory.add(tx);

        // Notify observers (BidObserver pattern)
        notificationService.notifyObservers(itemId, bidAmount, bidderId);
        // NOTE: notifyRealtime() đã bị bỏ — BidHandler.handleBid() sẽ broadcast BID_UPDATE
        //       với đầy đủ thông tin (có kèm bidderName), tránh client nhận 2 lần.

        // Trigger auto-bid BẤT ĐỒNG BỘ sau khi đã lưu DB xong
        // Tránh lỗi đệ quy ngược (stack unwinding) ghi đè kết quả auto-bid
        if (autoBidder != null) {
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                autoBidder.onNewBid(itemId, bidderId, bidAmount);
            });
        }
    }

    // Đánh dấu tất cả transaction cũ của item này là không thắng
    private void markPreviousTransactionsAsLost(String itemId) {
        for (BidTransaction tx : transactionHistory) {
            if (tx.getItemId().equals(itemId) && tx.isWinning()) {
                tx.markAsLost();  // Cần thêm method này trong BidTransaction
            }
        }
    }

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
}
