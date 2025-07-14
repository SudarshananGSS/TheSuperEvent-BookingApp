package model;

/**
 * Item currently in a user's cart linking an event with a quantity.
 */
public class CartItem {
    private String username;
    private Event event;
    private int quantity;

    public CartItem(String username, Event event, int quantity) {
        this.username = username;
        this.event = event;
        this.quantity = quantity;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
