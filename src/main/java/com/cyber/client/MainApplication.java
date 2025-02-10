package com.cyber.client;

import com.cyber.client.client.ClientStatus;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        new Thread(ClientStatus::sendOnlineStatus).start();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/cyber/client/view/Login.fxml"));
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        Image logo = new Image(Objects.requireNonNull(getClass().getResource("/com/cyber/client/assets/logo.jpg")).toExternalForm());
        Scene scene = new Scene(fxmlLoader.load(), primaryScreenBounds.getWidth(), primaryScreenBounds.getHeight());
        stage.getIcons().add(logo);
        stage.setWidth(primaryScreenBounds.getWidth());
        stage.setHeight(primaryScreenBounds.getHeight());
        stage.setTitle("Cyber Management");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> ClientStatus.sendOfflineStatus());
    }

    public static void main(String[] args) {
        launch();
    }
}