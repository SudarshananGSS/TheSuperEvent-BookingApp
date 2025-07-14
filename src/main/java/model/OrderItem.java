package model;

/**
 * Line item belonging to an Order referencing an event and quantity.
 */
public class OrderItem {
    private String orderId;
    private int eventId;
    private int quantity;
    // Title of the event for easy display when listing orders
    private String eventTitle;

    public OrderItem() {}

    public OrderItem(String orderId, int eventId, int quantity) {
        this.orderId = orderId;
        this.eventId = eventId;
        this.quantity = quantity;
    }

    public OrderItem(String orderId, int eventId, int quantity, String eventTitle) {
        this(orderId, eventId, quantity);
        this.eventTitle = eventTitle;
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }
}
