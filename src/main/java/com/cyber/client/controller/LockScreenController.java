package com.cyber.client.controller;

import com.cyber.client.client.ClientManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.util.Objects;

public class LockScreenController {

    @FXML
    private ImageView backgroundImage;

    public void initialize() {
        Image bgImage = new Image(Objects.requireNonNull(getClass().getResource("/com/cyber/client/assets/lock.jpg")).toExternalForm());
        backgroundImage.setImage(bgImage);
        ClientManager.startListeningBalance(this::handleServerMessage);
    }

    public void handleServerMessage(String message) {
        System.out.println("Message from server: " + message);
        try {
            if (message.equals("UNLOCK")) {
                System.out.println("🔓 UNLOCK command received! Switching to LoginRegister.fxml");
                Platform.runLater(() -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cyber/client/view/LoginRegister.fxml"));
                        Parent root = loader.load();
                        Scene scene = new Scene(root);

                        Stage stage = (Stage) Stage.getWindows().stream()
                                .filter(Window::isShowing)
                                .findFirst()
                                .orElse(null);

                        if (stage == null) {
                            System.out.println("⚠️ Error: No active stage found!");
                            return;
                        }

                        stage.setScene(scene);
                        stage.setMaximized(true);
                        stage.setFullScreen(true);
                        stage.setFullScreenExitHint("");
                        stage.setAlwaysOnTop(true);
                        stage.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("⚠️ Error loading LoginRegister.fxml!");
                    }
                });
            } else {
                System.out.println("⚠️ Unexpected message format: " + message);
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + message);
            e.printStackTrace();
        }
    }
}
