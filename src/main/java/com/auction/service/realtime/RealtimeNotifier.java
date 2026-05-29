package com.auction.service.realtime;

import com.auction.dao.ItemDAO;
import com.auction.dao.UserDAO;
import com.auction.entity.items.Item;
import com.auction.entity.message.Message;
import com.auction.entity.user.Bidder;
import com.auction.server.AuctionServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class RealtimeNotifier {
    private static RealtimeNotifier instance;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<String, List<Bidder>> watchers = new ConcurrentHashMap<>();
    private final ItemDAO itemDAO = new ItemDAO();
    private final UserDAO userDAO = new UserDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private RealtimeNotifier() {}

    public static RealtimeNotifier getInstance() {
        if (instance == null) {
            synchronized (RealtimeNotifier.class) {
                if (instance == null) {
                    instance = new RealtimeNotifier();
                }
            }
        }
        return instance;
    }

    public void watchItem(String itemId, Bidder bidder) {
        watchers.computeIfAbsent(itemId, k -> new CopyOnWriteArrayList<>()).add(bidder);
    }

    public void notifyRealtime(String itemId, double price, String bidderId) {
        try {
            Item item = itemDAO.findById(itemId);
            UserDAO.UserRecord bidder = userDAO.findById(bidderId);

            String itemName = (item != null) ? item.getName() : "Unknown item";
            String bidderName = (bidder != null) ? bidder.username : "Unknown";

            ObjectNode node = objectMapper.createObjectNode();
            node.put("itemId", itemId);
            node.put("amount", price);
            node.put("bidderId", bidderId);
            node.put("itemName", itemName);
            node.put("bidderName", bidderName);

            String json = objectMapper.writeValueAsString(node);
            AuctionServer.broadcast(new Message("BID_UPDATE", json));

            List<Bidder> watching = watchers.get(itemId);
            if (watching != null) {
                for (Bidder b : watching) {
                    String text = String.format("[REALTIME] %s: $%.2f by %s", itemName, price, bidderName);
                    System.out.println("📡 -> " + b.getUsername() + ": " + text);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void notifyExtension(String itemId, long remainingSeconds) {
        String json = String.format("{\"itemId\":\"%s\",\"remainingSeconds\":%d}", itemId, remainingSeconds);
        AuctionServer.broadcast(new Message("ANTI_SNIPING_TRIGGERED", json));
    }

    public void startCountdown(String itemId, int seconds, Runnable onEnd) {
        scheduler.schedule(() -> {
            System.out.println("⏰ TIME'S UP for " + itemId);
            onEnd.run();
        }, seconds, TimeUnit.SECONDS);

        for (int i = seconds; i >= 0; i -= 5) {
            final int remaining = i;
            scheduler.schedule(() -> {
                System.out.println("⏳ " + itemId + " ends in " + remaining + "s");
            }, seconds - i, TimeUnit.SECONDS);
        }
    }
}
