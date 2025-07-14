package service;

import dao.EventDao;
import model.Event;
import model.EventGroup;
import utils.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides operations for querying and modifying events including import from file.
 */
public class EventService {
    private final EventDao eventDao;

    /**
     * Constructs the service with the required {@link EventDao} dependency.
     *
     * @param eventDao DAO used for all persistence operations
     */
    public EventService(EventDao eventDao) {
        this.eventDao = eventDao;
    }

    /**
     * Retrieve all events from the database regardless of active status.
     *
     * @return list of every event record
     * @throws SQLException if the query fails
     */
    public List<Event> getAllEvents() throws SQLException {
        return eventDao.getAllEvents();
    }

    /**
     * Obtain only events that are currently marked as active.
     *
     * @return active events
     * @throws SQLException if the query fails
     */
    public List<Event> getAllActiveEvents() throws SQLException {
        List<Event> events = eventDao.getAllActiveEvents();
        int todayIndex = getTodayIndex();
        return events.stream()
                .filter(e -> getDayIndex(e.getDay()) >= todayIndex)
                .collect(Collectors.toList());
    }

    /**
     * Persist a new event after checking that no duplicate title/venue/day combination exists.
     *
     * @param event event instance to add
     * @return result indicating success or the failure reason
     */
    public Result<String> addEvent(Event event) {
        try {
            Event existing = eventDao.getEventByTitleVenueDay(event.getTitle(), event.getVenue(), event.getDay());
            if (existing != null) {
                return Result.failure("Duplicate event exists.");
            }
            boolean success = eventDao.addEvent(event);
            return success ? Result.success("Event added.") : Result.failure("Failed to add event.");
        } catch (SQLException e) {
            return Result.failure("Error adding event: " + e.getMessage());
        }
    }

    /**
     * Fetch a single event by its identifier.
     *
     * @param eventId primary key
     * @return the event or {@code null}
     */
    public Event getEventById(int eventId) {
        try {
            return eventDao.getEventById(eventId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Lookup an event by title, venue and day.
     *
     * @param title event title
     * @param venue venue name
     * @param day   day of the performance
     * @return matching event or {@code null}
     */
    public Event getEventByTitleVenueDay(String title, String venue, String day) {
        try {
            return eventDao.getEventByTitleVenueDay(title, venue, day);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Update the supplied event details after performing duplicate checks.
     *
     * @param event event with modified fields
     * @return result describing the outcome
     */
    public Result<String> updateEvent(Event event) {
        try {
            Event existing = eventDao.getEventByTitleVenueDay(event.getTitle(), event.getVenue(), event.getDay());
            if (existing != null && existing.getEventId() != event.getEventId()) {
                return Result.failure("Duplicate event exists.");
            }
            boolean success = eventDao.updateEvent(event);
            return success ? Result.success("Event updated.") : Result.failure("Failed to update event.");
        } catch (SQLException e) {
            return Result.failure("Error updating event: " + e.getMessage());
        }
    }

    /**
     * Permanently remove an event from storage.
     *
     * @param eventId identifier of the event to delete
     * @return success result or failure message
     */
    public Result<String> deleteEvent(int eventId) {
        try {
            boolean success = eventDao.deleteEvent(eventId);
            return success ? Result.success("Event deleted.") : Result.failure("Failed to delete event.");
        } catch (SQLException e) {
            return Result.failure("Error deleting event: " + e.getMessage());
        }
    }

    /**
     * Mark an event as inactive so that normal users cannot see it.
     *
     * @param eventId event identifier
     * @return result indicating success or failure
     */
    public Result<String> disableEvent(int eventId) {
        try {
            boolean success = eventDao.disableEvent(eventId);
            return success ? Result.success("Event disabled.") : Result.failure("Failed to disable event.");
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure("Error disabling event: " + e.getMessage());
        }
    }

    /**
     * Reactivate a previously disabled event.
     *
     * @param eventId event identifier
     * @return result of the enable operation
     */
    public Result<String> enableEvent(int eventId) {
        try {
            boolean success = eventDao.enableEvent(eventId);
            return success ? Result.success("Event enabled.") : Result.failure("Failed to enable event.");
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.failure("Error enabling event: " + e.getMessage());
        }
    }

    /**
     * Bulk import events from a semicolon separated file.
     *
     * @param filepath path to events.dat
     * @throws IOException    if file cannot be read
     * @throws SQLException   if a persistence error occurs
     */
    public void importEventsFromFile(String filepath) throws IOException, SQLException {
        List<String> lines = Files.readAllLines(Paths.get(filepath));
        for (String line : lines) {
            String[] parts = line.split(";");
            String title = parts[0];
            String venue = parts[1];
            String day = parts[2];
            double price = Double.parseDouble(parts[3]);
            int sold = Integer.parseInt(parts[4]);
            int total = Integer.parseInt(parts[5]);
            boolean active = true;
            Event event = new Event(0, title, venue, day, price, total, sold, active);
            eventDao.addEvent(event);  // Use DAO here
        }
    }

    /**
     * Increment the sold ticket count for an event.
     *
     * @param eventId  event identifier
     * @param quantity amount sold in this transaction
     * @return operation result
     */
    public Result<String> updateSoldTickets(int eventId, int quantity) {
        try {
            Event event = eventDao.getEventById(eventId);
            if (event == null) {
                return Result.failure("Event not found.");
            }
            event.setSoldTickets(event.getSoldTickets() + quantity);
            eventDao.updateEvent(event);
            return Result.success("Sold tickets updated.");
        } catch (SQLException e) {
            return Result.failure("Failed to update sold tickets: " + e.getMessage());
        }
    }

    /**
     * Retrieve events grouped by title to assist admin views.
     *
     * @return grouped event information
     */
    public List<EventGroup> getGroupedEvents() {
        try {
            return eventDao.getGroupedEvents();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /** Map day name to index (Mon=1, Tue=2,..., Sun=7). */
    private int getDayIndex(String day) {
        switch (day.toLowerCase()) {
            case "mon": return 1;
            case "tue": return 2;
            case "wed": return 3;
            case "thu": return 4;
            case "fri": return 5;
            case "sat": return 6;
            case "sun": return 7;
            default: return 0;
        }
    }

    /** Index of today based on the system clock. */
    private int getTodayIndex() {
        DayOfWeek dow = LocalDate.now().getDayOfWeek();
        String shortName = dow.toString().substring(0, 3).toLowerCase();
        return getDayIndex(shortName);
    }
}
