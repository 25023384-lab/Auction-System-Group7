package com.auction.util;

import com.auction.client.ClientConnection;
import com.auction.controller.auction.CreateItemController;
import com.auction.controller.auction.ItemDetailController;
import com.auction.entity.message.Message;
import com.auction.entity.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class DialogManager {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void showInfo(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, content);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.show();
        });
    }

    public static void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, message);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.show();
        });
    }

    public static void handleCreateNewItem(ClientConnection connection, User currentSeller, Runnable onHiddenCallback) {
        try {
            FXMLLoader loader = new FXMLLoader(DialogManager.class.getResource("/create-item.fxml"));
            Parent root = loader.load();
            CreateItemController controller = loader.getController();
            controller.setConnection(connection);
            controller.setCurrentSeller(currentSeller);

            Stage stage = new Stage();
            stage.setTitle("Create New Auction Item");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setOnHidden(event -> {
                if (onHiddenCallback != null) onHiddenCallback.run();
            });
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Cannot open Create Item window: " + e.getMessage());
        }
    }

    public static void openItemDetailsWindow(String detailsJson, ClientConnection connection, User currentUser, Consumer<String> onError) {
        Platform.runLater(() -> {
            try {
                URL fxmlUrl = DialogManager.class.getResource("/item-detail.fxml");
                if (fxmlUrl == null) {
                    if (onError != null) onError.accept("item-detail.fxml not found.");
                    return;
                }
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent root = loader.load();
                ItemDetailController controller = loader.getController();
                controller.setConnectionAndUser(connection, currentUser);
                controller.loadDetails(detailsJson);

                Stage stage = new Stage();
                stage.setTitle("Item Details");
                stage.setScene(new Scene(root, 480, 420));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
                if (onError != null) onError.accept("Cannot open details: " + e.getMessage());
            }
        });
    }

    public static void handleTopUp(ClientConnection connection, User currentUser, Consumer<String> onNotification) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Top Up Balance");
        dialog.setHeaderText(null);
        dialog.setGraphic(null);

        DialogPane dialogPane = dialog.getDialogPane();
        try {
            URL cssUrl = DialogManager.class.getResource("/welcome-style.css");
            if (cssUrl != null) {
                dialogPane.getStylesheets().add(cssUrl.toExternalForm());
            }
        } catch (Exception ignored) {}
        dialogPane.getStyleClass().add("modern-dialog");

        // Custom Layout
        VBox content = new VBox(15);
        content.setStyle("-fx-padding: 10; -fx-background-color: white;");

        Label titleLabel = new Label("Top Up Balance");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: #0F172A;");

        Label instructionLabel = new Label("Enter the amount you wish to add to your bidding account:");
        instructionLabel.setStyle("-fx-text-fill: #64748B; -fx-wrap-text: true;");

        TextField amountField = new TextField("100");
        amountField.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-padding: 10; -fx-alignment: center; -fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-text-fill: #2563EB;");

        content.getChildren().addAll(titleLabel, instructionLabel, amountField);
        dialogPane.setContent(content);

        ButtonType submitButtonType = new ButtonType("Top Up", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        // Style the buttons explicitly to override native JavaFX styles
        javafx.scene.Node submitBtn = dialogPane.lookupButton(submitButtonType);
        if (submitBtn != null) submitBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 14px; -fx-padding: 8 24; -fx-background-radius: 8; -fx-cursor: hand;");

        javafx.scene.Node cancelBtn = dialogPane.lookupButton(ButtonType.CANCEL);
        if (cancelBtn != null) cancelBtn.setStyle("-fx-background-color: white; -fx-border-color: #CBD5E1; -fx-border-radius: 8; -fx-text-fill: #64748B; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 24; -fx-cursor: hand;");

        // Result Converter
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                return amountField.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount > 0) {
                    Map<String, Object> req = new HashMap<>();
                    req.put("userId", currentUser.getId());
                    req.put("amount", amount);
                    connection.sendMessage(new Message("TOP_UP", objectMapper.writeValueAsString(req)));
                } else {
                    if (onNotification != null) onNotification.accept("[ERROR] Top-up amount must be positive.");
                }
            } catch (NumberFormatException e) {
                if (onNotification != null) onNotification.accept("[ERROR] Invalid amount format.");
            } catch (Exception e) {
                if (onNotification != null) onNotification.accept("[ERROR] Could not send top-up request.");
            }
        });
    }
}
