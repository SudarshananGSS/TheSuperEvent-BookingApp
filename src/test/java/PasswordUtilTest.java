import org.junit.jupiter.api.Test;
import utils.PasswordUtil;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilTest {
    @Test
    public void encryptProducesDeterministicHash() {
        String hash1 = PasswordUtil.encrypt("secret");
        String hash2 = PasswordUtil.encrypt("secret");
        assertEquals(hash1, hash2);
        assertNotEquals("secret", hash1);
        assertEquals(64, hash1.length());
    }

    @Test
    public void validateMatchesEncryptedPassword() {
        String password = "mypassword";
        String hash = PasswordUtil.encrypt(password);
        assertTrue(PasswordUtil.validate(password, hash));
        assertFalse(PasswordUtil.validate("wrong", hash));
    }

    @Test
    public void emptyPasswordStillHashes() {
        String hash = PasswordUtil.encrypt("");
        assertEquals(64, hash.length());
        assertTrue(PasswordUtil.validate("", hash));
    }
}