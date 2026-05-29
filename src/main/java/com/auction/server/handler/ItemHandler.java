package com.auction.server.handler;

import com.auction.entity.items.Item;
import com.auction.entity.message.Message;
import com.auction.service.item.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.PrintWriter;
import java.util.List;
import java.util.function.Consumer;

/**
 * Xử lý các yêu cầu liên quan đến Item: Tạo, Sửa, Xóa, Liệt kê.
 */
public class ItemHandler {
    private final ItemService itemService;
    private final ObjectMapper objectMapper;

    public ItemHandler(ItemService itemService, ObjectMapper objectMapper) {
        this.itemService = itemService;
        this.objectMapper = objectMapper;
    }

    public void handleCreateItem(Message msg, PrintWriter out, Consumer<Message> broadcast) {
        try {
            Item newItem = objectMapper.readValue(msg.getData(), Item.class);
            itemService.createItem(newItem);
            out.println(objectMapper.writeValueAsString(
                    new Message("CREATE_ITEM_SUCCESS", "Item created.")));
            broadcast.accept(new Message("NEW_ITEM_ADDED", objectMapper.writeValueAsString(newItem)));
        } catch (Exception e) {
            try {
                out.println(objectMapper.writeValueAsString(
                        new Message("CREATE_ITEM_FAILED", e.getMessage())));
            } catch (Exception ignored) {}
        }
    }

    public void handleUpdateItem(Message msg, PrintWriter out, Consumer<Message> broadcast) {
        try {
            Item updatedItem = objectMapper.readValue(msg.getData(), Item.class);
            itemService.updateItem(updatedItem);
            out.println(objectMapper.writeValueAsString(
                    new Message("UPDATE_ITEM_SUCCESS", "Item updated.")));
            broadcast.accept(new Message("ITEM_STATUS_CHANGED", objectMapper.writeValueAsString(updatedItem)));
        } catch (Exception e) {
            try {
                out.println(objectMapper.writeValueAsString(
                        new Message("UPDATE_ITEM_FAILED", e.getMessage())));
            } catch (Exception ignored) {}
        }
    }

    public void handleListItems(PrintWriter out) {
        try {
            List<Item> items = itemService.getAllItems();
            out.println(objectMapper.writeValueAsString(
                    new Message("ITEM_LIST", objectMapper.writeValueAsString(items))));
        } catch (Exception e) {
            try {
                out.println(objectMapper.writeValueAsString(new Message("ERROR", e.getMessage())));
            } catch (Exception ignored) {}
        }
    }

    public void handleDeleteItem(Message msg, PrintWriter out, Consumer<Message> broadcast) {
        try {
            String itemId = msg.getData();
            itemService.deleteItem(itemId);
            broadcast.accept(new Message("ITEM_REMOVED", itemId));
            out.println(objectMapper.writeValueAsString(
                    new Message("DELETE_SUCCESS", itemId)));
        } catch (Exception e) {
            try { out.println(objectMapper.writeValueAsString(new Message("ERROR", e.getMessage()))); }
            catch (Exception ignored) {}
        }
    }

    public void handleGetSellerItems(Message msg, PrintWriter out) {
        try {
            String sellerId = msg.getData();
            List<Item> items = itemService.getSellerItems(sellerId);
            out.println(objectMapper.writeValueAsString(
                    new Message("SELLER_ITEMS", objectMapper.writeValueAsString(items))));
        } catch (Exception e) {
            try { out.println(objectMapper.writeValueAsString(new Message("ERROR", e.getMessage()))); }
            catch (Exception ignored) {}
        }
    }

    public void handleGetItemDetails(Message msg, PrintWriter out,
                                     com.auction.dao.UserDAO userDAO,
                                     com.auction.dao.BidTransactionDAO bidDAO) {
        try {
            String itemId = msg.getData();
            Item item = itemService.getItemById(itemId);
            if (item == null) {
                out.println(objectMapper.writeValueAsString(
                        new Message("ERROR", "Item not found: " + itemId)));
                return;
            }
            int bidCount = bidDAO.countBids(itemId);
            com.auction.entity.bid.BidTransaction highestBid = bidDAO.getHighestBid(itemId);

            // Lấy tên seller
            String sellerName = item.getSellerId();
            try {
                com.auction.dao.UserDAO.UserRecord sellerRec = userDAO.findById(item.getSellerId());
                if (sellerRec != null) sellerName = sellerRec.username;
            } catch (Exception ignored) {}

            // Lấy tên winner (nếu có)
            String winnerName = null;
            if (item.getHighestBidderId() != null) {
                try {
                    com.auction.dao.UserDAO.UserRecord winnerRec = userDAO.findById(item.getHighestBidderId());
                    if (winnerRec != null) winnerName = winnerRec.username;
                } catch (Exception ignored) {}
            }

            java.util.Map<String, Object> details = new java.util.HashMap<>();
            details.put("item", item);
            details.put("sellerName", sellerName);
            details.put("bidCount", bidCount);
            details.put("highestBidAmount", highestBid != null ? highestBid.getBidAmount() : 0.0);
            details.put("winnerName", winnerName != null ? winnerName : "N/A");

            out.println(objectMapper.writeValueAsString(
                    new Message("ITEM_DETAILS", objectMapper.writeValueAsString(details))));
        } catch (Exception e) {
            try { out.println(objectMapper.writeValueAsString(new Message("ERROR", e.getMessage()))); }
            catch (Exception ignored) {}
        }
    }
}
