package com.auction.service.network;

import com.auction.client.ClientConnection;
import com.auction.client.api.AuctionApiClient;
import com.auction.controller.auction.AuctionController;
import com.auction.entity.items.Item;
import com.auction.entity.message.Message;
import com.auction.entity.user.Bidder;
import com.auction.util.DialogManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.collections.FXCollections;

import java.util.List;
import java.util.Map;

public class AuctionNetworkService {

    private final ClientConnection connection;
    private final AuctionApiClient apiClient;
    private final AuctionController controller;
    private final ObjectMapper objectMapper;

    public AuctionNetworkService(ClientConnection connection, AuctionController controller) {
        this.connection = connection;
        this.apiClient = new AuctionApiClient(connection);
        this.controller = controller;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void startListening() {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    Message msg = connection.receiveMessage();
                    handleIncomingMessage(msg);
                }
            } catch (Exception e) {
                System.out.println("Message listener stopped: " + e.getMessage());
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void handleIncomingMessage(Message msg) {
        Platform.runLater(() -> {
            try {
                switch (msg.getType()) {
                    case "BID_UPDATE": {
                        JsonNode node = objectMapper.readTree(msg.getData());
                        String itemId = node.path("itemId").asText();
                        double newAmount = node.path("amount").asDouble();
                        String newBidderId = node.path("bidderId").asText();

                        String itemName = node.path("itemName").asText("Unknown item");
                        String bidderName = node.path("bidderName").asText("Unknown");
                        double amount = node.path("amount").asDouble(0);

                        controller.updateItemBid(itemId, newAmount, newBidderId, itemName, bidderName, amount);
                        break;
                    }

                    case "BID_RESULT": {
                        controller.showBidResult("true".equals(msg.getData()), msg.getData());
                        break;
                    }

                    case "ANTI_SNIPING_TRIGGERED": {
                        controller.fetchItemsFromServer();
                        JsonNode node = objectMapper.readTree(msg.getData());
                        String itemId = node.path("itemId").asText();
                        long rem = node.path("remainingSeconds").asLong();
                        String log = String.format("🛡️ [Anti-Sniping] Auction for \"%s\" extended! New time: %ds", itemId, rem);
                        controller.addNotification(log);
                        if (controller.getAdminDashboardController() != null) {
                            controller.getAdminDashboardController().addSystemLog(log);
                        }
                        break;
                    }

                    case "ITEM_STATUS_CHANGED": {
                        try {
                            Item changedItem = objectMapper.readValue(msg.getData(), Item.class);
                            controller.updateItemStatus(changedItem);
                        } catch (Exception e) {
                            controller.addNotification("[SYSTEM] Item status updated.");
                        }
                        break;
                    }

                    case "NEW_ITEM_ADDED": {
                        controller.fetchItemsFromServer();
                        try {
                            Item newItem = objectMapper.readValue(msg.getData(), Item.class);
                            controller.addNotification("[NEW] \"" + newItem.getName() + "\" is now available for auction!");
                        } catch (Exception e) {
                            controller.addNotification("[NEW] A new item is available for auction!");
                        }
                        break;
                    }

                    case "NOTIFY":
                        controller.addNotification("[INFO] " + msg.getData());
                        break;

                    case "ERROR":
                        DialogManager.showError(msg.getData());
                        controller.addNotification("[ERROR] " + msg.getData());
                        break;

                    case "ITEM_LIST": {
                        List<Item> items = objectMapper.readValue(msg.getData(), new TypeReference<>() {});
                        controller.loadItems(FXCollections.observableArrayList(items));
                        if (controller.getAdminDashboardController() != null) {
                            controller.getAdminDashboardController().loadItems(items);
                        }
                        break;
                    }

                    case "SELLER_ITEMS": {
                        List<Item> sellerItems = objectMapper.readValue(msg.getData(), new TypeReference<>() {});
                        if (controller.getMainLayoutController() != null && controller.getMainLayoutController().getMyItemsController() != null) {
                            controller.getMainLayoutController().getMyItemsController().loadItems(sellerItems);
                        } else {
                            controller.loadItems(FXCollections.observableArrayList(sellerItems));
                        }
                        controller.addNotification("[DASHBOARD] Showing your " + sellerItems.size() + " item(s).");
                        break;
                    }

                    case "DELETE_SUCCESS":
                        controller.addNotification("[SYSTEM] Item deleted successfully.");
                        if (controller.getMainLayoutController() != null && controller.getMainLayoutController().getMyItemsController() != null) {
                            controller.getMainLayoutController().getMyItemsController().fetchMyItems();
                        }
                        apiClient.getItems();
                        break;

                    case "ALL_USERS": {
                        List<com.auction.dao.UserDAO.UserRecord> users = objectMapper.readValue(msg.getData(), new TypeReference<>() {});
                        if (controller.getAdminDashboardController() != null) {
                            controller.getAdminDashboardController().loadUsers(users);
                        }
                        break;
                    }

                    case "DELETE_USER_SUCCESS":
                        controller.addNotification("[ADMIN] Deleted user successfully: " + msg.getData());
                        if (controller.getAdminDashboardController() != null) {
                            controller.getAdminDashboardController().handleRefreshAll();
                        }
                        break;

                    case "BID_HISTORY": {
                        List<Map<String, Object>> bids = objectMapper.readValue(msg.getData(), new TypeReference<>() {});
                        controller.updateBidHistoryList(bids);
                        break;
                    }

                    case "ITEM_DETAILS": {
                        DialogManager.openItemDetailsWindow(msg.getData(), connection, controller.getCurrentUser(), controller::addNotification);
                        break;
                    }

                    case "AUTO_BID_REGISTERED": {
                        JsonNode node = objectMapper.readTree(msg.getData());
                        String itemId = node.path("itemId").asText();
                        double max = node.path("maxBid").asDouble();
                        double increment = node.path("increment").asDouble();
                        String log = String.format("🤖 [AUTO-BID] Active for %s (Max: $%.2f)", itemId, max);
                        controller.updateAutoBidStatus(max, increment);
                        controller.showBidResult(true, "Auto-Bid activated!");
                        controller.addNotification(log);
                        if (controller.getAdminDashboardController() != null) {
                            controller.getAdminDashboardController().addSystemLog(log);
                        }
                        break;
                    }

                    case "TOP_UP_SUCCESS": {
                        JsonNode node = objectMapper.readTree(msg.getData());
                        double newBalance = node.path("newBalance").asDouble();
                        controller.addNotification(String.format("[TOP-UP] Added funds successfully. New Balance: $%.2f", newBalance));
                        if (controller.getCurrentUser() instanceof Bidder) {
                            ((Bidder) controller.getCurrentUser()).setBalance(newBalance);
                            controller.updateBalanceDisplay(newBalance);
                        }
                        break;
                    }

                    case "ITEM_REMOVED": {
                        controller.fetchItemsFromServer();
                        controller.addNotification("[REMOVED] An item has been deleted.");
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                controller.addNotification("[ERROR] " + e.getMessage());
            }
        });
    }
}
