package com.cyber.client.controller;

import com.cyber.client.model.Food;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class ItemController {
    @FXML
    private Label nameLabel;

    @FXML
    private Label priceLable;

    @FXML
    private ImageView img;

    private Food food;
    private FoodController foodController;

    @FXML
    private void click(MouseEvent mouseEvent) {
        foodController.onFruitClicked(food);
    }

    public void setData(Food food, FoodController foodController) {
        this.food = food;
        this.foodController = foodController;
        nameLabel.setText(food.getName());
        priceLable.setText(FoodController.CURRENCY + food.getPrice());
        img.setImage(FoodController.loadImage(food.getImgSrc()));
    }
}