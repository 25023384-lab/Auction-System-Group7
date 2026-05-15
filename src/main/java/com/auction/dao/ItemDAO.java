package com.auction.dao;

import com.auction.entity.Art;
import com.auction.entity.Electronics;
import com.auction.entity.Item;
import com.auction.entity.Vehicle;
import com.auction.util.DBHelper;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public void save(Item item) throws SQLException {
        boolean exists = findById(item.getId()) != null;
        String sql;

        if (exists) {
            sql = """
                UPDATE items SET
                    name = ?, description = ?, starting_price = ?, current_bid = ?,
                    highest_bidder_id = ?, start_time = ?, end_time = ?, status = ?,
                    warranty_months = ?, artist_name = ?, make = ?, model = ?, year = ?
                WHERE id = ?
            """;
        } else {
            sql = """
                INSERT INTO items (name, description, starting_price, current_bid,
                                   highest_bidder_id, start_time, end_time, status,
                                   warranty_months, artist_name, make, model, year,
                                   id, type, seller_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        }

        try (PreparedStatement stmt = DBHelper.getConnection().prepareStatement(sql)) {
            stmt.setString(1, item.getName());
            stmt.setString(2, item.getDescription());
            stmt.setDouble(3, item.getStartingPrice());
            stmt.setDouble(4, item.getCurrentHighestBid());
            stmt.setString(5, item.getHighestBidderId());
            stmt.setString(6, item.getStartTime().format(formatter));
            stmt.setString(7, item.getEndTime().format(formatter));
            stmt.setString(8, item.getStatus().name());

            if (item instanceof Electronics elec) {
                stmt.setInt(9, elec.getWarrantyMonths());
                stmt.setNull(10, Types.VARCHAR); // artist_name
                stmt.setNull(11, Types.VARCHAR); // make
                stmt.setNull(12, Types.VARCHAR); // model
                stmt.setNull(13, Types.INTEGER); // year
            } else if (item instanceof Art art) {
                stmt.setNull(9, Types.INTEGER); // warranty_months
                stmt.setString(10, art.getArtistName());
                stmt.setNull(11, Types.VARCHAR); // make
                stmt.setNull(12, Types.VARCHAR); // model
                stmt.setNull(13, Types.INTEGER); // year
            } else if (item instanceof Vehicle vehicle) {
                stmt.setNull(9, Types.INTEGER); // warranty_months
                stmt.setNull(10, Types.VARCHAR); // artist_name
                stmt.setString(11, vehicle.getMake());
                stmt.setString(12, vehicle.getModel());
                stmt.setInt(13, vehicle.getYear());
            } else {
                stmt.setNull(9, Types.INTEGER);
                stmt.setNull(10, Types.VARCHAR);
                stmt.setNull(11, Types.VARCHAR);
                stmt.setNull(12, Types.VARCHAR);
                stmt.setNull(13, Types.INTEGER);
            }

            if (exists) {
                stmt.setString(14, item.getId());
            } else {
                stmt.setString(14, item.getId());
                stmt.setString(15, item.getType());
                stmt.setString(16, item.getSellerId());
            }

            stmt.executeUpdate();
        }
    }

    public Item findById(String id) throws SQLException {
        String sql = "SELECT * FROM items WHERE id = ?";
        try (PreparedStatement stmt = DBHelper.getConnection().prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToItem(rs);
                }
            }
        }
        return null;
    }

    public List<Item> findAll() throws SQLException {
        String sql = "SELECT * FROM items ORDER BY created_at DESC";
        List<Item> list = new ArrayList<>();
        try (Statement stmt = DBHelper.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToItem(rs));
            }
        }
        return list;
    }

    public List<Item> findBySeller(String sellerId) throws SQLException {
        String sql = "SELECT * FROM items WHERE seller_id = ? ORDER BY created_at DESC";
        List<Item> list = new ArrayList<>();
        try (PreparedStatement stmt = DBHelper.getConnection().prepareStatement(sql)) {
            stmt.setString(1, sellerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToItem(rs));
                }
            }
        }
        return list;
    }

    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM items WHERE id = ?";
        try (PreparedStatement stmt = DBHelper.getConnection().prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
    }

    public void updateStatus(String id, Item.Status status) throws SQLException {
        String sql = "UPDATE items SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = DBHelper.getConnection().prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setString(2, id);
            stmt.executeUpdate();
        }
    }

    public void updateCurrentBid(String id, double amount, String bidderId) throws SQLException {
        String sql = "UPDATE items SET current_bid = ?, highest_bidder_id = ? WHERE id = ?";
        try (PreparedStatement stmt = DBHelper.getConnection().prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setString(2, bidderId);
            stmt.setString(3, id);
            stmt.executeUpdate();
        }
    }

    private Item mapRowToItem(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        double startingPrice = rs.getDouble("starting_price");
        LocalDateTime startTime = LocalDateTime.parse(rs.getString("start_time"), formatter);
        LocalDateTime endTime = LocalDateTime.parse(rs.getString("end_time"), formatter);
        String type = rs.getString("type");
        String sellerId = rs.getString("seller_id");

        Item item;

        if ("ELECTRONICS".equals(type)) {
            int warranty = rs.getInt("warranty_months");
            item = new Electronics(id, name, description, startingPrice, startTime, endTime, sellerId, warranty);
        } else if ("ART".equals(type)) {
            String artist = rs.getString("artist_name");
            item = new Art(id, name, description, startingPrice, startTime, endTime, sellerId, artist);
        } else if ("VEHICLE".equals(type)) {
            String make = rs.getString("make");
            String model = rs.getString("model");
            int year = rs.getInt("year");
            item = new Vehicle(id, name, description, startingPrice, startTime, endTime, sellerId, make, model, year);
        } else {
            throw new SQLException("Unknown item type in database: " + type + " for item id: " + id);
        }

        item.setCurrentHighestBid(rs.getDouble("current_bid"));
        item.setHighestBidderId(rs.getString("highest_bidder_id"));
        item.setStatus(Item.Status.valueOf(rs.getString("status")));

        return item;
    }
}