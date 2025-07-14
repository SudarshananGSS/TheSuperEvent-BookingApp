package dao;

import model.CartItem;
import model.Event;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite-backed implementation of {@link CartDao}.
 */
public class CartDaoImpl implements CartDao {
    private final String TABLE_NAME = "cart";

    public CartDaoImpl() {
    }

    @Override
    public void setup() throws SQLException {
        Connection connection = Database.getInstance().getConnection();
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + "username TEXT NOT NULL, "
                + "event_id INTEGER NOT NULL, "
                + "quantity INTEGER NOT NULL, "
                + "PRIMARY KEY(username, event_id), "
                + "FOREIGN KEY(username) REFERENCES users(username), "
                + "FOREIGN KEY(event_id) REFERENCES events(event_id))";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    @Override
    public List<CartItem> getCartByUsername(String username) throws SQLException {
        List<CartItem> cart = new ArrayList<>();
        Connection connection = Database.getInstance().getConnection();

        String sql = "SELECT c.username, c.event_id, c.quantity, "
                + "e.title, e.venue, e.day, e.price, e.total_tickets, e.sold_tickets, e.active "
                + "FROM cart c JOIN events e ON c.event_id = e.event_id WHERE c.username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Event event = new Event(
                            rs.getInt("event_id"),
                            rs.getString("title"),
                            rs.getString("venue"),
                            rs.getString("day"),
                            rs.getDouble("price"),
                            rs.getInt("total_tickets"),
                            rs.getInt("sold_tickets"),
                            rs.getBoolean("active")
                    );
                    CartItem item = new CartItem(rs.getString("username"), event, rs.getInt("quantity"));
                    cart.add(item);
                }
            }
        }
        return cart;
    }

    @Override
    public boolean addOrUpdateCartItem(CartItem item) throws SQLException {
        Connection connection = Database.getInstance().getConnection();
        String sql = "INSERT INTO " + TABLE_NAME + " (username, event_id, quantity) VALUES (?, ?, ?) "
                + "ON CONFLICT(username, event_id) DO UPDATE SET quantity = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, item.getUsername());
            stmt.setInt(2, item.getEvent().getEventId());
            stmt.setInt(3, item.getQuantity());
            stmt.setInt(4, item.getQuantity());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean removeCartItem(String username, int eventId) throws SQLException {
        Connection connection = Database.getInstance().getConnection();
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE username = ? AND event_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setInt(2, eventId);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean clearCart(String username) throws SQLException {
        Connection connection = Database.getInstance().getConnection();
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            return stmt.executeUpdate() > 0;
        }
    }
}
