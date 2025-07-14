package dao;

import model.Event;
import model.EventGroup;

import java.sql.SQLException;
import java.util.List;

/**
 * DAO interface for CRUD operations on events.
 */
public interface EventDao {
    /** Create the events table if it does not exist. */
    void setup() throws SQLException;
    /** Insert a new event record. */
    boolean addEvent(Event event) throws SQLException;
    /** Retrieve only events currently marked as active. */
    List<Event> getAllActiveEvents() throws SQLException;
    /** Retrieve all events regardless of status. */
    List<Event> getAllEvents() throws SQLException;
    /** Update an existing event. */
    boolean updateEvent(Event event) throws SQLException;
    /** Set an event's active flag to false. */
    boolean disableEvent(int eventId) throws SQLException;
    /** Set an event's active flag to true. */
    boolean enableEvent(int eventId) throws SQLException;
    /** Group events by title for admin display.*/
    List<EventGroup> getGroupedEvents() throws SQLException;
    /** Delete an event. */
    boolean deleteEvent(int eventId) throws SQLException;
    /** Find an event by its primary key. */
    Event getEventById(int eventId) throws SQLException;
    /** Find an event by title/venue/day combination. */
    Event getEventByTitleVenueDay(String title, String venue, String day) throws SQLException;
    /** Mark events whose day is in the past as inactive. */
    void disableExpiredEvents() throws SQLException;
}
