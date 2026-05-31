package com.auction.controller.auction;
import com.auction.client.ClientConnection;
import com.auction.controller.navigation.MainLayoutController;
import com.auction.controller.admin.AdminDashboardController;
import com.auction.client.api.AuctionApiClient;
import com.auction.entity.items.Item;
import com.auction.entity.user.User;
import com.auction.entity.user.Bidder;
import com.auction.service.network.AuctionNetworkService;
import com.auction.util.AuctionUIHelper;
import com.auction.util.DialogManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
public class AuctionController {
    @FXML private Button createNewItemButton;
    @FXML private Button myItemsButton;
    @FXML private TableView<Item> itemTable;
    @FXML private TableColumn<Item, String> nameColumn;
    @FXML private TableColumn<Item, String> descriptionColumn;
    @FXML private TableColumn<Item, String> priceColumn;
    @FXML private TableColumn<Item, String> statusColumn;
    @FXML private TableColumn<Item, String> endTimeColumn;
    @FXML private TableColumn<Item, String> myStatusColumn;
    @FXML private Text selectedItemText;
    @FXML private TextField bidAmountField;
    @FXML private Label bidMessageLabel;
    @FXML private TextField maxBidField;
    @FXML private TextField incrementField;
    @FXML private ListView<String> notificationList;
    @FXML private ListView<String> bidHistoryList;
    @FXML private LineChart<String, Number> priceChart;
    @FXML private Button viewDetailsButton;
    @FXML private Label userBalanceLabel;
    @FXML private Button topUpButton;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label autoBidStatusLabel;
    private ObservableList<Item> masterData = FXCollections.observableArrayList();
    private XYChart.Series<String, Number> priceSeries = new XYChart.Series<>();
    private ClientConnection connection;
    private AuctionApiClient apiClient;
    private User currentUser;
    private final ScheduledExecutorService timerExecutor = Executors.newSingleThreadScheduledExecutor();
    private MainLayoutController mainLayoutController;
    private AdminDashboardController adminDashboardController;
    private AuctionNetworkService networkService;
    public void setMainLayoutController(MainLayoutController mlc) { this.mainLayoutController = mlc; }
    public MainLayoutController getMainLayoutController() { return mainLayoutController; }
    public void setAdminDashboardController(AdminDashboardController adc) { this.adminDashboardController = adc; }
    public AdminDashboardController getAdminDashboardController() { return adminDashboardController; }
    public User getCurrentUser() { return currentUser; }
    public AuctionController() {
    }
    @FXML
    public void initialize() {
        AuctionUIHelper.setupTableColumns(nameColumn, descriptionColumn, priceColumn, statusColumn, myStatusColumn, endTimeColumn, () -> currentUser);
        itemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                selectedItemText.setText(newSel.getName() + " [" + newSel.getStatus() + "]");
                AuctionUIHelper.updateChart(newSel, priceSeries);
                fetchBidHistory(newSel.getId());
                if (viewDetailsButton != null) viewDetailsButton.setDisable(false);
            } else {
                selectedItemText.setText("No item selected");
                priceChart.getData().clear();
                bidHistoryList.getItems().clear();
                if (autoBidStatusLabel != null) autoBidStatusLabel.setText("");
                if (viewDetailsButton != null) viewDetailsButton.setDisable(true);
            }
        });
        priceChart.getData().add(priceSeries);
        if (statusFilter != null) {
            statusFilter.getItems().addAll("ALL", "OPEN", "RUNNING", "FINISHED");
            statusFilter.setValue("ALL");
            statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterItems());
        }
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterItems());
        }
        if (viewDetailsButton != null) viewDetailsButton.setDisable(true);
        timerExecutor.scheduleAtFixedRate(
                () -> Platform.runLater(() -> itemTable.refresh()),
                1, 1, TimeUnit.SECONDS);
    }
    public void setConnectionAndUser(ClientConnection connection, User user) {
        this.connection = connection;
        this.apiClient = new AuctionApiClient(connection);
        this.networkService = new AuctionNetworkService(connection, this);
        this.networkService.startListening();
        setCurrentUser(user);
    }
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            if ("SELLER".equals(user.getRole())) {
                if (createNewItemButton != null) { createNewItemButton.setVisible(true); createNewItemButton.setManaged(true); }
                if (myItemsButton != null) { myItemsButton.setVisible(true); myItemsButton.setManaged(true); }
                if (topUpButton != null) { topUpButton.setVisible(false); topUpButton.setManaged(false); }
                if (userBalanceLabel != null) { userBalanceLabel.setVisible(false); userBalanceLabel.setManaged(false); }
            } else if (userBalanceLabel != null && "BIDDER".equals(user.getRole())) {
                userBalanceLabel.setVisible(true); userBalanceLabel.setManaged(true);
                Bidder bidder = (Bidder) user;
                updateBalanceDisplay(bidder.getBalance());
                if (topUpButton != null) { topUpButton.setVisible(true); topUpButton.setManaged(true); }
            }
        }
    }
    public void loadItems(ObservableList<Item> items) {
        Platform.runLater(() -> {
            masterData.setAll(items);
            filterItems();
        });
    }
    private void filterItems() {
        if (searchField == null || statusFilter == null) return;
        String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String status = statusFilter.getValue() == null ? "ALL" : statusFilter.getValue();
        ObservableList<Item> filtered = FXCollections.observableArrayList();
        for (Item item : masterData) {
            boolean matchesSearch = item.getName().toLowerCase().contains(searchText);
            boolean matchesStatus = status.equals("ALL") || (item.getStatus() != null && item.getStatus().name().equals(status));
            if (matchesSearch && matchesStatus) {
                filtered.add(item);
            }
        }
        itemTable.setItems(filtered);
    }
    public void fetchItemsFromServer() {
        apiClient.getItems();
    }
    private void fetchBidHistory(String itemId) {
        apiClient.getBidHistory(itemId);
    }
    public void updateBidHistoryList(List<Map<String, Object>> bids) {
        Platform.runLater(() -> {
            bidHistoryList.getItems().clear();
            for (Map<String, Object> bid : bids) {
                String name = bid.getOrDefault("bidderName", bid.get("bidderId")).toString();
                double amount = ((Number) bid.get("amount")).doubleValue();
                String status = bid.getOrDefault("status", "").toString();
                bidHistoryList.getItems().add(String.format("$%.2f — %s [%s]", amount, name, status));
            }
        });
    }
    public void addNotification(String text) {
        notificationList.getItems().add(0, text);
        if (notificationList.getItems().size() > 100) {
            notificationList.getItems().remove(100, notificationList.getItems().size());
        }
    }
    @FXML
    private void handleCreateNewItem() {
        DialogManager.handleCreateNewItem(connection, currentUser, this::fetchItemsFromServer);
    }
    @FXML
    private void handlePlaceBid() {
        Item selectedItem = itemTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showBidResult(false, "Please select an item to bid.");
            return;
        }
        if (selectedItem.getStatus() != Item.Status.RUNNING) {
            showBidResult(false, "Auction is not active (status: " + selectedItem.getStatus() + ").");
            return;
        }
        String amountText = bidAmountField.getText().trim();
        if (amountText.isEmpty()) {
            showBidResult(false, "Please enter a bid amount.");
            return;
        }
        try {
            double amount = Double.parseDouble(amountText);
            apiClient.placeBid(selectedItem.getId(), currentUser.getId(), amount);
            bidMessageLabel.setTextFill(javafx.scene.paint.Color.GRAY);
            bidMessageLabel.setText("Sending bid...");
        } catch (NumberFormatException e) {
            showBidResult(false, "Invalid amount format.");
        } catch (Exception e) {
            showBidResult(false, "Network error: " + e.getMessage());
        }
    }
    @FXML
    private void handleAutoBid() {
        Item selectedItem = itemTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showBidResult(false, "Please select an item for Auto-Bid.");
            return;
        }
        if (currentUser == null || !"BIDDER".equals(currentUser.getRole())) {
            showBidResult(false, "Only bidders can use Auto-Bid.");
            return;
        }
        String maxText = maxBidField.getText().trim();
        String stepText = incrementField.getText().trim();
        if (maxText.isEmpty() || stepText.isEmpty()) {
            showBidResult(false, "Enter Max Bid and Step for Auto-Bid.");
            return;
        }
        try {
            double maxBid = Double.parseDouble(maxText);
            double increment = Double.parseDouble(stepText);
            if (maxBid <= selectedItem.getCurrentHighestBid()) {
                showBidResult(false, "Max bid must be higher than current price.");
                return;
            }
            apiClient.registerAutoBid(selectedItem.getId(), currentUser.getId(), maxBid, increment);
            bidMessageLabel.setTextFill(javafx.scene.paint.Color.GRAY);
            bidMessageLabel.setText("Registering auto-bid...");
        } catch (NumberFormatException e) {
            showBidResult(false, "Invalid Auto-Bid parameters.");
        } catch (Exception e) {
            showBidResult(false, "Network error: " + e.getMessage());
        }
    }
    @FXML
    private void handleViewDetails() {
        Item selectedItem = itemTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showBidResult(false, "Please select an item to view details.");
            return;
        }
        apiClient.getItemDetails(selectedItem.getId());
    }
    @FXML
    private void handleLogout() {
        try {
            if (connection != null) {
                apiClient.logout();
                connection.close();
            }
            timerExecutor.shutdown();
            com.auction.client.AuctionClient.showLogin();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleShowMyItems() {
        apiClient.getSellerItems(currentUser.getId());
    }
    @FXML
    private void handleTopUp() {
        DialogManager.handleTopUp(connection, currentUser, this::addNotification);
    }
    public void updateItemBid(String itemId, double newAmount, String newBidderId, String itemName, String bidderName, double amount) {
        for (Item it : itemTable.getItems()) {
            if (it.getId().equals(itemId)) {
                it.setCurrentHighestBid(newAmount);
                it.setHighestBidderId(newBidderId);
                break;
            }
        }

        itemTable.refresh();
        String log = String.format("[BID] %s placed $%.2f on \"%s\"", bidderName, amount, itemName);
        addNotification(log);
        if (adminDashboardController != null) adminDashboardController.addSystemLog(log);
        Item selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getId().equals(itemId)) {
            String timeLabel = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            priceSeries.getData().add(new XYChart.Data<>(timeLabel, amount));
            fetchBidHistory(selected.getId());
        }
    }
    public void showBidResult(boolean success, String message) {
        if (success) {
            bidMessageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            bidMessageLabel.setText(message.equals("true") ? "Bid placed successfully!" : message);
            bidAmountField.clear();
        } else {
            bidMessageLabel.setTextFill(javafx.scene.paint.Color.RED);
            bidMessageLabel.setText(message.startsWith("Bid failed") ? message : "Bid failed: " + message);
        }
    }
    public void updateItemStatus(Item changedItem) {
        for (int i = 0; i < itemTable.getItems().size(); i++) {
            if (itemTable.getItems().get(i).getId().equals(changedItem.getId())) {
                itemTable.getItems().set(i, changedItem);
                break;
            }
        }
        itemTable.refresh();
        Item selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getId().equals(changedItem.getId())) {
            selectedItemText.setText(selected.getName() + " [" + selected.getStatus() + "]");
        }
        String winner = changedItem.getHighestBidderId();
        String notif;
        if (changedItem.getStatus() == Item.Status.FINISHED) {
            notif = String.format("[CLOSED] \"%s\" auction ended! Winner: %s @ $%.2f",
                    changedItem.getName(), winner != null ? winner : "No bids", changedItem.getCurrentHighestBid());

            if (currentUser != null && currentUser.getId().equals(winner)) {
                apiClient.getItemDetails(changedItem.getId());
            }
        } else {
            notif = String.format("[STATUS] \"%s\" is now %s", changedItem.getName(), changedItem.getStatus());
        }
        addNotification(notif);
    }
    public void updateAutoBidStatus(double max, double increment) {
        if (autoBidStatusLabel != null) {
            autoBidStatusLabel.setText(String.format("ACTIVE: Max $%.2f | Step $%.2f", max, increment));
        }
    }
    public void updateBalanceDisplay(double newBalance) {
        if (userBalanceLabel != null) {
            userBalanceLabel.setText(String.format("Balance: $%.2f", newBalance));
        }
    }
}
