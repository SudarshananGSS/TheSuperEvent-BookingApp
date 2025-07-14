package dao;

import model.Order;
import model.OrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite-backed implementation of {@link OrderDao}.
 */
public class OrderDaoImpl implements OrderDao {
    private final String ORDER_TABLE = "orders";
    private final String ITEM_TABLE = "order_items";

    public OrderDaoImpl() {}

    /** {@inheritDoc} */
    @Override
    public void setup() throws SQLException {
        Connection connection = Database.getInstance().getConnection();

        String orderSql = "CREATE TABLE IF NOT EXISTS " + ORDER_TABLE + " (" +
                "order_id TEXT PRIMARY KEY," +
                "username TEXT NOT NULL," +
                "order_datetime TEXT NOT NULL," +
                "total_price REAL NOT NULL," +
                "FOREIGN KEY(username) REFERENCES users(username))";

        String itemSql = "CREATE TABLE IF NOT EXISTS " + ITEM_TABLE + " (" +
                "order_id TEXT NOT NULL," +
                "event_id INTEGER NOT NULL," +
                "quantity INTEGER NOT NULL," +
                "PRIMARY KEY(order_id, event_id)," +
                "FOREIGN KEY(order_id) REFERENCES orders(order_id)," +
                "FOREIGN KEY(event_id) REFERENCES events(event_id))";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(orderSql);
            stmt.executeUpdate(itemSql);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean addOrder(Order order, List<OrderItem> items) throws SQLException {
        Connection connection = Database.getInstance().getConnection();

        String orderSql = "INSERT INTO " + ORDER_TABLE + " (order_id, username, order_datetime, total_price) VALUES (?, ?, ?, ?)";
        String itemSql = "INSERT INTO " + ITEM_TABLE + " (order_id, event_id, quantity) VALUES (?, ?, ?)";

        connection.setAutoCommit(false);
        try (PreparedStatement psOrder = connection.prepareStatement(orderSql);
             PreparedStatement psItem = connection.prepareStatement(itemSql)) {

            psOrder.setString(1, order.getOrderId());
            psOrder.setString(2, order.getUsername());
            psOrder.setString(3, order.getOrderDatetime());
            psOrder.setDouble(4, order.getTotalPrice());
            psOrder.executeUpdate();

            for (OrderItem item : items) {
                psItem.setString(1, order.getOrderId());
                psItem.setInt(2, item.getEventId());
                psItem.setInt(3, item.getQuantity());
                psItem.addBatch();
            }

            psItem.executeBatch();
            connection.commit();
            return true;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true); // Reset auto-commit after transaction
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getNextOrderId() throws SQLException {
        Connection connection = Database.getInstance().getConnection();
        String sql = "SELECT MAX(order_id) FROM " + ORDER_TABLE;
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                String lastId = rs.getString(1);
                int nextId = (lastId == null) ? 1 : Integer.parseInt(lastId) + 1;
                return String.format("%04d", nextId);
            }
        }
        return "0001";
    }

    /** {@inheritDoc} */
    @Override
    public List<Order> getOrdersByUser(String username) throws SQLException {
        return getOrders("SELECT * FROM " + ORDER_TABLE + " WHERE username = ? ORDER BY order_datetime DESC", username);
    }

    /** {@inheritDoc} */
    @Override
    public List<Order> getAllOrders() throws SQLException {
        return getOrders("SELECT * FROM " + ORDER_TABLE + " ORDER BY order_datetime DESC", null);
    }

    private List<Order> getOrders(String sql, String username) throws SQLException {
        List<Order> orders = new ArrayList<>();
        Connection connection = Database.getInstance().getConnection();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (username != null) {
                ps.setString(1, username);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(new Order(
                            rs.getString("order_id"),
                            rs.getString("username"),
                            rs.getString("order_datetime"),
                            rs.getDouble("total_price")
                    ));
                }
            }
        }
        return orders;
    }

    /** {@inheritDoc} */
    @Override
    public List<OrderItem> getOrderItems(String orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        Connection connection = Database.getInstance().getConnection();
        String sql = "SELECT oi.order_id, oi.event_id, oi.quantity, e.title " +
                "FROM " + ITEM_TABLE + " oi JOIN events e ON oi.event_id = e.event_id " +
                "WHERE oi.order_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem(
                            rs.getString("order_id"),
                            rs.getInt("event_id"),
                            rs.getInt("quantity"),
                            rs.getString("title")
                    );
                    items.add(item);
                }
            }
        }
        return items;
    }
}
