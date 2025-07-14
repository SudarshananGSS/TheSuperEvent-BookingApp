import org.junit.jupiter.api.Test;
import utils.Result;

import static org.junit.jupiter.api.Assertions.*;

public class ResultTest {
    @Test
    public void successResultHoldsData() {
        Result<String> r = Result.success("ok");
        assertTrue(r.isSuccess());
        assertEquals("ok", r.getData());
        assertNull(r.getMessage());
    }

    @Test
    public void failureResultHasMessage() {
        Result<Integer> r = Result.failure("bad");
        assertFalse(r.isSuccess());
        assertNull(r.getData());
        assertEquals("bad", r.getMessage());
    }

    @Test
    public void successAllowsNullData() {
        Result<String> r = Result.success(null);
        assertTrue(r.isSuccess());
        assertNull(r.getData());
    }
}