package com.auction.service;

import com.auction.entity.Art;
import com.auction.entity.Electronics;
import com.auction.entity.Item;
import com.auction.entity.Vehicle;
import java.time.LocalDateTime;

public class ItemFactory {

    /**
     * Tạo một đối tượng Item (Art hoặc Electronics) với đầy đủ các thuộc tính.
     *
     * @param type          Loại sản phẩm ("electronics" hoặc "art")
     * @param id            ID của sản phẩm
     * @param name          Tên sản phẩm
     * @param description   Mô tả chi tiết
     * @param startingPrice Giá khởi điểm
     * @param startTime     Thời gian bắt đầu đấu giá
     * @param endTime       Thời gian kết thúc đấu giá
     * @param extraAttrs    Thuộc tính bổ sung (Integer cho warranty, String cho artist)
     * @return Một đối tượng Item đã được khởi tạo.
     */
    public static Item createItem(String type, String id, String name, String description,
                                  double startingPrice, LocalDateTime startTime, LocalDateTime endTime,
                                  String sellerId, Object... extraAttrs) {
        switch (type.toLowerCase()) {
            case "electronics":
                if (extraAttrs.length == 0 || !(extraAttrs[0] instanceof Integer)) {
                    throw new IllegalArgumentException("Electronics item requires warranty months (Integer).");
                }
                int warrantyMonths = (Integer) extraAttrs[0];
                return new Electronics(id, name, description, startingPrice, startTime, endTime, sellerId, warrantyMonths);

            case "art":
                if (extraAttrs.length == 0 || !(extraAttrs[0] instanceof String)) {
                    throw new IllegalArgumentException("Art item requires artist name (String).");
                }
                String artistName = (String) extraAttrs[0];
                return new Art(id, name, description, startingPrice, startTime, endTime, sellerId, artistName);

            case "vehicle":
                if (extraAttrs.length < 3 || !(extraAttrs[0] instanceof String) || !(extraAttrs[1] instanceof String)) {
                    throw new IllegalArgumentException("Vehicle item requires make, model, and year (String).");
            }
                String make = (String) extraAttrs[0];
                String model = (String) extraAttrs[1];
                int year = (Integer) extraAttrs[2];
                return new Vehicle(id, name, description, startingPrice, startTime, endTime, sellerId, make, model, year);

            default:
                throw new IllegalArgumentException("Unknown item type: " + type);
        }
    }
}
