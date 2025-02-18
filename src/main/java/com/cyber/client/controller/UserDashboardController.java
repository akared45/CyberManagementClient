package com.cyber.client.controller;

import com.cyber.client.client.ClientManager;
import com.cyber.client.model.User;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCombination;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;

public class UserDashboardController {
    private static final double EXCHANGE_RATE = 10000;
    @FXML
    private Label lblName;
    @FXML
    private Text txtUsage;
    @FXML
    private Text txtRemaining;
    @FXML
    private Button handleFood;
    @FXML
    private ProgressBar progressBar;
    private double initialPlaytime;
    private Instant startTime;
    private AnimationTimer timer;
    private User loggedInUser;
    public void setUser(User user) {
        this.loggedInUser = user;
        System.out.println("User set in dashboard: " + user.getName() + ", Balance: " + user.getBalance());
        ClientManager.startListeningBalance(this::handleServerMessage);
        updateUserData(user);
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
                System.out.println("🔒 LOCK command received! Switching to Login.fxml");

                Platform.runLater(() -> {
                    try {
                        loggedInUser = null;
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cyber/client/view/Login.fxml"));
                        Parent root = loader.load();
                        Scene scene = new Scene(root);
                        Stage stage = (Stage) lblName.getScene().getWindow();
                        stage.setScene(scene);
                        stage.setMaximized(true);
                        stage.setTitle("Login");
                        stage.setFullScreen(true);
                        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
                        stage.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("⚠️ Error loading Login.fxml!");

                    }
                });
            } else {
                System.out.println("⚠️ Unexpected message format: " + message);
                DecimalFormat formatter = new DecimalFormat("#,### đ");
                System.out.println("Số tiền: " + formatter.format(Double.parseDouble(message)));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cyber/client/view/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) lblName.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Cyber Management");
            stage.setFullScreen(true);
            stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
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
            alert.setTitle("Thông báo");
            alert.setHeaderText(null);
            alert.setContentText("Bạn đã hết thời gian sử dụng! Vui lòng nạp tiền để tiếp tục.");
            alert.show();
            PauseTransition delay = new PauseTransition(javafx.util.Duration.millis(5000));
            delay.setOnFinished(event -> {
                alert.close();
                returnToLogin();
            });
            delay.play();
        });
    }

    @FXML
    public void handleFood(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cyber/client/view/Food.fxml"));
        Parent root = loader.load();
        FoodController foodController = loader.getController();
        foodController.setUser(loggedInUser);
        Stage newStage = new Stage();
        newStage.setScene(new Scene(root));
        newStage.setTitle("Dịch vụ");
        newStage.show();
    }


}