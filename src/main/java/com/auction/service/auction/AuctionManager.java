package com.auction.service.auction;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.auction.entity.bid.BidTransaction;
import com.auction.entity.items.Item;
import com.auction.event.BidObserver;
import com.auction.entity.user.Bidder;
import com.auction.service.analytics.AnalyticsService;
import com.auction.service.bidding.AutoBidder;
import com.auction.service.bidding.BiddingService;
import com.auction.service.notification.NotificationService;
import com.auction.service.strategy.BiddingStrategy;
import com.auction.service.strategy.DefaultBiddingStrategy;

import com.auction.exception.InvalidBidException;
import com.auction.exception.AuctionClosedException;

public class AuctionManager {
    private Map<String, Item> activeAuctions;
    private BiddingStrategy strategy;
    private AntiSniping antiSniping;
    private List<BidTransaction> transactionHistory;
    private AutoBidder autoBidder;
    private Map<String, Bidder> bidders;
    private NotificationService notificationService;
    private AnalyticsService analyticsService;
    private BiddingService biddingService;

    public AuctionManager() {
        activeAuctions = new ConcurrentHashMap<>();
        strategy = new DefaultBiddingStrategy();
        antiSniping = AntiSniping.getInstance();
        transactionHistory = new CopyOnWriteArrayList<>();
        bidders = new ConcurrentHashMap<>();
        notificationService = new NotificationService();
        analyticsService = new AnalyticsService();
        autoBidder = new AutoBidder(this);
        biddingService = new BiddingService(activeAuctions, bidders, strategy, antiSniping,
                analyticsService, notificationService, autoBidder, transactionHistory);
    }

    public void setAutoBidder(AutoBidder autoBidder) {
        this.autoBidder = autoBidder;
        biddingService = new BiddingService(activeAuctions, bidders, strategy, antiSniping,
                analyticsService, notificationService, autoBidder, transactionHistory);
    }

    public void addBidder(Bidder bidder) {
        bidders.put(bidder.getId(), bidder);
    }

    public Bidder getBidder(String bidderId) {
        return bidders.get(bidderId);
    }

    public void addObserver(BidObserver observer) {
        notificationService.addObserver(observer);
    }

    public void addItem(Item item) {
        activeAuctions.put(item.getId(), item);
    }

    public Item getItem(String itemId) {
        return activeAuctions.get(itemId);
    }

    public void updateItemStatus(Item item) {
        if (activeAuctions.containsKey(item.getId())) {
            activeAuctions.put(item.getId(), item);
        }
    }

    public void watchItem(String itemId, Bidder bidder) {
        notificationService.watchItem(itemId, bidder);
    }

    public void startCountdown(String itemId, int seconds) {
        notificationService.startCountdown(itemId, seconds, () -> {
            System.out.println("🏆 AUCTION CLOSED for " + itemId);
            Item item = activeAuctions.get(itemId);
            if (item != null) {
                System.out.println("Winner: " + item.getHighestBidderId() + " | $" + item.getCurrentHighestBid());
            }
        });
    }

    public boolean placeBid(String itemId, String bidderId, double bidAmount)
            throws InvalidBidException, AuctionClosedException {
        return biddingService.placeBid(itemId, bidderId, bidAmount);
    }

    public void showAnalytics(String itemId) {
        analyticsService.printStats(itemId);
    }

    public long getRemainingTime(String itemId) {
        return antiSniping.getRemainingSeconds(itemId);
    }

    public void printTransactionHistory(String itemId) {
        biddingService.printTransactionHistory(itemId);
    }

    public void printAllTransactions() {
        biddingService.printAllTransactions();
    }

    public boolean isAuctionActive(String itemId) {
        return antiSniping.isAuctionActive(itemId);
    }
}
