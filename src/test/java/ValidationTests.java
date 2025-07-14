import static org.junit.jupiter.api.Assertions.*;

import dao.UserDao;
import org.junit.jupiter.api.Test;
import utils.PasswordUtil;

public class ValidationTests {

    @Test
    public void testPasswordEncryption() {
        String password = "abc123";
        String encrypted = PasswordUtil.encrypt(password);
        assertNotEquals(password, encrypted);
        assertEquals(64, encrypted.length());
    }

    @Test
    public void testConfirmationCodeValidation() {
        String validCode = "123456";
        String invalidCode = "abc123";
        assertTrue(validCode.matches("\\d{6}"));
        assertFalse(invalidCode.matches("\\d{6}"));
    }

    @Test
    public void testEventAvailability() {
        int available = 10;
        int requested = 15;
        assertTrue(requested > available);
    }

    @Test
    public void emptyPasswordEncrypts() {
        String encrypted = PasswordUtil.encrypt("");
        assertEquals(64, encrypted.length());
    }

    @Test
    public void testBookingDateValidation() {
        String eventDay = "Mon";
        String today = "Wed";
        assertFalse(isBookingValid(eventDay, today));
    }

    private boolean isBookingValid(String eventDay, String today) {
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        int e = java.util.Arrays.asList(days).indexOf(eventDay);
        int t = java.util.Arrays.asList(days).indexOf(today);
        return e >= t;
    }
}
