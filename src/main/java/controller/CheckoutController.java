package controller;

import config.AppConfig;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import service.OrderService;
import utils.NavigationManager;
import utils.Result;
import utils.UserSession;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Controller handling the final checkout process including confirmation code validation.
 */
public class CheckoutController{
    @FXML private TextField confirmationCodeField;
    @FXML private Label statusLabel;
    @FXML private Button confirmButton;

    private OrderService orderService;

    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @FXML public void initialize() {
        // Allow only digits up to 6 characters
        confirmationCodeField.textProperty().addListener((obs, old, text) -> {
            if (!text.matches("\\d{0,6}")) {
                confirmationCodeField.setText(old);
            }
        });
        Image check = new Image(getClass().getResourceAsStream("/icons/check.png"));
        ImageView checkView = new ImageView(check);
        checkView.setFitWidth(AppConfig.ICON_SIZE);
        checkView.setFitHeight(AppConfig.ICON_SIZE);
        confirmButton.setGraphic(checkView);
    }

    @FXML
    private void handleBack() {
        NavigationManager.goBack();
    }

    @FXML
    private void handleConfirm() {
        String username = UserSession.getCurrentUser();
        if (username == null) {
            statusLabel.setText("No user logged in.");
            return;
        }

        String code = confirmationCodeField.getText().trim();

        Result<String> result = orderService.checkout(username, code);

        statusLabel.getStyleClass().clear();
        statusLabel.setText(result.getMessage());
        statusLabel.getStyleClass().add(result.isSuccess() ? "success-label" : "error-label");

        if (result.isSuccess()) {
            NavigationManager.loadScene("/view/HomeView1.fxml", "Home");
        }

    }
}
