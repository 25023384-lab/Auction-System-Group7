package com.auction.controller;

import com.auction.client.ClientConnection;
import com.auction.entity.Item;
import com.auction.entity.Art;
import com.auction.entity.Electronics;
import com.auction.entity.Vehicle;
import com.auction.entity.Message;
import com.auction.entity.User;
import com.auction.service.ItemFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Arrays;
import java.util.List;

public class CreateItemController {

    @FXML
    private TextField nameField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField priceField;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private Spinner<Integer> startHourSpinner;
    @FXML
    private Spinner<Integer> startMinuteSpinner;
    
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Spinner<Integer> endHourSpinner;
    @FXML
    private Spinner<Integer> endMinuteSpinner;
    
    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private VBox electronicsPane;
    @FXML
    private TextField warrantyField;
    @FXML
    private VBox artPane;
    @FXML
    private TextField artistField;
    @FXML
    private VBox vehiclePane; // Thêm VBox cho Vehicle
    @FXML
    private TextField makeField; // Thêm trường hãng xe
    @FXML
    private TextField modelField; // Thêm trường mẫu xe
    @FXML
    private TextField yearField; // Thêm trường năm sản xuất
    @FXML
    private Label messageLabel;

    private ClientConnection connection;
    private User currentSeller;
    private Item editingItem;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    public CreateItemController() {
    }

    public void setConnection(ClientConnection connection) {
        this.connection = connection;
    }

    public void setCurrentSeller(User currentSeller) {
        this.currentSeller = currentSeller;
    }

    public void setEditingItem(Item item) {
        this.editingItem = item;
        if (item != null) {
            nameField.setText(item.getName());
            descriptionArea.setText(item.getDescription());
            priceField.setText(String.valueOf(item.getStartingPrice()));
            if (item.getStartTime() != null) {
                startDatePicker.setValue(item.getStartTime().toLocalDate());
                startHourSpinner.getValueFactory().setValue(item.getStartTime().getHour());
                startMinuteSpinner.getValueFactory().setValue(item.getStartTime().getMinute());
            }
            if (item.getEndTime() != null) {
                endDatePicker.setValue(item.getEndTime().toLocalDate());
                endHourSpinner.getValueFactory().setValue(item.getEndTime().getHour());
                endMinuteSpinner.getValueFactory().setValue(item.getEndTime().getMinute());
            }
            
            if (item instanceof Electronics elec) {
                categoryComboBox.setValue("Electronics");
                warrantyField.setText(String.valueOf(elec.getWarrantyMonths()));
            } else if (item instanceof Art art) {
                categoryComboBox.setValue("Art");
                artistField.setText(art.getArtistName());
            } else if (item instanceof Vehicle vehicle) {
                categoryComboBox.setValue("Vehicle");
                makeField.setText(vehicle.getMake());
                modelField.setText(vehicle.getModel());
                yearField.setText(String.valueOf(vehicle.getYear()));
            }
            categoryComboBox.setDisable(true); // Don't allow changing category when editing
        }
    }

    @FXML
    public void initialize() {
        // Xóa sạch trước khi thêm để tránh trùng lặp nếu initialize gọi lại
        categoryComboBox.getItems().clear();
        categoryComboBox.getItems().addAll("Electronics", "Art", "Vehicle");

        // Khởi tạo Spinner Giờ (0-23) và Phút (0-59)
        startHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 8));
        endHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 20));
        startMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        endMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));

        categoryComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            boolean isElectronics = "Electronics".equals(newValue);
            boolean isArt = "Art".equals(newValue);
            boolean isVehicle = "Vehicle".equals(newValue);
            
            electronicsPane.setVisible(isElectronics);
            electronicsPane.setManaged(isElectronics);
            artPane.setVisible(isArt);
            artPane.setManaged(isArt);
            vehiclePane.setVisible(isVehicle);
            vehiclePane.setManaged(isVehicle);
            
            if (newValue != null) {
                messageLabel.setText(""); // Xóa thông báo lỗi khi đã chọn category
            }
        });
    }

    @FXML
    private void handleCreateItem() {
        if (connection == null || currentSeller == null) {
            messageLabel.setText("Error: Not connected or not logged in.");
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        // 1. Kiểm tra các trường cơ bản
        if (nameField.getText().trim().isEmpty()) {
            showError("Item Name is required.", nameField);
            return;
        }
        if (descriptionArea.getText().trim().isEmpty()) {
            showError("Description is required.", descriptionArea);
            return;
        }
        if (priceField.getText().trim().isEmpty()) {
            showError("Starting Price is required.", priceField);
            return;
        }
        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            showError("Please select both Start and End dates.", startDatePicker);
            return;
        }
        
        String category = categoryComboBox.getValue();
        if (category == null) {
            showError("Please select a Category.", categoryComboBox);
            return;
        }

        try {
            double price = Double.parseDouble(priceField.getText());
            
            int sHour = startHourSpinner.getValue();
            int sMin = startMinuteSpinner.getValue();
            LocalDateTime startTime = startDatePicker.getValue().atTime(sHour, sMin);
            
            int eHour = endHourSpinner.getValue();
            int eMin = endMinuteSpinner.getValue();
            LocalDateTime endTime = endDatePicker.getValue().atTime(eHour, eMin);

            if (endTime.isBefore(startTime)) {
                showError("End time must be after start time.", endDatePicker);
                return;
            }

            // 2. Kiểm tra các trường đặc thù
            if (category.equals("Electronics")) {
                if (warrantyField.getText().trim().isEmpty()) {
                    showError("Warranty months is required for Electronics.", warrantyField);
                    return;
                }
            } else if (category.equals("Art")) {
                if (artistField.getText().trim().isEmpty()) {
                    showError("Artist name is required for Art.", artistField);
                    return;
                }
            } else if (category.equals("Vehicle")) {
                if (makeField.getText().trim().isEmpty() || modelField.getText().trim().isEmpty() || yearField.getText().trim().isEmpty()) {
                    showError("Make, Model, and Year are required for Vehicle.", vehiclePane);
                    return;
                }
                if (!yearField.getText().matches("\\d{4}")) {
                    showError("Year must be a 4-digit number.", yearField);
                    return;
                }
            }

            // Tạo ID cho sản phẩm hoặc dùng ID cũ nếu đang Edit
            String itemId = (editingItem != null) ? editingItem.getId() : "ITM_" + UUID.randomUUID().toString().substring(0, 8);
            Item newItem = null;

            // Dùng ItemFactory để tạo đối tượng Item
            if (category.equals("Electronics")) {
                int warranty = Integer.parseInt(warrantyField.getText().trim());
                newItem = ItemFactory.createItem("electronics", itemId, nameField.getText(), descriptionArea.getText(), price, startTime, endTime, currentSeller.getId(), warranty);
            } else if (category.equals("Art")) {
                newItem = ItemFactory.createItem("art", itemId, nameField.getText(), descriptionArea.getText(), price, startTime, endTime, currentSeller.getId(), artistField.getText().trim());
            } else if (category.equals("Vehicle")) {
                String make = makeField.getText().trim();
                String model = modelField.getText().trim();
                int year = Integer.parseInt(yearField.getText().trim());
                newItem = ItemFactory.createItem("vehicle", itemId, nameField.getText(), descriptionArea.getText(), price, startTime, endTime, currentSeller.getId(), make, model, year);
            }

            if (newItem == null) return;

            // Đóng gói đối tượng Item thành JSON
            String itemJson = objectMapper.writeValueAsString(newItem);
            String messageType = (editingItem != null) ? "UPDATE_ITEM" : "CREATE_ITEM";
            Message msg = new Message(messageType, itemJson);

            Platform.runLater(() -> {
                messageLabel.setTextFill(javafx.scene.paint.Color.BLUE);
                messageLabel.setText((editingItem != null ? "Updating" : "Creating") + " item...");
            });

            Thread sendThread = new Thread(() -> {
                try {
                    connection.sendMessage(msg);
                    Platform.runLater(() -> {
                        messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                        messageLabel.setText("Success!");
                        nameField.getScene().getWindow().hide();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> showError("Network error: " + ex.getMessage()));
                }
            });
            sendThread.setDaemon(true);
            sendThread.start();

        } catch (NumberFormatException e) {
            showError("Price, Warranty, and Year must be valid numbers.", priceField);
        } catch (Exception e) {
            showError("Error: " + e.getMessage(), null);
        }
    }

    private void showError(String msg, Node fieldToHighlight) {
        messageLabel.setTextFill(javafx.scene.paint.Color.RED);
        messageLabel.setText(msg);

        // Xóa style lỗi khỏi tất cả các trường trước
        List<Node> allFields = Arrays.asList(nameField, descriptionArea, priceField, startDatePicker, endDatePicker,
                categoryComboBox, warrantyField, artistField, makeField, modelField, yearField, vehiclePane);
        for (Node field : allFields) {
            if (field != null) {
                field.getStyleClass().remove("error-field");
            }
        }

        // Thêm style lỗi cho trường cụ thể
        if (fieldToHighlight != null) {
            fieldToHighlight.getStyleClass().add("error-field");
        }
    }

    // Phương thức nạp chồng để xử lý các cuộc gọi không có trường cụ thể
    private void showError(String msg) {
        showError(msg, null);
    }

    @FXML
    private void handleCancel() {
        nameField.getScene().getWindow().hide();
    }
}