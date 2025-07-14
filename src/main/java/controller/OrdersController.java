package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import model.Order;
import service.OrderService;
import utils.NavigationManager;
import utils.Result;
import utils.UserSession;
import java.io.File;
import java.util.List;

/**
 * Controller displaying orders history and supporting CSV export.
 */
public class OrdersController{
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, String> orderIdCol;
    @FXML private TableColumn<Order, String> dateCol;
    @FXML private TableColumn<Order, String> itemsCol;
    @FXML private TableColumn<Order, Double> totalCol;
    @FXML private TableColumn<Order, String> usernameCol;
    @FXML private Button exportButton;
    @FXML private Label statusLabel;


    private OrderService orderService;

    public OrdersController() {}

    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
        loadOrders();
    }

    @FXML public void initialize() {
    }

    private void loadOrders() {
        String username = UserSession.getCurrentUser();
        List<Order> orders;

        if (username == null) {
            statusLabel.setText("No user logged in.");
            return;
        }

        boolean isAdmin = "admin".equals(username);
        if (isAdmin) {
            orders = orderService.getAllOrders();
            statusLabel.setText("Displaying all orders (Admin).");
        } else {
            orders = orderService.getOrdersForUser(username);
            statusLabel.setText("Displaying your orders.");
        }

        if (orders.isEmpty()) {
            statusLabel.setText("No orders found.");
            return;
        }

        ObservableList<Order> orderList = FXCollections.observableArrayList(orders);
        ordersTable.setItems(orderList);

        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setVisible(isAdmin);
        dateCol.setCellValueFactory(new PropertyValueFactory<>("orderDatetime"));
        itemsCol.setCellValueFactory(new PropertyValueFactory<>("items"));
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
    }


    @FXML private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Orders Export File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(ordersTable.getScene().getWindow());

        if (file == null) {
            statusLabel.setText("Export cancelled.");
            return;
        }

        String username = UserSession.getCurrentUser();
        List<Order> orders = "admin".equals(username)
                ? orderService.getAllOrders()
                : orderService.getOrdersForUser(username);

        if (orders.isEmpty()) {
            statusLabel.setText("No orders to export.");
            return;
        }

        Result<String> result = orderService.exportOrders(orders, file.getAbsolutePath());
        statusLabel.setText(result.getMessage());
    }

    @FXML
    private void handleBack() {
        NavigationManager.goBack();
    }


}
