package com.cyber.client.controller;

import com.cyber.client.client.ClientManager;
import com.cyber.client.client.ClientStatus;
import com.cyber.client.database.DatabaseConnection;
import com.cyber.client.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.util.Objects;

public class LoginController {
    @FXML
    public HBox rootHBox;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private ImageView loginImage;

    @FXML
    private VBox loginVBox;

    @FXML
    private VBox registerVBox;

    @FXML
    public void initialize() {
        loginImage.setImage(new Image(Objects.requireNonNull(getClass().getResource("/com/cyber/client/assets/navi.jpg")).toExternalForm()));
    }

    @FXML
    private void switchToRegister() {
        loginVBox.setVisible(false);
        registerVBox.setVisible(true);
    }

    @FXML
    private void handleBackToLogin() {
        registerVBox.setVisible(false);
        loginVBox.setVisible(true);
    }
    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter both username and password!");
            return;
        }
        String loginMessage = "LOGIN:" + username + ":" + password;
        String response = ClientManager.sendMessage(loginMessage);

        if (response != null && response.startsWith("LOGIN_SUCCESS:"))  {
            String[] parts = response.split(":");
            if (parts.length == 3) {
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Login successful!");
                String loggedInUsername = parts[1];
                double balance = Double.parseDouble(parts[2]);
                User loggedInUser=new User(loggedInUsername,balance);
                loadDashboard(loggedInUser);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid response from server!");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Incorrect username or password!");
        }
    }

    private void loadDashboard(User loggedInUser) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/cyber/client/view/UserDashboard.fxml"));
            Parent root = fxmlLoader.load();
            UserDashboardController userDashboardController = fxmlLoader.getController();
            userDashboardController.setUser(loggedInUser);
            Stage stage = getStage(root);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load the user dashboard!");
        }
    }
    private Stage getStage(Parent root) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double windowWidth = screenBounds.getWidth() * 0.3;
        double windowHeight = screenBounds.getHeight();

        Scene scene = new Scene(root, windowWidth, windowHeight);
        Stage stage = (Stage) rootHBox.getScene().getWindow();
        stage.setScene(scene);
        stage.setWidth(windowWidth);
        stage.setHeight(windowHeight);
        stage.setTitle("User Dashboard");
        stage.setX(screenBounds.getMaxX() - windowWidth);
        stage.setY(0);
        stage.setTitle("User Dashboard");
        return stage;
    }
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
