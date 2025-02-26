package com.cyber.client.controller;

import com.cyber.client.client.ClientManager;
import com.cyber.client.database.DatabaseConnection;
import com.cyber.client.model.Food;
import com.cyber.client.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FoodController implements Initializable {
    public static final String CURRENCY = "$";
    private static final String DEFAULT_IMAGE_PATH = "/com/cyber/client/assets/kiwi.png";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @FXML private TextField searchField;
    @FXML private Label userAccount;
    @FXML private VBox chosenFoodCard;
    @FXML private Label foodNameLabel;
    @FXML private Label foodPriceLabel;
    @FXML public Label descriptionLabel;
    @FXML private ImageView foodImg;
    @FXML private GridPane grid;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private VBox paymentItems;
    @FXML private Label totalLabel;

    private final List<Food> foods = new ArrayList<>();
    private double total = 0.0;
    private User loggedInUser;

    public void setUser(User user) {
        this.loggedInUser = user;
        userAccount.setText(loggedInUser.getName());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeSpinner();
        loadFoodsFromDatabase();
        if (!foods.isEmpty()) {
            setChosenFood(foods.getFirst());
        }
        populateFoodGrid(foods);
    }

    private void initializeSpinner() {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        quantitySpinner.setValueFactory(valueFactory);
    }

    private void loadFoodsFromDatabase() {
        String query = "SELECT FOODS.food_id, FOODS.food_name, FOODS.description, FOODS.price, FOODS.quantity, FOODS.image_url, FOOD_CATEGORIES.category_name " +
                "FROM FOODS " +
                "JOIN FOOD_CATEGORIES ON FOODS.category_id = FOOD_CATEGORIES.category_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Food food = new Food();
                food.setId(rs.getInt("food_id"));
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
    }

    private void populateFoodGrid(List<Food> foodsToDisplay) {

        grid.getChildren().clear();
        int column = 0;
        int row = 1;
        try {
            for (Food food : foodsToDisplay) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/com/cyber/client/view/Item.fxml"));
                AnchorPane anchorPane = fxmlLoader.load();
                ItemController itemController = fxmlLoader.getController();
                itemController.setData(food, this);
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

    public void onFoodClicked(Food food) {
        setChosenFood(food);
    }

    private void setChosenFood(Food food) {
        foodNameLabel.setText(food.getName());
        foodPriceLabel.setText(CURRENCY + food.getPrice());
        descriptionLabel.setText(food.getDescription());
        foodImg.setImage(loadImage(food.getImgSrc()));
        chosenFoodCard.setStyle("-fx-background-color: #" + food.getColor() + "; -fx-background-radius: 30;");
        quantitySpinner.getValueFactory().setValue(1);
    }

    public static Image loadImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            if (imageUrl.startsWith("http") || imageUrl.startsWith("https")) {
                return new Image(imageUrl);
            } else {
                File file = new File(imageUrl);
                if (file.exists()) {
                    return new Image(file.toURI().toString());
                }
            }
        }
        return new Image(Objects.requireNonNull(FoodController.class.getResourceAsStream(DEFAULT_IMAGE_PATH)));
    }

    @FXML
    private void addToCart() {
        int quantity = quantitySpinner.getValue();
        double price = Double.parseDouble(foodPriceLabel.getText().replace(CURRENCY, ""));
        double itemTotal = quantity * price;
        Label itemLabel = createCartItemLabel(foodNameLabel.getText(), quantity, itemTotal);
        Button removeButton = createRemoveButton(itemLabel, itemTotal);
        HBox container = new HBox(10, itemLabel, removeButton);
        paymentItems.getChildren().add(container);
        updateTotal(itemTotal);
    }

    private Label createCartItemLabel(String foodName, int quantity, double itemTotal) {
        Label label = new Label(foodName + " x " + quantity + " - " + CURRENCY + String.format("%.2f", itemTotal));
        label.getStyleClass().add("cart-item-label");
        return label;
    }

    private Button createRemoveButton(Label itemLabel, double itemTotal) {
        Button removeButton = new Button();
        ImageView trashIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/cyber/client/assets/trash.jpg"))));
        trashIcon.setFitWidth(50);
        trashIcon.setFitHeight(50);
        removeButton.setGraphic(trashIcon);
        removeButton.setStyle("-fx-background-color: transparent; -fx-padding: 4px;");
        removeButton.setOnAction(e -> {
            paymentItems.getChildren().removeIf(node -> node instanceof HBox && ((HBox) node).getChildren().contains(itemLabel));
            updateTotal(-itemTotal);
        });
        return removeButton;
    }

    private void updateTotal(double amount) {
        total += amount;
        totalLabel.setText("Total: " + CURRENCY + String.format("%.2f", total));
    }

    @FXML
    public void handleCheckOut() {
        if (paymentItems.getChildren().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Checkout Failed", "Your cart is empty.");
            return;
        }
        if (loggedInUser == null) {
            showAlert(Alert.AlertType.ERROR, "Login Required", "You need to log in to place an order.");
            return;
        }
        if (!checkStockAvailability()) {
            return;
        }

        Map<Food, Integer> orderedFoods = extractOrderedFoods();
        double totalAmount = calculateTotalAmount(orderedFoods);
        int orderId = insertOrderIntoDatabase(totalAmount);

        if (orderId != -1) {
            insertOrderDetailsIntoDatabase(orderId, orderedFoods);
            try {
                ClientManager.sendMessage("New Order!!!");
                System.out.println("Message sent to server: Insert");
            } catch (Exception e) {
                System.out.println("Failed to send message: " + e.getMessage());
            }
            clearCartAndShowSuccessAlert();
        } else {
            showAlert(Alert.AlertType.ERROR, "Order Failed", "Could not process your order.");
        }
    }

    private boolean checkStockAvailability() {
        String sql = "SELECT quantity FROM FOODS WHERE food_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Map.Entry<Food, Integer> entry : extractOrderedFoods().entrySet()) {
                Food food = entry.getKey();
                int orderedQuantity = entry.getValue();

                stmt.setInt(1, food.getId());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int availableQuantity = rs.getInt("quantity");
                    if (availableQuantity < orderedQuantity) {
                        showAlert(Alert.AlertType.ERROR, "Insufficient Stock",
                                "Not enough stock for " + food.getName() + ". Available: " + availableQuantity);
                        return false;
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Stock Error", "Food item not found in database.");
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }


    private void clearCartAndShowSuccessAlert() {
        paymentItems.getChildren().clear();
        total = 0.0;
        totalLabel.setText("Total: " + CURRENCY + String.format("%.2f", total));
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notification");
        alert.setHeaderText(null);
        alert.setContentText("Order successful, please wait!");
        alert.showAndWait();
    }

    private Map<Food, Integer> extractOrderedFoods() {
        Map<Food, Integer> orderedFoods = new HashMap<>();
        for (Node node : paymentItems.getChildren()) {
            if (node instanceof HBox hbox) {
                for (Node child : hbox.getChildren()) {
                    if (child instanceof Label label) {
                        String[] parts = label.getText().split("\\s*-\\s*\\" + CURRENCY);
                        if (parts.length == 2) {
                            String[] foodParts = parts[0].split(" x ");
                            if (foodParts.length == 2) {
                                String foodName = foodParts[0].trim();
                                int quantity = Integer.parseInt(foodParts[1].trim());
                                Food food = foods.stream()
                                        .filter(f -> f.getName().equals(foodName))
                                        .findFirst()
                                        .orElse(null);
                                if (food != null) {
                                    orderedFoods.put(food, quantity);
                                }
                            }
                        }
                    }
                }
            }
        }

        return orderedFoods;
    }

    private double calculateTotalAmount(Map<Food, Integer> orderedFoods) {
        return orderedFoods.entrySet().stream()
                .mapToDouble(entry -> entry.getKey().getPrice() * entry.getValue())
                .sum();
    }

    private int insertOrderIntoDatabase(double totalAmount) {
        String sql = "INSERT INTO orders (user_id, order_date, total_amount, status) VALUES (?, ?, ?, 'PENDING')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, loggedInUser.getId());
            stmt.setString(2, LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
            stmt.setDouble(3, totalAmount);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    private void insertOrderDetailsIntoDatabase(int orderId, Map<Food, Integer> orderedFoods) {
        String sqlOrderDetails = "INSERT INTO order_details (order_id, food_id, quantity, price, total_price, discount) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlUpdateFood = "UPDATE FOODS SET quantity = quantity - ? WHERE food_id = ? AND quantity >= ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmtOrderDetails = conn.prepareStatement(sqlOrderDetails);
                 PreparedStatement stmtUpdateFood = conn.prepareStatement(sqlUpdateFood)) {

                for (Map.Entry<Food, Integer> entry : orderedFoods.entrySet()) {
                    Food food = entry.getKey();
                    int quantity = entry.getValue();
                    double totalPrice = quantity * food.getPrice();

                    stmtOrderDetails.setInt(1, orderId);
                    stmtOrderDetails.setInt(2, food.getId());
                    stmtOrderDetails.setInt(3, quantity);
                    stmtOrderDetails.setDouble(4, food.getPrice());
                    stmtOrderDetails.setDouble(5, totalPrice);
                    stmtOrderDetails.setDouble(6, 0.0);
                    stmtOrderDetails.executeUpdate();

                    stmtUpdateFood.setInt(1, quantity);
                    stmtUpdateFood.setInt(2, food.getId());
                    stmtUpdateFood.setInt(3, quantity);
                    int affectedRows = stmtUpdateFood.executeUpdate();

                    if (affectedRows == 0) {
                        conn.rollback();
                        showAlert(Alert.AlertType.ERROR, "Order Failed", "Not enough stock for " + food.getName());
                        return;
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim().toLowerCase();
            List<Food> filteredFoods = foods.stream()
                    .filter(food -> food.getName().toLowerCase().contains(keyword))
                    .toList();
            populateFoodGrid(filteredFoods);
    }
}