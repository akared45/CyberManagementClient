package com.cyber.client.controller;

import com.cyber.client.database.DatabaseConnection;
import com.cyber.client.model.Food;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class FoodController implements Initializable {
    public static final String CURRENCY = "$";
    @FXML
    public Label descriptionLabel;

    @FXML
    private VBox chosenFruitCard;

    @FXML
    private Label fruitNameLable;

    @FXML
    private Label fruitPriceLabel;

    @FXML
    private ImageView fruitImg;

    @FXML
    private GridPane grid;

    private final List<Food> foods = new ArrayList<>();

    private List<Food> getDataFromDatabase() {
        List<Food> foods = new ArrayList<>();
        String query = "SELECT FOODS.food_name, FOODS.description ,FOODS.price, FOODS.quantity, FOODS.image_url, FOOD_CATEGORIES.category_name " +
                "FROM FOODS " +
                "JOIN FOOD_CATEGORIES ON FOODS.category_id = FOOD_CATEGORIES.category_id ";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Food food = new Food();
                food.setName(rs.getString("food_name"));
                food.setDescription(rs.getString("description"));
                food.setPrice(rs.getDouble("price"));
                food.setImgSrc(rs.getString("image_url"));
                food.setColor("36454F");
                foods.add(food);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return foods;
    }


    public static Image loadImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            if (imageUrl.startsWith("http") || imageUrl.startsWith("https")) {
                return new Image(imageUrl);
            } else if (imageUrl.startsWith("file:/")) {
                return new Image(imageUrl);
            } else {
                return new Image(Objects.requireNonNull(FoodController.class.getResourceAsStream("/com/cyber/client/assets/kiwi.png")));
            }
        } else {
            return new Image(Objects.requireNonNull(FoodController.class.getResourceAsStream("/com/cyber/client/assets/kiwi.png")));
        }
    }

    private void setChosenFruit(Food food) {
        fruitNameLable.setText(food.getName());
        descriptionLabel.setText(food.getDescription());
        fruitPriceLabel.setText(CURRENCY + food.getPrice());
        fruitImg.setImage(loadImage(food.getImgSrc()));
        chosenFruitCard.setStyle("-fx-background-color: #" + food.getColor() + ";\n" +
                "    -fx-background-radius: 30;");
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        foods.addAll(getDataFromDatabase());
        if (foods.size() > 0) {
            setChosenFruit(foods.get(0));
        }
        int column = 0;
        int row = 1;
        try {
            for (int i = 0; i < foods.size(); i++) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/com/cyber/client/view/Item.fxml"));
                AnchorPane anchorPane = fxmlLoader.load();
                ItemController itemController = fxmlLoader.getController();
                itemController.setData(foods.get(i), this);
                if (column == 3) {
                    column = 0;
                    row++;
                }
                grid.add(anchorPane, column++, row);
                grid.setMinWidth(Region.USE_COMPUTED_SIZE);
                grid.setPrefWidth(Region.USE_COMPUTED_SIZE);
                grid.setMaxWidth(Region.USE_PREF_SIZE);
                grid.setMinHeight(Region.USE_COMPUTED_SIZE);
                grid.setPrefHeight(Region.USE_COMPUTED_SIZE);
                grid.setMaxHeight(Region.USE_PREF_SIZE);

                GridPane.setMargin(anchorPane, new Insets(10));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onFruitClicked(Food food) {
        setChosenFruit(food);
    }
}