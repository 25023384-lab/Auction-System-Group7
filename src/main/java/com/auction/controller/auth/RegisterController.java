package com.auction.controller.auth;

import com.auction.client.ClientConnection;
import com.auction.client.api.AuthApiClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField visiblePasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private TextField visibleConfirmPasswordField;
    @FXML
    private CheckBox showPasswordCheckBox;
    @FXML
    private RadioButton bidderRadioButton;
    @FXML
    private RadioButton sellerRadioButton;
    @FXML
    private Button registerButton;
    @FXML
    private Label messageLabel;

    private final ToggleGroup roleToggleGroup = new ToggleGroup();
    private ClientConnection connection;
    private AuthApiClient authApiClient;

    public RegisterController() {
        try {
            connection = new ClientConnection();
            authApiClient = new AuthApiClient(connection);
        } catch (Exception e) {
            Platform.runLater(() -> messageLabel.setText("Cannot connect to server."));
        }
    }

    @FXML
    public void initialize() {
        // Nhóm các RadioButton
        bidderRadioButton.setToggleGroup(roleToggleGroup);
        sellerRadioButton.setToggleGroup(roleToggleGroup);
        bidderRadioButton.setSelected(true);

        // Đồng bộ hóa nội dung giữa các ô password
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        visibleConfirmPasswordField.textProperty().bindBidirectional(confirmPasswordField.textProperty());

        // Thêm listener cho checkbox
        showPasswordCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            togglePasswordVisibility(passwordField, visiblePasswordField, newValue);
            togglePasswordVisibility(confirmPasswordField, visibleConfirmPasswordField, newValue);
        });
    }

    private void togglePasswordVisibility(PasswordField pf, TextField tf, boolean show) {
        if (show) {
            tf.setVisible(true);
            tf.setManaged(true);
            pf.setVisible(false);
            pf.setManaged(false);
        } else {
            tf.setVisible(false);
            tf.setManaged(false);
            pf.setVisible(true);
            pf.setManaged(true);
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        if (connection == null) {
            messageLabel.setText("No connection to server.");
            return;
        }

        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = bidderRadioButton.isSelected() ? "BIDDER" : "SELLER";

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Username and Password cannot be empty.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match.");
            return;
        }

        String email = username + "@example.com";

        try {
            authApiClient.register(username, email, password, role);

            Platform.runLater(() -> {
                messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
                messageLabel.setText("Registration successful! You can now login.");
            });
        } catch (Exception e) {
            Platform.runLater(() -> {
                messageLabel.setTextFill(javafx.scene.paint.Color.RED);
                messageLabel.setText(e.getMessage());
            });
        }
    }

    @FXML
    private void switchToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = (Stage) registerButton.getScene().getWindow();
            Scene scene = new Scene(loginRoot);
            stage.setScene(scene);
            stage.setTitle("Auction System Login");

        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Error: Could not load login screen.");
        }
    }
}
