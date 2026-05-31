package com.auction.service.scheduler;

import com.auction.dao.ItemDAO;
import com.auction.entity.items.Item;
import com.auction.entity.message.Message;
import com.auction.server.AuctionServer;
import com.auction.service.auction.AuctionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionScheduler {
    private final ItemDAO itemDAO = new ItemDAO();
    private final AuctionManager auctionManager;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuctionScheduler(AuctionManager auctionManager) {
        this.auctionManager = auctionManager;
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void start() {
        // Load all items into the auction manager on startup
        try {
            List<Item> allItems = itemDAO.findAll();
            for (Item item : allItems) {
                auctionManager.addItem(item);
            }
        } catch (Exception e) {
            System.err.println("Error loading initial items: " + e.getMessage());
        }

        scheduler.scheduleAtFixedRate(this::checkAuctions, 0, 1, TimeUnit.SECONDS);
    }

    private void checkAuctions() {
        try {
            List<Item> allItems = itemDAO.findAll();
            LocalDateTime now = LocalDateTime.now();

            for (Item item : allItems) {
                boolean changed = false;

                // 1. Chuyển OPEN -> RUNNING
                if (item.getStatus() == Item.Status.OPEN && !now.isBefore(item.getStartTime())) {
                    item.setStatus(Item.Status.RUNNING);
                    changed = true;
                    System.out.println("🚀 Auction STARTED: " + item.getName());
                }

                // 2. Chuyển RUNNING -> FINISHED
                if (item.getStatus() == Item.Status.RUNNING && now.isAfter(item.getEndTime())) {
                    item.setStatus(Item.Status.FINISHED);
                    changed = true;
                    System.out.println("🏁 Auction FINISHED: " + item.getName() +
                                       " | Winner: " + item.getHighestBidderId());
                }

                if (changed) {
                    // Cập nhật Database
                    itemDAO.save(item);
                    // Cập nhật Memory Manager
                    auctionManager.updateItemStatus(item);
                    // Broadcast cho Client
                    AuctionServer.broadcast(new Message("ITEM_STATUS_CHANGED", objectMapper.writeValueAsString(item)));
                }
            }
        } catch (Exception e) {
            System.err.println("Error in AuctionScheduler: " + e.getMessage());
        }
    }

    public void stop() {
        scheduler.shutdown();
    }
}
