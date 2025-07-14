package service;

import dao.UserDao;
import model.User;
import utils.PasswordUtil;
import utils.Result;

import java.sql.SQLException;

/**
 * Service dealing with authentication and profile management.
 */
public class UserService {
    private final UserDao userDao;

    /**
     * Construct the service using the provided DAO implementation.
     */
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Authenticate a user based on credentials.
     *
     * @param username login name
     * @param password plain text password
     * @return success containing the {@link User} or failure reason
     */
    public Result<User> login(String username, String password) {
        try {
            User user = userDao.getUser(username);
            if (user == null) {
                return Result.failure("User not found.");
            }
            boolean valid = PasswordUtil.validate(password, user.getPassword());
            if (!valid) {
                return Result.failure("Invalid password.");
            }
            return Result.success(user);
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure("Login failed: " + e.getMessage());
        }
    }

    /**
     * Register a new user in the system.
     */
    public Result<String> signup(String username, String password, String preferredName) {
        try {
            if (userDao.getUser(username) != null) {
                return Result.failure("Username already exists.");
            }
            String encrypted = PasswordUtil.encrypt(password);
            User user = userDao.createUser(username, encrypted, preferredName);
            return user!= null ? Result.success("Registration successful.") : Result.failure("Signup failed.");
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure("Signup failed: " + e.getMessage());
        }
    }

    /** Validate an existing user's password. */
    public Result<Boolean> validateUserPassword(String username, String password) {
        try {
            User user = userDao.getUser(username);
            if (user != null) {
                boolean isValid = PasswordUtil.validate(password, user.getPassword());
                return Result.success(isValid);
            } else {
                return Result.failure("User not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure("Error validating password: " + e.getMessage());
        }
    }

    /** Update a user's preferred display name. */
    public Result<String> updateUserPreferredName(String username, String newPreferredName) {
        try {
            boolean success = userDao.updatePreferredName(username, newPreferredName);
            return success ? Result.success("Preferred name updated.") : Result.failure("Failed to update preferred name.");
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure("Error updating preferred name: " + e.getMessage());
        }
    }

    /** Retrieve a user record by username. */
    public Result<User> getUserByUsername(String username) {
        try {
            User user = userDao.getUser(username);
            if (user != null) {
                return Result.success(user);
            } else {
                return Result.failure("User not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure("Error retrieving user: " + e.getMessage());
        }
    }


    /**
     * Change the stored password for a user.
     */
    public Result<String> changePassword(String username, String newPassword) {
        try {
            String encrypted = PasswordUtil.encrypt(newPassword);
            boolean updated = userDao.updatePassword(username, encrypted);
            return updated ? Result.success("Password updated.") : Result.failure("Failed to update password.");
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure("Update failed: " + e.getMessage());
        }
    }
}
