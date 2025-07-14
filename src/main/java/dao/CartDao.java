package dao;

import model.CartItem;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO interface for persisting items in the shopping cart.
 */
public interface CartDao {

    void setup() throws SQLException;
    List<CartItem> getCartByUsername(String username) throws SQLException;
    boolean addOrUpdateCartItem(CartItem item) throws SQLException;
    boolean removeCartItem(String username, int eventId) throws SQLException;
    boolean clearCart(String username) throws SQLException;

}
