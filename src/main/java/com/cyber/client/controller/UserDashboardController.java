package com.cyber.client.controller;

import com.cyber.client.client.ClientManager;
import com.cyber.client.client.ClientStatus;
import com.cyber.client.database.DatabaseConnection;
import com.cyber.client.model.User;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UserDashboardController {
    private static final double EXCHANGE_RATE = 10000;
    @FXML
    public TextField amountField;
    @FXML
    private Button logoutButton;
    @FXML
    private Label lblName;
    @FXML
    private Text txtUsage;
    @FXML
    private Text txtRemaining;

    @FXML
    private ProgressBar progressBar;

    private double initialPlaytime;

    private Instant startTime;

    private AnimationTimer timer;

    private User loggedInUser;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void setUser(User user) {
        this.loggedInUser = user;
        System.out.println("User set in dashboard: " + user.getId() + user.getName() + ", Balance: " + user.getBalance());
        ClientManager.startListeningBalance(this::handleServerMessage);
        updateUserData(user);
        startBalanceUpdate();
    }
    private void startBalanceUpdate() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (loggedInUser != null) {
                    double remainingBalance = calculateRemainingBalance();
                    updateBalanceInDatabase(loggedInUser.getId(), remainingBalance);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    private void updateBalanceInDatabase(int userId, double newBalance) {
        String sql = "UPDATE users SET balance = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, newBalance);
            pstmt.setInt(2, userId);
            int updatedRows = pstmt.executeUpdate();

            if (updatedRows > 0) {
                System.out.println("✅ Updated balance in database: " + newBalance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("⚠️ Error updating balance in database");
        }
    }

    private void handleServerMessage(String message) {
        System.out.println("Message from server: " + message);
        try {
            if (message.startsWith("BALANCE_UPDATE:")) {
                String[] parts = message.split(":");
                if (parts.length == 2) {
                    double newBalance = Double.parseDouble(parts[1]);
                    System.out.println("📥 Received balance update from server: New Balance = " + newBalance);
                    Platform.runLater(() -> {
                        if (loggedInUser != null) {
                            loggedInUser.setBalance(newBalance);
                            updateUserData(loggedInUser);
                        }
                    });
                }
            } else if (message.equals("LOCK")) {
                System.out.println("🔒 LOCK command received! Switching to LoginRegister.fxml");
                Platform.runLater(() -> {
                    try {
                        loggedInUser = null;
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cyber/client/view/Lock.fxml"));
                        Parent root = loader.load();
                        Scene scene = new Scene(root);
                        Stage stage = (Stage) lblName.getScene().getWindow();
                        stage.setScene(scene);
                        stage.setMaximized(true);
                        stage.setFullScreen(true);
                        Platform.setImplicitExit(false);
                        stage.setOnCloseRequest(event -> {
                            event.consume();
                            System.out.println("Close request consumed!");
                            ClientStatus.sendOfflineStatus();
                        });
                        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
                        stage.setAlwaysOnTop(true);
                        stage.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("⚠️ Error loading LoginRegister.fxml!");
                    }
                });
            }else {
                System.out.println("abc");
            }
        } catch (Exception e) {
            System.err.println("Error processing balance or lock message: " + message);
            e.printStackTrace();
        }
    }

    public void updateUserData(User loggedInUser) {
        lblName.setText(loggedInUser.getName());
        double balance = loggedInUser.getBalance();
        initialPlaytime = balance / EXCHANGE_RATE;
        txtUsage.setText("00:00");
        txtRemaining.setText(formatTime(initialPlaytime * 3600));
        progressBar.setProgress(1.0);
        startTime = Instant.now();
        startTimer();
    }

    private void startTimer() {
        if (timer != null) {
            timer.stop();
        }

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                Duration elapsedTime = Duration.between(startTime, Instant.now());
                long elapsedSeconds = elapsedTime.getSeconds();
                double remainingSeconds = initialPlaytime * 3600 - elapsedSeconds;
                if (remainingSeconds <= 0) {
                    txtRemaining.setText("00:00");
                    progressBar.setProgress(0.0);
                    txtUsage.setText(formatTime(initialPlaytime * 3600));
                    timer.stop();
                    showOutOfTimeAlert();
                    return;
                }
                txtUsage.setText(formatTime(elapsedSeconds));
                txtRemaining.setText(formatTime(remainingSeconds));
                double progress = remainingSeconds / (initialPlaytime * 3600);
                progressBar.setProgress(progress);
            }
        };
        timer.start();
    }

    private void returnToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cyber/client/view/LoginRegister.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) lblName.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Cyber Management");
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
            stage.setOnCloseRequest(event -> event.consume());
            ClientStatus.sendOfflineStatus();
            loggedInUser = null;
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String formatTime(double seconds) {
        int minutes = (int) (seconds / 60);
        int secs = (int) (seconds % 60);
        return String.format("%02d:%02d", minutes, secs);
    }

    private void showOutOfTimeAlert() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Notification");
            alert.setHeaderText(null);
            alert.setContentText("Your time has expired! Please recharge to continue.");
            alert.show();
            PauseTransition delay = new PauseTransition(javafx.util.Duration.millis(3000));
            delay.setOnFinished(event -> {
                alert.close();
                returnToLogin();
            });
            delay.play();
        });
    }

    @FXML
    public void handleBalance() throws IOException{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cyber/client/view/Deposit.fxml"));
        Parent root = loader.load();
        DepositController depositController = loader.getController();
        depositController.setUser(loggedInUser, this);
        Stage newStage = new Stage();
        newStage.setScene(new Scene(root));
        newStage.setTitle("Deposit money");
        newStage.show();
    }

    public double calculateRemainingBalance() {
        if (loggedInUser == null || startTime == null) {
            return 0.0;
        }
        Duration elapsedTime = Duration.between(startTime, Instant.now());
        long elapsedSeconds = elapsedTime.getSeconds();
        double usedAmount = elapsedSeconds * EXCHANGE_RATE / 3600;
        double remainingBalance = loggedInUser.getBalance() - usedAmount;
        return Math.max(remainingBalance, 0.0);
    }
    @FXML
    public void handleFood() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cyber/client/view/Food.fxml"));
        Parent root = loader.load();
        FoodController foodController = loader.getController();
        foodController.setUser(loggedInUser);
        Stage newStage = new Stage();
        newStage.setScene(new Scene(root));
        newStage.setTitle("Service");
        newStage.setMaximized(true);
        newStage.show();
    }

    @FXML
    private void Logout() {
        if (loggedInUser != null) {
            int userId = loggedInUser.getId();
            loggedInUser = null;

            endSession(userId);
            ClientStatus.sendOfflineStatus();
            returnToLogin();
        } else {
            System.out.println("⚠️ No logged-in user. Logout skipped.");
        }
    }


    private void endSession(int userId) {
        String sql = "UPDATE sessions " +
                "SET end_time = NOW(), " +
                "    total_time = TIMESTAMPDIFF(SECOND, start_time, NOW()), " +
                "    session_cost = ROUND((TIMESTAMPDIFF(SECOND, start_time, NOW()) / 3600) * 10000, 0) " +
                "WHERE user_id = ? AND end_time IS NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            int updatedRows = pstmt.executeUpdate();

            if (updatedRows > 0) {
                System.out.println("✅ Session ended: User " + userId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}