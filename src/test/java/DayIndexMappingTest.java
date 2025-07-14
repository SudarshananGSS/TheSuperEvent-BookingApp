import org.junit.jupiter.api.Test;
import service.OrderService;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;

public class DayIndexMappingTest {
    @Test
    public void shortAndLongDayNames() throws Exception {
        OrderService service = new OrderService(null, null, null);
        Method m = OrderService.class.getDeclaredMethod("getDayIndex", String.class);
        m.setAccessible(true);
        assertEquals(1, m.invoke(service, "Mon"));
        assertEquals(7, m.invoke(service, "Sun"));
        assertEquals(0, m.invoke(service, "Sunday"));
    }
}