package model;

/**
 * Represents a completed purchase containing order metadata.
 */
public class Order {
    private String orderId;
    private String username;
    private String orderDatetime;  // ISO format
    // Description of events and seat counts in this order
    private String items;
    private double totalPrice;

    public Order() {}

    public Order(String orderId, String username, String orderDatetime, double totalPrice) {
        this(orderId, username, orderDatetime, "", totalPrice);
    }

    public Order(String orderId, String username, String orderDatetime, String items, double totalPrice) {
        this.orderId = orderId;
        this.username = username;
        this.orderDatetime = orderDatetime;
        this.items = items;
        this.totalPrice = totalPrice;
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOrderDatetime() {
        return orderDatetime;
    }

    public void setOrderDatetime(String orderDatetime) {
        this.orderDatetime = orderDatetime;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

}
