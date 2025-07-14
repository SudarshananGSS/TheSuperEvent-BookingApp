package dao;

import model.Order;
import model.OrderItem;

import java.sql.SQLException;
import java.util.List;

/**
 * DAO interface for storing orders and order items.
 */
public interface OrderDao {
    /** Create tables for orders and order items if needed. */
    void setup() throws SQLException;

    /** Persist a new order and its items. */
    boolean addOrder(Order order, List<OrderItem> items) throws SQLException;

    /** Generate the next sequential order id. */
    String getNextOrderId() throws SQLException;

    /** Retrieve orders for the specified user. */
    List<Order> getOrdersByUser(String username) throws SQLException;

    /** Retrieve all orders in the database. */
    List<Order> getAllOrders() throws SQLException;

    /** Retrieve line items for a specific order including event titles. */
    List<OrderItem> getOrderItems(String orderId) throws SQLException;
}
