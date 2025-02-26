package com.cyber.client.controller;

import com.cyber.client.client.ClientManager;
import com.cyber.client.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class DepositController {

    @FXML
    private TextField amountField;

    private User loggedInUser;
    private UserDashboardController dashboardController;

    public void setUser(User user, UserDashboardController dashboardController) {
        this.loggedInUser = user;
        this.dashboardController = dashboardController;
    }

    @FXML
    public void handleDeposit() {
        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) {
            showAlert("Error", "Please enter amount!");
            return;
        }
        try {
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                showAlert("Error", "Amount must be greater than 0!");
                return;
            }
            double remainingBalance = dashboardController.calculateRemainingBalance();
            loggedInUser.setBalance(remainingBalance);
            String message = "DEPOSIT:" + loggedInUser.getId() + ":" + amount + ":" + remainingBalance;
            String response = ClientManager.sendMessage(message);
            if (response != null && response.startsWith("DEPOSIT_PENDING:")) {
                showAlert("Deposit Pending", "Your deposit is pending admin confirmation. Please wait for further updates.");
            } else {
                System.out.println("Error");
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid number!");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
