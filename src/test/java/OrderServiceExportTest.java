import org.junit.jupiter.api.Test;
import service.OrderService;
import utils.Result;
import model.Order;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrderServiceExportTest {
    @Test
    public void exportOrdersWritesCsvWithEscaping() throws IOException {
        OrderService service = new OrderService(null, null, null);
        List<Order> orders = Arrays.asList(
                new Order("1", "john", "2024-01-01", 10.0),
                new Order("2", "jane,doe", "2024-01-02", 20.0),
                new Order("3", "\"quoted\"", "2024-01-03", 30.0)
        );
        Path file = Files.createTempFile("orders", ".csv");
        Result<String> result = service.exportOrders(orders, file.toString());
        assertTrue(result.isSuccess(), result.getMessage());
        List<String> lines = Files.readAllLines(file);
        assertEquals("Order ID,Username,DateTime,Items,Total Price", lines.get(0));
        assertEquals(4, lines.size());
        assertTrue(lines.get(1).contains("john"));
        assertTrue(lines.get(2).contains("\"jane,doe\""));
        assertTrue(lines.get(3).contains("\"\""));
        Files.deleteIfExists(file);
    }

    @Test
    public void exportOrdersFailsWithEmptyList() {
        OrderService service = new OrderService(null, null, null);
        Result<String> result = service.exportOrders(List.of(), "out.csv");
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("No orders"));
    }

    @Test
    public void exportOrdersHandlesBadPath() {
        OrderService service = new OrderService(null, null, null);
        List<Order> orders = List.of(new Order("1", "u", "2024", 1.0));
        Result<String> result = service.exportOrders(orders, ".");
        assertFalse(result.isSuccess());
    }
}