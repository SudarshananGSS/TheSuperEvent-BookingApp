package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import service.UserService;
import utils.NavigationManager;
import utils.Result;

/**
 * Controller managing new user registration form.
 */
public class SignupController{
	@FXML private TextField usernameField;
	@FXML private PasswordField passwordField;
	@FXML private TextField preferredNameField;
	@FXML private Label strengthLabel;
	@FXML private Label statusLabel;
	@FXML private Button backButton;

	private UserService userService;

	public SignupController() {}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@FXML public void initialize() {
		passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal == null) newVal = "";
			boolean strong = isStrongPassword(newVal);
			strengthLabel.getStyleClass().setAll(strong ? "success-label" : "error-label");
			strengthLabel.setText(strong ? "Strong password" : "Weak password");
		});
		usernameField.requestFocus();
	}

	private boolean isStrongPassword(String pw) {
		if (pw.length() < 8) return false;
		boolean upper = pw.matches(".*[A-Z].*");
		boolean lower = pw.matches(".*[a-z].*");
		boolean digit = pw.matches(".*\\d.*");
		boolean special = pw.matches(".*[^A-Za-z0-9].*");
		return upper && lower && digit && special;
	}

	@FXML
	private void handleBack() {
		NavigationManager.goBack();
	}

	@FXML private void handleSignup() {
		String username = usernameField.getText().trim();
		String password = passwordField.getText();
		String preferredName = preferredNameField.getText().trim();

		if (username.isEmpty() || password.isEmpty() || preferredName.isEmpty()) {
			statusLabel.getStyleClass().setAll("error-label");
			statusLabel.setText("All fields are required.");
			return;
		}
		if (!isStrongPassword(password)) {
			statusLabel.getStyleClass().setAll("error-label");
			statusLabel.setText("Password is too weak.");
			return;
		}

		try {
			Result<String> result = userService.signup(username, password, preferredName);
			statusLabel.getStyleClass().clear();
			statusLabel.getStyleClass().add(result.isSuccess() ? "success-label" : "error-label");
			statusLabel.setText(result.getMessage());
			if (result.isSuccess()) {
				NavigationManager.loadScene("/view/LoginView.fxml", "Login");
			}
		} catch (Exception e) {
			statusLabel.setText("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
