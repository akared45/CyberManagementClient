package com.cyber.client.client;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientStatus {
    public static void sendOnlineStatus() {
        ClientManager.sendMessage("ONLINE");
    }

    public static void sendOfflineStatus() {
        ClientManager.sendMessage("OFFLINE");
    }

    public static void startListeningForLockCommand(Stage primaryStage) {
        new Thread(() -> {
            try {
                System.out.println("🔹 Connecting to server...");
                Socket socket = new Socket(ClientManager.SERVER_IP, ClientManager.SERVER_PORT);
                System.out.println("✅ Connected to server!");

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response = in.readLine();
                System.out.println("Response from server: " + response);
                    if (response == null) {
                        System.out.println("⚠️ Received null message. Connection might be closed!");
                    }
                    System.out.println("📩 Received message: " + response);
                    if (response.equals("LOCK")) {
                        System.out.println("🔒 LOCK command received! Switching to Login.fxml");

                        Platform.runLater(() -> {
                            try {
                                FXMLLoader loader = new FXMLLoader(ClientStatus.class.getResource("/com/cyber/client/view/Login.fxml"));
                                Parent root = loader.load();
                                Scene scene = new Scene(root);

                                primaryStage.setScene(scene);
                                primaryStage.setMaximized(true);
                                primaryStage.setTitle("Login");
                                primaryStage.setFullScreen(true);
                                primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
                                primaryStage.show();
                            } catch (Exception e) {
                                e.printStackTrace();
                                System.out.println("⚠️ Error loading Login.fxml!");
                            }
                        });
                }
                System.out.println("🔴 Closing socket...");
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("⚠️ Error in socket connection!");
            }
        }).start();
    }



}