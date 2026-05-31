package com.auction.util;

import com.auction.entity.items.Item;
import com.auction.entity.user.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Supplier;

public class AuctionUIHelper {

    public static void setupTableColumns(
            TableColumn<Item, String> nameColumn,
            TableColumn<Item, String> descriptionColumn,
            TableColumn<Item, String> priceColumn,
            TableColumn<Item, String> statusColumn,
            TableColumn<Item, String> myStatusColumn,
            TableColumn<Item, String> endTimeColumn,
            Supplier<User> currentUserSupplier) {

        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));
        descriptionColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDescription()));
        priceColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("$%.2f", cellData.getValue().getCurrentHighestBid())));
        statusColumn.setCellValueFactory(cellData -> {
            var item = cellData.getValue();
            return new SimpleStringProperty(item.getStatus() != null ? item.getStatus().name() : "N/A");
        });

        // My Status Column
        myStatusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));
        myStatusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Item auctionItem = getTableView().getItems().get(getIndex());
                    User currentUser = currentUserSupplier.get();
                    if (currentUser == null || auctionItem.getHighestBidderId() == null) {
                        setText("-");
                        setStyle("-fx-text-fill: #95a5a6;");
                    } else if (currentUser.getId().equals(auctionItem.getHighestBidderId())) {
                        setText("👑 Winning");
                        setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    } else {
                        setText("Outbid");
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // End Time Column
        endTimeColumn.setCellValueFactory(cellData -> {
            Item item = cellData.getValue();
            LocalDateTime end = item.getEndTime();
            if (end == null) return new SimpleStringProperty("N/A");
            if (end.isBefore(LocalDateTime.now())) return new SimpleStringProperty("Ended");
            Duration duration = Duration.between(LocalDateTime.now(), end);
            return new SimpleStringProperty(String.format("%02dh:%02dm:%02ds", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()));
        });

        endTimeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    if (item.startsWith("00h:00m") || item.equals("Ended")) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
    }

    public static void updateChart(Item item, XYChart.Series<String, Number> priceSeries) {
        priceSeries.getData().clear();
        if (item != null) {
            priceSeries.setName(item.getName());
            if (item.getCurrentHighestBid() > item.getStartingPrice()) {
                priceSeries.getData().add(new XYChart.Data<>("Current Bid", item.getCurrentHighestBid()));
            } else {
                priceSeries.getData().add(new XYChart.Data<>("Start", item.getStartingPrice()));
            }
        }
    }
}
