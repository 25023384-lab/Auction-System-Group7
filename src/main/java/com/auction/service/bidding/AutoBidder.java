package com.auction.service.bidding;

import com.auction.service.auction.AuctionManager;
import com.auction.entity.items.Item;

import java.util.*;
import java.util.concurrent.*;

public class AutoBidder {

    private static class AutoConfig {
        String bidderId;
        double maxBid;
        double increment;
        long registeredAt;

        AutoConfig(String bidderId,
                double maxBid, double increment) {
            this.bidderId = bidderId;
            this.maxBid = maxBid;
            this.increment = increment;
            this.registeredAt = System.currentTimeMillis();
        }
    }

    private final Map<String, List<AutoConfig>> configs = new ConcurrentHashMap<>();

    private final AuctionManager manager;

    public AutoBidder(AuctionManager manager) {
        this.manager = manager;
    }

    public void register(
            String bidderId, String itemId,
            double maxBid, double increment) {

        synchronized (this) {
            configs.computeIfAbsent(itemId, k -> new CopyOnWriteArrayList<>())
                    .add(new AutoConfig(bidderId, maxBid, increment));
        }

        System.out.println("🤖 [AutoBid-Register] User " + bidderId
                + " registered for item " + itemId + " with max bid $" + maxBid + " and increment $" + increment);

        // Tự động kích hoạt thầu lượt đầu tiên nếu chưa dẫn đầu
        if (manager != null) {
            Item item = manager.getItem(itemId);
            if (item != null) {
                double currentPrice = item.getCurrentHighestBid();
                String currentWinner = item.getHighestBidderId();

                if (!bidderId.equals(currentWinner)) {
                    double nextBid = currentPrice + increment;
                    if (nextBid <= maxBid) {
                        System.out.println("🤖 [AutoBid-FirstTrigger] User " + bidderId + " is not the highest bidder. Triggering first auto-bid of $" + nextBid);
                        try {
                            manager.placeBid(itemId, bidderId, nextBid);
                        } catch (Exception e) {
                            System.err.println("🤖 [AutoBid-FirstTrigger] Failed to place first bid: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    public void onNewBid(String itemId, String winnerBidderId, double currentPrice) {
        List<AutoConfig> list = configs.get(itemId);
        if (list == null || list.isEmpty()) {
            // No auto-bids configured for this item.
            return;
        }
        
        System.out.println("\n🤖 [AutoBid-Trigger] New bid of $" + currentPrice + " on item " + itemId + " by " + winnerBidderId + ". Checking auto-bidders...");

        // Sort by registration time to give priority to the first person who registered.
        list.sort(Comparator.comparingLong(c -> c.registeredAt));

        for (AutoConfig cfg : list) {
            System.out.println("   [AutoBid-Check] Evaluating config for User " + cfg.bidderId + " (Max: $" + cfg.maxBid + ", Increment: $" + cfg.increment + ")");

            if (cfg.bidderId.equals(winnerBidderId)) {
                System.out.println("      -> Skipping: This user is already the highest bidder.");
                continue;
            }

            double nextBid = currentPrice + cfg.increment;
            System.out.println("      -> Calculated next potential bid: $" + String.format("%.2f", nextBid));

            if (nextBid <= cfg.maxBid) {
                System.out.println("      -> Decision: Next bid is within the max limit. Placing bid...");
                try {
                    // This call is recursive: an auto-bid triggers onNewBid again.
                    boolean ok = manager.placeBid(itemId, cfg.bidderId, nextBid);
                    if (ok) {
                        System.out.println("      -> SUCCESS: Auto-bid placed for " + cfg.bidderId + ". Breaking loop for this round.");
                        // Once a successful auto-bid is placed, we stop for this round.
                        // The new bid will trigger this whole process again if needed.
                        break; 
                    }
                } catch (Exception e) {
                    System.err.println("      -> FAILED: Auto-bid for " + cfg.bidderId + " failed: " + e.getMessage());
                }
            } else {
                System.out.println("      -> Decision: Next bid of $" + nextBid + " would exceed the max bid of $" + cfg.maxBid + ". Stopping for this user.");
            }
        }
        System.out.println("🤖 [AutoBid-Trigger] Finished checking all auto-bidders for this round.\n");
    }

    public void unregister(String itemId) {
        configs.remove(itemId);
    }
}
