package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.User;
import service.UserService;
import utils.NavigationManager;
import utils.Result;
import utils.UserSession;

/**
 * Controller for user profile allowing password changes.
 */
public class ProfileController{
    @FXML private TextField usernameField;
    @FXML private TextField preferredNameField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private Button saveButton, backButton;

    private UserService userService;
    private String username;

    public void setUserService(UserService userService) {
        this.userService = userService;
        this.username = UserSession.getCurrentUser();
        loadUserData();
    }


    private void loadUserData() {
        try {
            Result<User> result = userService.getUserByUsername(username);
            if (result.isSuccess() && result.getData() != null) {
                User user = result.getData();
                usernameField.setText(user.getUsername());
                usernameField.setEditable(false);
                preferredNameField.setText(user.getPreferredName());
                currentPasswordField.clear();
                newPasswordField.clear();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", result.getMessage() != null ? result.getMessage() : "User not found.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load user data.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSave() {
        boolean updated = false;
        String newPreferredName = preferredNameField.getText();

        if (newPreferredName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Invalid Name", "Preferred name cannot be empty.");
            return;
        }

        // Update preferred name if changed
        if (!newPreferredName.equals(getCurrentPreferredName())) {
            Result<String> result = userService.updateUserPreferredName(username, newPreferredName);
            if (result.isSuccess()) {
                updated = true;
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", result.getMessage());
                return;
            }
        }

        // Update password if new password is provided
        String newPassword = newPasswordField.getText();
        String currentPassword = currentPasswordField.getText();

        if (!newPassword.isEmpty()) {
            Result<Boolean> validation = userService.validateUserPassword(username, currentPassword);
            if (!validation.isSuccess() || !validation.getData()) {
                showAlert(Alert.AlertType.ERROR, "Invalid Password", "Current password is incorrect.");
                return;
            }

            Result<String> result = userService.changePassword(username, newPassword);
            if (result.isSuccess()) {
                updated = true;
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", result.getMessage());
                return;
            }
        }

        if (updated) {
            showAlert(Alert.AlertType.INFORMATION, "Profile Updated", "Your profile has been updated successfully.");
        } else {
            showAlert(Alert.AlertType.INFORMATION, "No Changes", "No changes were made to your profile.");
        }

        loadUserData();
    }

    private String getCurrentPreferredName() {
        try {
            Result<User> result = userService.getUserByUsername(username);
            User user = result.isSuccess() ? result.getData() : null;
            return user != null ? user.getPreferredName() : "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
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
