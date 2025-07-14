package controller;

import config.AppConfig;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;
import model.CartItem;
import service.CartService;
import utils.NavigationManager;
import utils.Result;
import utils.UserSession;
import java.util.List;

/**
 * Controller for the cart view. Displays cart items and allows updating quantities or removing events.
 */
public class CartController{
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> titleCol;
    @FXML private TableColumn<CartItem, Integer> quantityCol;
    @FXML private TableColumn<CartItem, String> venueCol;
    @FXML private TableColumn<CartItem, Double> priceCol;
    @FXML private TableColumn<CartItem, String> dayCol;
    @FXML private TableColumn<CartItem, Integer> availableCol;
    @FXML private TableColumn<CartItem, Double> totalPriceCol;
    @FXML private TableColumn<CartItem, String> statusCol;

    @FXML private Button removeButton;
    @FXML private Label statusLabel;
    @FXML private Label totalPriceLabel;

    @FXML
    private Button backButton;
    @FXML
    private Button checkoutButton;


    private CartService cartService;
    private String username;

    public void setCartService(CartService cartService) {
        this.cartService = cartService;
        this.username = UserSession.getCurrentUser();
        loadCart();
    }


    @FXML public void initialize() {
        setupTableColumns();
        Image check = new Image(getClass().getResourceAsStream("/icons/check.png"));
        ImageView checkView = new ImageView(check);
        checkView.setFitWidth(AppConfig.ICON_SIZE);
        checkView.setFitHeight(AppConfig.ICON_SIZE);
        checkoutButton.setGraphic(checkView);
    }

    private void setupTableColumns() {
        titleCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getEvent().getTitle()));
        venueCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getEvent().getVenue()));
        dayCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getEvent().getDay()));
        priceCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getEvent().getPrice()));
        availableCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getEvent().getAvailableTickets()));
        totalPriceCol.setCellValueFactory(cellData -> {
            CartItem item = cellData.getValue();
            return new ReadOnlyObjectWrapper<>(item.getEvent().getPrice() * item.getQuantity());
        });
        statusCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(itemStatus(cellData.getValue())));
        setupQuantityColumn();
    }

    private String itemStatus(CartItem item) {
        return item.getEvent().isActive() ? "Available" : "Unavailable";
    }

    private void loadCart() {
        try {
            List<CartItem> items = cartService.getCartItems(username);
            ObservableList<CartItem> list = FXCollections.observableArrayList(items);
            cartTable.setItems(list);
            cartTable.setEditable(true);
            updateTotalPrice(list);
            updateCheckoutButton(list);
        } catch (Exception e) {
            cartTable.setItems(FXCollections.observableArrayList());
            statusLabel.setText("Error loading cart.");
        }
    }

    private void updateTotalPrice(ObservableList<CartItem> items) {
        double total = items.stream().mapToDouble(i -> i.getEvent().getPrice() * i.getQuantity()).sum();
        totalPriceLabel.setText(String.format("Total: $%.2f", total));
    }


    private void updateCheckoutButton(ObservableList<CartItem> items) {
        boolean hasInvalid = items.stream().anyMatch(item -> item.getQuantity() > item.getEvent().getAvailableTickets());
        checkoutButton.setDisable(hasInvalid);
    }

    // Set up the quantity column with editing and highlighting
    private void setupQuantityColumn() {
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        quantityCol.setCellFactory(col -> new TextFieldTableCell<CartItem, Integer>(new IntegerStringConverter()) {
            @Override
            public void updateItem(Integer quantity, boolean empty) {
                super.updateItem(quantity, empty);
                if (empty || quantity == null) {
                    setText(null);
                    setStyle("");
                } else {
                    CartItem cartItem = getTableView().getItems().get(getIndex());
                    int available = cartItem.getEvent().getAvailableTickets();
                    setText(quantity.toString());
                    if (quantity > available) {
                        setStyle("-fx-background-color: red; -fx-text-fill: white;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        quantityCol.setOnEditCommit(this::handleQuantityEdit);
    }

    // Handle quantity edit commit logic
    private void handleQuantityEdit(TableColumn.CellEditEvent<CartItem, Integer> event) {
        CartItem item = event.getRowValue();
        int newQuantity = event.getNewValue();
        int available = item.getEvent().getAvailableTickets();

        if (newQuantity <= 0) {
            showAlert(Alert.AlertType.WARNING, "Invalid Quantity", "Quantity must be greater than 0.");
            cartTable.refresh();
            return;
        }
        if (newQuantity > available) {
            showAlert(Alert.AlertType.WARNING, "Quantity Exceeds Available Tickets",
                    "The event only has " + available + " tickets available.");
            cartTable.refresh();
            return;
        }

        item.setQuantity(newQuantity);

        Result<String> result = cartService.updateCartItem(item);
        statusLabel.getStyleClass().setAll(result.isSuccess() ? "success-label" : "error-label");
        statusLabel.setText(result.getMessage());
        cartTable.refresh();
        updateTotalPrice(cartTable.getItems());
        updateCheckoutButton(cartTable.getItems());
    }

    @FXML private void handleRemove() {
        CartItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Result<String> result = cartService.removeCartItem(username, selected.getEvent().getEventId());
            statusLabel.getStyleClass().setAll(result.isSuccess() ? "success-label" : "error-label");
            statusLabel.setText(result.getMessage());
            loadCart();
        }
    }

    @FXML private void handleCheckout() {
        Result<String> validation = cartService.validateCartBeforeCheckout(username);
        if (!validation.isSuccess()) {
            showAlert(Alert.AlertType.WARNING, "Checkout Blocked", validation.getMessage());
            loadCart();
            return;
        }
        NavigationManager.loadScene("/view/CheckoutView.fxml", "Checkout");
    }

    @FXML
    private void handleRefresh() {
        loadCart();  // Reload latest data from database
    }

    @FXML
    private void handleBack() {
        NavigationManager.goBack();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        if (content == null || content.trim().isEmpty()) {
            content = type == Alert.AlertType.INFORMATION
                    ? "Operation completed successfully." : "";
        }
        alert.setContentText(content);
        alert.showAndWait();
    }
}
