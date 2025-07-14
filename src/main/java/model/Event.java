package model;

/**
 * Represents a single performance of an event including venue, day and
 * ticket counts. Instances are stored by the DAO layer and manipulated by
 * services when processing bookings.
 */

public class Event {
    private int eventId;
    private String title;
    private String venue;
    private String day;
    private double price;
    private int totalTickets;
    private int soldTickets;
    private int availableTickets;
    private boolean isActive;      // Field to support enable/disable by admin

    public Event() {}

    public Event(int eventId, String title, String venue, String day, double price, int totalTickets, int soldTickets, boolean isActive) {
        this.eventId = eventId;
        this.title = title;
        this.venue = venue;
        this.day = day;
        this.price = price;
        this.totalTickets = totalTickets;
        this.soldTickets = soldTickets;
        this.availableTickets = totalTickets - soldTickets;
        this.isActive = isActive;
    }

    public void setAvailableTickets(int availableTickets) {
        this.availableTickets = availableTickets;
    }

    public int getAvailableTickets() {
        return availableTickets;
    }

    // Getters and Setters
    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(int totalTickets) {
        this.totalTickets = totalTickets;
    }

    public int getSoldTickets() {
        return soldTickets;
    }

    public void setSoldTickets(int soldTickets) {
        this.soldTickets = soldTickets;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
}
