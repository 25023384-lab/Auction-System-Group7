package com.auction.server.handler;

import com.auction.dao.ItemDAO;
import com.auction.dao.UserDAO;
import com.auction.entity.items.Item;
import com.auction.entity.message.Message;
import com.auction.entity.user.Bidder;
import com.auction.service.auction.AuctionManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Xử lý các yêu cầu thanh toán: Nạp tiền, Thanh toán hóa đơn, Hủy đơn.
 */
public class PaymentHandler {
    private final UserDAO userDAO;
    private final ItemDAO itemDAO;
    private final AuctionManager auctionManager;
    private final ObjectMapper objectMapper;

    public PaymentHandler(UserDAO userDAO, ItemDAO itemDAO, AuctionManager auctionManager, ObjectMapper objectMapper) {
        this.userDAO = userDAO;
        this.itemDAO = itemDAO;
        this.auctionManager = auctionManager;
        this.objectMapper = objectMapper;
    }

    public void handleTopUp(Message msg, PrintWriter out) {
        try {
            Map<String, Object> req = objectMapper.readValue(msg.getData(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            String userId = (String) req.get("userId");
            double amount = ((Number) req.get("amount")).doubleValue();

            UserDAO.UserRecord rec = userDAO.findById(userId);
            if (rec != null) {
                double newBalance = rec.balance + amount;
                userDAO.updateBalance(userId, newBalance);

                // Cập nhật Cache trên Server
                Bidder cachedBidder = auctionManager.getBidder(userId);
                if (cachedBidder != null) {
                    cachedBidder.setBalance(newBalance);
                }

                Map<String, Object> resp = new HashMap<>();
                resp.put("newBalance", newBalance);
                out.println(objectMapper.writeValueAsString(new Message("TOP_UP_SUCCESS", objectMapper.writeValueAsString(resp))));
            } else {
                out.println(objectMapper.writeValueAsString(new Message("ERROR", "User not found for top-up")));
            }
        } catch (Exception e) {
            try { out.println(objectMapper.writeValueAsString(new Message("ERROR", e.getMessage()))); }
            catch (Exception ignored) {}
        }
    }

    public void handlePayItem(Message msg, PrintWriter out, String currentUserId, Consumer<Message> broadcast) {
        try {
            String itemId = msg.getData();
            Item item = itemDAO.findById(itemId);

            if (item == null || item.getStatus() != Item.Status.FINISHED) {
                out.println(objectMapper.writeValueAsString(new Message("ERROR", "Invalid item or not finished yet.")));
                return;
            }

            String winnerId = item.getHighestBidderId();
            if (winnerId == null || !winnerId.equals(currentUserId)) {
                out.println(objectMapper.writeValueAsString(new Message("ERROR", "You are not the winner.")));
                return;
            }

            UserDAO.UserRecord winner = userDAO.findById(winnerId);
            double amountToPay = item.getCurrentHighestBid();

            if (winner.balance >= amountToPay) {
                // Trừ tiền
                double newBalance = winner.balance - amountToPay;
                userDAO.updateBalance(winnerId, newBalance);

                // Cập nhật RAM
                Bidder cachedBidder = auctionManager.getBidder(winnerId);
                if (cachedBidder != null) {
                    cachedBidder.setBalance(newBalance);
                }

                // Chuyển trạng thái Item thành PAID
                item.setStatus(Item.Status.PAID);
                itemDAO.save(item);
                auctionManager.addItem(item);

                // Báo cho user số dư mới thông qua TOP_UP_SUCCESS
                Map<String, Object> resp = new HashMap<>();
                resp.put("newBalance", newBalance);
                out.println(objectMapper.writeValueAsString(new Message("TOP_UP_SUCCESS", objectMapper.writeValueAsString(resp))));

                // Broadcast cập nhật Item
                broadcast.accept(new Message("ITEM_STATUS_CHANGED", objectMapper.writeValueAsString(item)));
                out.println(objectMapper.writeValueAsString(new Message("NOTIFY", "Payment successful! Item is now PAID.")));
            } else {
                out.println(objectMapper.writeValueAsString(new Message("ERROR", "Insufficient balance to pay for this item!")));
            }
        } catch (Exception e) {
            try { out.println(objectMapper.writeValueAsString(new Message("ERROR", e.getMessage()))); }
            catch (Exception ignored) {}
        }
    }

    public void handleCancelOrder(Message msg, PrintWriter out, String currentUserId, Consumer<Message> broadcast) {
        try {
            String itemId = msg.getData();
            Item item = itemDAO.findById(itemId);

            if (item == null || item.getStatus() != Item.Status.FINISHED) {
                out.println(objectMapper.writeValueAsString(new Message("ERROR", "Invalid item or not finished yet.")));
                return;
            }

            String winnerId = item.getHighestBidderId();
            if (winnerId == null || !winnerId.equals(currentUserId)) {
                out.println(objectMapper.writeValueAsString(new Message("ERROR", "You are not the winner.")));
                return;
            }

            // Chuyển trạng thái Item thành CANCELED
            item.setStatus(Item.Status.CANCELED);
            itemDAO.save(item);
            auctionManager.addItem(item);

            // Broadcast cập nhật Item
            broadcast.accept(new Message("ITEM_STATUS_CHANGED", objectMapper.writeValueAsString(item)));
            out.println(objectMapper.writeValueAsString(new Message("NOTIFY", "Order has been canceled.")));

        } catch (Exception e) {
            try { out.println(objectMapper.writeValueAsString(new Message("ERROR", e.getMessage()))); }
            catch (Exception ignored) {}
        }
    }
}
