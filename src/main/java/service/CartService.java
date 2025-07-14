package service;

import dao.CartDao;
import dao.EventDao;
import model.CartItem;
import model.Event;
import utils.Result;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.util.List;

/**
 * Business logic for managing the shopping cart and validating items.
 */
public class CartService {
    private final CartDao cartDao;
    private final EventService eventService;

    /**
     * Constructs the cart service.
     *
     * @param cartDao      DAO for cart persistence
     * @param eventService service used for event lookups
     */
    public CartService(CartDao cartDao, EventService eventService) {
        this.cartDao = cartDao;
        this.eventService = eventService;
    }

    /**
     * Retrieve the current user's cart items.
     *
     * @param username user identifier
     * @return observable list of items for JavaFX tables
     */
    public ObservableList<CartItem> getCartItems(String username) {
        try {
            List<CartItem> items = cartDao.getCartByUsername(username);
            return FXCollections.observableArrayList(items);
        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    /**
     * Validate the cart items before proceeding to checkout.
     *
     * @param username owner of the cart
     * @return success if valid or failure with issues listed
     */
    public Result<String> validateCartBeforeCheckout(String username) {
        try {
            List<CartItem> items = cartDao.getCartByUsername(username);
            if (items.isEmpty()) {
                return Result.failure("Your cart is empty.");
            }

            StringBuilder issues = new StringBuilder();

            for (CartItem item : items) {
                Event latestEvent = eventService.getEventById(item.getEvent().getEventId());
                Event cartEvent = item.getEvent();

                String eventIssues = validateCartItem(cartEvent, latestEvent, item.getQuantity());
                if (!eventIssues.isEmpty()) {
                    issues.append(eventIssues).append("\n");
                }
            }

            if (issues.length() > 0) {
                return Result.failure(issues.toString());
            }

            return Result.success("Cart is valid.");
        } catch (SQLException e) {
            return Result.failure("Validation error: " + e.getMessage());
        }
    }

    /**
     * Validate a single cart item against the latest event information.
     */
    private String validateCartItem(Event cartEvent, Event latestEvent, int requestedQuantity) {
        StringBuilder issues = new StringBuilder();

        if (latestEvent == null || !latestEvent.isActive()) {
            issues.append("Event ").append(cartEvent.getTitle()).append(" is no longer available.");
            return issues.toString();
        }

        if (requestedQuantity > latestEvent.getAvailableTickets()) {
            issues.append("Event ").append(latestEvent.getTitle())
                    .append(": Requested ").append(requestedQuantity)
                    .append(", Available ").append(latestEvent.getAvailableTickets()).append(".");
        }

        if (!cartEvent.getTitle().equals(latestEvent.getTitle()) ||
                !cartEvent.getVenue().equals(latestEvent.getVenue()) ||
                !cartEvent.getDay().equals(latestEvent.getDay()) ||
                Math.abs(cartEvent.getPrice() - latestEvent.getPrice()) > 0.01) {
            issues.append("Event ").append(cartEvent.getTitle()).append(" has changed:\n");
            appendFieldChange(issues, "Title", cartEvent.getTitle(), latestEvent.getTitle());
            appendFieldChange(issues, "Venue", cartEvent.getVenue(), latestEvent.getVenue());
            appendFieldChange(issues, "Day", cartEvent.getDay(), latestEvent.getDay());
            appendFieldChange(issues, "Price", "$" + cartEvent.getPrice(), "$" + latestEvent.getPrice());
        }

        return issues.toString();
    }

    /** Helper to append formatted field change text. */
    private void appendFieldChange(StringBuilder issues, String fieldName, String oldVal, String newVal) {
        if (!oldVal.equals(newVal)) {
            issues.append("  ").append(fieldName).append(": ").append(oldVal).append(" -> ").append(newVal).append("\n");
        }
    }

    /**
     * Add a new cart entry or update an existing one with the provided quantity.
     */
    public Result<String> addOrUpdateCartItem(String username, int eventId, int quantity) {
        try {
            Event event = eventService.getEventById(eventId);
            if (event == null) {
                return Result.failure("Event not found.");
            }
            int availableTickets = event.getTotalTickets() - event.getSoldTickets();
            if (quantity > availableTickets) {
                return Result.failure("Only " + availableTickets + " tickets available.");
            }

            CartItem item = new CartItem(username, event, quantity);
            boolean success = cartDao.addOrUpdateCartItem(item);
            return success ? Result.success("Item added/updated successfully.")
                    : Result.failure("Failed to add/update item.");
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure("Error: " + e.getMessage());
        }
    }

    /** Remove an item from the cart. */
    public Result<String> removeCartItem(String username, int eventId) {
        try {
            boolean success = cartDao.removeCartItem(username, eventId);
            return success ? Result.success("Item removed.") : Result.failure("Failed to remove item.");
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure("Error: " + e.getMessage());
        }
    }

    /**
     * Update the quantity for a cart item after validating availability.
     */
    public Result<String> updateCartItem(CartItem item) {
        try {
            Event event = eventService.getEventById(item.getEvent().getEventId());
            if (event == null || !event.isActive()) {
                return Result.failure("Event is no longer available.");
            }
            if (item.getQuantity() <= 0) {
                return Result.failure("Quantity must be greater than 0.");
            }
            if (item.getQuantity() > event.getAvailableTickets()) {
                return Result.failure("Only " + event.getAvailableTickets() + " tickets available.");
            }

            boolean success = cartDao.addOrUpdateCartItem(item);
            return success ? Result.success("Quantity updated.") : Result.failure("Failed to update cart item.");
        } catch (SQLException e) {
            return Result.failure("Error updating cart: " + e.getMessage());
        }
    }

    /** Clear the user's cart after checkout. */
    public Result<String> clearCart(String username) {
        try {
            boolean success = cartDao.clearCart(username);
            return success ? Result.success("Cart cleared.") : Result.failure("Failed to clear cart.");
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure("Error: " + e.getMessage());
        }
    }
}
