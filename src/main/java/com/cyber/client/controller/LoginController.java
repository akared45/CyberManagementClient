package com.cyber.client.controller;

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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
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

    private User loggedInUser;
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

    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter both username and password!");
            return;
        }
        if (validateLogin(username, password)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Login successful!");
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/cyber/client/view/UserDashboard.fxml"));
                Parent root = fxmlLoader.load();
                UserDashboardController dashboardController = fxmlLoader.getController();
                dashboardController.setUser(loggedInUser);
                Stage stage = getStage(root);
                stage.show();
                ClientStatus.startListeningForLockCommand(stage);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load the user dashboard!");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Incorrect username or password!");
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

    private boolean validateLogin(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int id = resultSet.getInt("user_id");
                String name = resultSet.getString("username");
                String userPassword = resultSet.getString("password");
                double balance = resultSet.getDouble("balance");
                LocalDateTime created = resultSet.getTimestamp("create_date").toLocalDateTime();

                loggedInUser = new User(id, name, userPassword, balance, created);
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot connect to the database!");
        }
        return false;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
