package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import model.User;
import service.UserService;
import utils.NavigationManager;
import utils.Result;
import utils.UserSession;

/**
 * Controller for user login screen handling credential verification.
 */
public class LoginController{
	@FXML private TextField name;
	@FXML private PasswordField password;
	@FXML private Label message;
	@FXML private Button login;
	@FXML private Button signup;


	private UserService userService;

	public LoginController() {}  // Required for FXML

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@FXML
	private void initialize() {
		name.requestFocus();
		login.setOnAction(event -> handleLogin());
		signup.setOnAction(event -> NavigationManager.loadScene("/view/SignupView.fxml", "Sign Up"));
	}

	private void handleLogin() {
		String username = name.getText().trim();
		String pw = password.getText();

		if (username.isEmpty() || pw.isEmpty()) {
			showMessage("Empty username or password", Color.RED);
			return;
		}

		try {
			Result<User> result = userService.login(username, pw);

			if (result.isSuccess()) {
				User user = result.getData();
				UserSession.setCurrentUser(user.getUsername());

				if ("admin".equals(user.getUsername())) {
					NavigationManager.loadScene("/view/AdminView1.fxml", "Admin Dashboard");
				} else {
					NavigationManager.loadScene("/view/HomeView1.fxml", "HomeView");
				}
			} else {
				showMessage(result.getMessage(), Color.RED);
			}
		} catch (Exception e) {
			showMessage("Error: " + e.getMessage(), Color.RED);
			e.printStackTrace();
		}

		name.clear();
		password.clear();
	}

	private void showMessage(String text, Color color) {
		message.getStyleClass().clear();
		message.getStyleClass().add(color == Color.RED ? "error-label" : "success-label");
		message.setText(text);
	}



}
