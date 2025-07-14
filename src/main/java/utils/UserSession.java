package utils;

/**
 * Stores the username of the currently logged-in user.
 */
public class UserSession {
    private static String currentUser = null;

    public static void setCurrentUser(String username) {
        currentUser = username;
    }

    public static String getCurrentUser() {
        return currentUser;
    }

    public static void clear() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
