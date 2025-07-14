package service;

import dao.CartDao;
import dao.EventDao;
import dao.OrderDao;
import model.CartItem;
import model.Order;
import model.OrderItem;
import utils.Result;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Handles checkout processing, order persistence and export features.
 */
public class OrderService {
    private final OrderDao orderDao;
    private final EventService eventService;
    private final CartService cartService;

    /**
     * Construct the service with dependencies.
     */
    public OrderService(OrderDao orderDao, EventService eventService, CartService cartService) {
        this.orderDao = orderDao;
        this.eventService = eventService;
        this.cartService=cartService;
    }


    /**
     * Place an order for the given user after validating confirmation code and cart state.
     *
     * @param username         purchaser
     * @param confirmationCode 6 digit code entered by user
     * @return success message with order id or failure reason
     */
    public Result<String> checkout(String username, String confirmationCode) {
        if (!confirmationCode.matches("\\d{6}")) {
            return Result.failure("Invalid confirmation code.");
        }

        // Use CartService for cart validation
        Result<String> validation = cartService.validateCartBeforeCheckout(username);
        if (!validation.isSuccess()) {
            return Result.failure(validation.getMessage());
        }

        try {
            List<CartItem> cartItems = cartService.getCartItems(username);
            if (cartItems.isEmpty()) {
                return Result.failure("Your cart is empty.");
            }

            // Validate ticket availability and event dates
            for (CartItem item : cartItems) {
                // Validate event day is today or later
                String day = item.getEvent().getDay().toLowerCase();
                int todayIndex = getTodayIndex();
                int eventIndex = getDayIndex(day);
                if (eventIndex < todayIndex) {
                    return Result.failure("Event " + item.getEvent().getTitle() + " is in the past.");
                }
            }

            // Generate unique order ID
            String orderId = orderDao.getNextOrderId();
            String orderDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            double totalPrice = cartItems.stream()
                    .mapToDouble(item -> item.getQuantity() * item.getEvent().getPrice())
                    .sum();

            // Create Order and OrderItems
            Order order = new Order(orderId, username, orderDateTime, totalPrice);
            List<OrderItem> orderItems = new ArrayList<>();
            for (CartItem item : cartItems) {
                orderItems.add(new OrderItem(orderId, item.getEvent().getEventId(), item.getQuantity()));
                // Update sold tickets
                eventService.updateSoldTickets(item.getEvent().getEventId(), item.getQuantity());
            }

            // Save order
            orderDao.addOrder(order, orderItems);
            cartService.clearCart(username);

            return Result.success("Order placed successfully. Order ID: " + orderId);
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure("Checkout failed: " + e.getMessage());
        }
    }

    /** Map day name to index (Mon=1, Tue=2,..., Sun=7). */
    private int getDayIndex(String day) {
        switch (day.toLowerCase()) {
            case "mon": return 1;
            case "tue": return 2;
            case "wed": return 3;
            case "thu": return 4;
            case "fri": return 5;
            case "sat": return 6;
            case "sun": return 7;
            default: return 0;
        }
    }

    /** Index of today based on the system clock. */
    private int getTodayIndex() {
        return getDayIndex(LocalDateTime.now().getDayOfWeek().toString().substring(0, 3).toLowerCase());
    }

    /**
     * Retrieve all orders belonging to a specific user.
     */
    public List<Order> getOrdersForUser(String username) {
        try {
            List<Order> orders = orderDao.getOrdersByUser(username);
            populateItems(orders);
            return orders;
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /** Retrieve every order in the system. */
    public List<Order> getAllOrders() {
        try {
            List<Order> orders = orderDao.getAllOrders();
            populateItems(orders);
            return orders;
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Load order items for each order and build a summary string of events and quantities.
     */
    private void populateItems(List<Order> orders) throws SQLException {
        for (Order order : orders) {
            List<OrderItem> items = orderDao.getOrderItems(order.getOrderId());
            StringBuilder sb = new StringBuilder();
            for (OrderItem item : items) {
                if (sb.length() > 0) sb.append("; ");
                sb.append(item.getEventTitle()).append(" x ").append(item.getQuantity());
            }
            order.setItems(sb.toString());
        }
    }

    /**
     * Export provided orders to CSV at the specified path.
     */
    public Result<String> exportOrders(List<Order> orders, String filepath) {
        if (orders == null || orders.isEmpty()) {
            return Result.failure("No orders to export.");
        }

        try (PrintWriter writer = new PrintWriter(filepath)) {
            // Write CSV header matching the order table view
            writer.println("Order ID,Username,DateTime,Items,Total Price");

            for (Order order : orders) {
                writer.printf("%s,%s,%s,%s,%.2f%n",
                        escapeCsv(order.getOrderId()),
                        escapeCsv(order.getUsername()),
                        escapeCsv(order.getOrderDatetime()),
                        escapeCsv(order.getItems()),
                        order.getTotalPrice());
            }

            return Result.success("Orders exported to " + filepath);
        } catch (IOException e) {
            return Result.failure("Error writing export file: " + e.getMessage());
        }
    }

    /** Helper to escape CSV fields. */
    private String escapeCsv(String field) {
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            field = field.replace("\"", "\"\"");
            return "\"" + field + "\"";
        }
        return field;
    }
}
