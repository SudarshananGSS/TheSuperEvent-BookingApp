import org.junit.jupiter.api.Test;
import utils.UserSession;

import static org.junit.jupiter.api.Assertions.*;

public class UserSessionTest {
    @Test
    public void loginAndLogoutChangeState() {
        UserSession.setCurrentUser("john");
        assertTrue(UserSession.isLoggedIn());
        assertEquals("john", UserSession.getCurrentUser());

        UserSession.clear();
        assertFalse(UserSession.isLoggedIn());
        assertNull(UserSession.getCurrentUser());
    }

    @Test
    public void clearWithoutLoginKeepsLoggedOut() {
        UserSession.clear();
        assertFalse(UserSession.isLoggedIn());
        assertNull(UserSession.getCurrentUser());
    }

    @Test
    public void settingNullUserTreatsAsLogout() {
        UserSession.setCurrentUser(null);
        assertFalse(UserSession.isLoggedIn());
    }
}