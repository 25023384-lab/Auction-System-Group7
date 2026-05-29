package com.auction.client.api;

import com.auction.client.ClientConnection;
import com.auction.dto.bid.AutoBidRequest;
import com.auction.dto.bid.BidRequest;
import com.auction.entity.items.Item;
import com.auction.entity.message.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.HashMap;
import java.util.Map;

/**
 * Đóng gói logic giao tiếp mạng cho các chức năng đấu giá và quản lý.
 * Gửi các request lên server, phản hồi sẽ được AuctionNetworkService xử lý bất đồng bộ.
 */
public class AuctionApiClient {
    private final ClientConnection connection;
    private final ObjectMapper objectMapper;

    public AuctionApiClient(ClientConnection connection) {
        this.connection = connection;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    private void send(String type, String data) {
        try {
            connection.sendMessage(new Message(type, data));
        } catch (Exception e) {
            System.err.println("Failed to send message [" + type + "]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void topUp(String userId, double amount) {
        try {
            Map<String, Object> req = new HashMap<>();
            req.put("userId", userId);
            req.put("amount", amount);
            send("TOP_UP", objectMapper.writeValueAsString(req));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getItems() {
        send("GET_ITEMS", "");
    }

    public void getBidHistory(String itemId) {
        send("GET_BID_HISTORY", itemId);
    }

    public void placeBid(String itemId, String bidderId, double amount) {
        try {
            BidRequest req = new BidRequest();
            req.setItemId(itemId);
            req.setBidderId(bidderId);
            req.setAmount(amount);
            send("BID", objectMapper.writeValueAsString(req));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerAutoBid(String itemId, String bidderId, double maxBid, double increment) {
        try {
            AutoBidRequest req = new AutoBidRequest();
            req.setItemId(itemId);
            req.setBidderId(bidderId);
            req.setMaxBid(maxBid);
            req.setIncrement(increment);
            send("REGISTER_AUTO_BID", objectMapper.writeValueAsString(req));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getItemDetails(String itemId) {
        send("GET_ITEM_DETAILS", itemId);
    }

    public void logout() {
        send("LOGOUT", "");
    }

    public void getSellerItems(String sellerId) {
        send("GET_SELLER_ITEMS", sellerId);
    }

    public void deleteItem(String itemId) {
        send("DELETE_ITEM", itemId);
    }

    public void payItem(String itemId) {
        send("PAY_ITEM", itemId);
    }

    public void cancelOrder(String itemId) {
        send("CANCEL_ORDER", itemId);
    }

    public void createItem(Item item) {
        try {
            send("CREATE_ITEM", objectMapper.writeValueAsString(item));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateItem(Item item) {
        try {
            send("UPDATE_ITEM", objectMapper.writeValueAsString(item));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getAllUsers() {
        send("GET_ALL_USERS", "");
    }

    public void deleteUser(String userId) {
        send("DELETE_USER", userId);
    }
}
