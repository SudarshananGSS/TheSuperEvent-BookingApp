package dao;

import model.Event;
import model.EventGroup;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SQLite implementation of {@link EventDao}.
 */
public class EventDaoImpl implements EventDao {
    private final String TABLE_NAME = "events";

    public EventDaoImpl() {
    }

    /** {@inheritDoc} */
    @Override
    public void setup() throws SQLException {
        Connection connection = Database.getInstance().getConnection();
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + "event_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "title TEXT NOT NULL, "
                + "venue TEXT NOT NULL, "
                + "day TEXT NOT NULL, "
                + "price REAL NOT NULL, "
                + "total_tickets INTEGER NOT NULL, "
                + "sold_tickets INTEGER NOT NULL DEFAULT 0, "  // Added column
                + "active BOOLEAN NOT NULL DEFAULT 1"
                + ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        }
        disableExpiredEvents();
    }

    /** {@inheritDoc} */
    @Override
    public boolean addEvent(Event event) throws SQLException {
        Connection connection = Database.getInstance().getConnection();
        String sql = "INSERT INTO " + TABLE_NAME + " (title, venue, day, price, total_tickets, sold_tickets, active) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getVenue());
            ps.setString(3, event.getDay());
            ps.setDouble(4, event.getPrice());
            ps.setInt(5, event.getTotalTickets());
            ps.setInt(6, event.getSoldTickets());  // Added
            ps.setBoolean(7, event.isActive());
            return ps.executeUpdate() > 0;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean updateEvent(Event event) throws SQLException {
        Connection connection = Database.getInstance().getConnection();
        String sql = "UPDATE " + TABLE_NAME + " SET title = ?, venue = ?, day = ?, price = ?, total_tickets = ?, sold_tickets = ?, active = ? WHERE event_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getVenue());
            ps.setString(3, event.getDay());
            ps.setDouble(4, event.getPrice());
            ps.setInt(5, event.getTotalTickets());
            ps.setInt(6, event.getSoldTickets());  // Added
            ps.setBoolean(7, event.isActive());
            ps.setInt(8, event.getEventId());
            return ps.executeUpdate() > 0;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean deleteEvent(int eventId) throws SQLException {
        Connection connection = Database.getInstance().getConnection();
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE event_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            return ps.executeUpdate() > 0;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Event getEventById(int eventId) throws SQLException {
        Connection connection = Database.getInstance().getConnection();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE event_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEvent(rs);
                }
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public List<Event> getAllEvents() throws SQLException {
        Connection connection = Database.getInstance().getConnection();
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY day";
        List<Event> events = new ArrayList<>();
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                events.add(mapEvent(rs));
            }
        }
        return events;
    }

    /** {@inheritDoc} */
    @Override
    public List<Event> getAllActiveEvents() throws SQLException {
        Connection connection = Database.getInstance().getConnection();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE active = 1 ORDER BY day";
        List<Event> activeEvents = new ArrayList<>();
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                activeEvents.add(mapEvent(rs));
            }
        }
        return activeEvents;
    }

    /** {@inheritDoc} */
    @Override
    public boolean disableEvent(int eventId) throws SQLException {
        Connection connection = Database.getInstance().getConnection();
        String sql = "UPDATE " + TABLE_NAME + " SET active = 0 WHERE event_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            return ps.executeUpdate() > 0;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean enableEvent(int eventId) throws SQLException {
        Connection connection = Database.getInstance().getConnection();
        String sql = "UPDATE " + TABLE_NAME + " SET active = 1 WHERE event_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            return ps.executeUpdate() > 0;
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<EventGroup> getGroupedEvents() throws SQLException {
        Connection connection = Database.getInstance().getConnection();
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY title, venue, day";
        Map<String, EventGroup> groupedMap = new LinkedHashMap<>();

        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String title = rs.getString("title");
                String venue = rs.getString("venue");
                String day = rs.getString("day");
                EventGroup group = groupedMap.computeIfAbsent(title, k -> new EventGroup(title));
                group.addOption(venue, day);
            }
        }

        return new ArrayList<>(groupedMap.values());
    }

    /** {@inheritDoc} */
    @Override
    public Event getEventByTitleVenueDay(String title, String venue, String day) throws SQLException {
        Connection connection = Database.getInstance().getConnection();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE title = ? AND venue = ? AND day = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, venue);
            ps.setString(3, day);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEvent(rs);
                }
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void disableExpiredEvents() throws SQLException {
        int todayIndex = getTodayIndex();
        String sql = "UPDATE " + TABLE_NAME + " SET active = 0 " +
                "WHERE active = 1 AND (" +
                "CASE lower(day) " +
                "WHEN 'mon' THEN 1 WHEN 'tue' THEN 2 WHEN 'wed' THEN 3 " +
                "WHEN 'thu' THEN 4 WHEN 'fri' THEN 5 WHEN 'sat' THEN 6 " +
                "WHEN 'sun' THEN 7 END) < ?";

        Connection connection = Database.getInstance().getConnection();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, todayIndex);
            int affectedRows = ps.executeUpdate();
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
        String shortName = java.time.LocalDate.now().getDayOfWeek().toString().substring(0, 3).toLowerCase();
        return getDayIndex(shortName);
    }

    private Event mapEvent(ResultSet rs) throws SQLException {
        return new Event(
                rs.getInt("event_id"),
                rs.getString("title"),
                rs.getString("venue"),
                rs.getString("day"),
                rs.getDouble("price"),
                rs.getInt("total_tickets"),
                rs.getInt("sold_tickets"),  // Mapping for sold_tickets
                rs.getBoolean("active")
        );
    }
}
