package com.auction.service.item;

import com.auction.dao.ItemDAO;
import com.auction.entity.items.Item;
import com.auction.service.auction.AuctionManager;

import java.sql.SQLException;
import java.util.List;

/**
 * Quản lý logic nghiệp vụ cho các Item (Vật phẩm đấu giá).
 */
public class ItemService {
    private final ItemDAO itemDAO;
    private final AuctionManager auctionManager;

    public ItemService(ItemDAO itemDAO, AuctionManager auctionManager) {
        this.itemDAO = itemDAO;
        this.auctionManager = auctionManager;
    }

    public void createItem(Item item) throws SQLException {
        itemDAO.save(item);
        auctionManager.addItem(item);
    }

    public void updateItem(Item item) throws SQLException {
        itemDAO.save(item);
        auctionManager.addItem(item);
    }

    public void deleteItem(String itemId) throws SQLException {
        itemDAO.delete(itemId);
    }

    public List<Item> getAllItems() throws SQLException {
        return itemDAO.findAll();
    }

    public List<Item> getSellerItems(String sellerId) throws SQLException {
        return itemDAO.findBySeller(sellerId);
    }

    public Item getItemById(String itemId) throws SQLException {
        return itemDAO.findById(itemId);
    }
}
