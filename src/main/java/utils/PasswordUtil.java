package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Provides simple SHA-256 based password hashing and validation.
 */
public class PasswordUtil {

    /**
     * Hash a password using SHA-256.
     */
    public static String encrypt(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());

            // Convert bytes to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }

    /**
     * Compare a plain text password against a stored hash.
     */
    public static boolean validate(String password, String storedHash) {
        String hashedInput = encrypt(password);
        return hashedInput.equals(storedHash);
    }
}