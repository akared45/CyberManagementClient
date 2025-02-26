package com.cyber.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/cyber/client/view/LoginRegister.fxml"));
        Image logo = new Image(Objects.requireNonNull(getClass().getResource("/com/cyber/client/assets/logo.jpg")).toExternalForm());
        Scene scene = new Scene(fxmlLoader.load());
        stage.getIcons().add(logo);
        stage.setTitle("Cyber Management");
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
