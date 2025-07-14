import model.Event;
import org.junit.jupiter.api.Test;
import service.EventService;
import utils.Result;
import dao.EventDao;
import model.EventGroup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EventServiceImportTest {
    private static class InMemoryEventDao implements EventDao {
        List<Event> events = new ArrayList<>();
        @Override public void setup() {}
        @Override public boolean addEvent(Event event) { events.add(event); return true; }
        @Override public List<Event> getAllActiveEvents() { return events; }
        @Override public List<Event> getAllEvents() { return events; }
        @Override public boolean updateEvent(Event event) { return true; }
        @Override public boolean disableEvent(int eventId) { return true; }
        @Override public boolean enableEvent(int eventId) { return true; }
        @Override public List<EventGroup> getGroupedEvents() { return new ArrayList<>(); }
        @Override public boolean deleteEvent(int eventId) { return true; }
        @Override public Event getEventById(int eventId) { return events.stream().filter(e->e.getEventId()==eventId).findFirst().orElse(null); }
        @Override public Event getEventByTitleVenueDay(String t,String v,String d){return events.stream().filter(e->e.getTitle().equals(t)&&e.getVenue().equals(v)&&e.getDay().equals(d)).findFirst().orElse(null);}
        @Override public void disableExpiredEvents() {}
    }

    @Test
    public void importEventsAddsEvents() throws IOException, SQLException {
        InMemoryEventDao dao = new InMemoryEventDao();
        EventService service = new EventService(dao);
        Path file = Files.createTempFile("events", ".dat");
        Files.write(file, List.of("Show;Venue;Mon;12.5;0;100"));
        service.importEventsFromFile(file.toString());
        assertEquals(1, dao.events.size());
        Event e = dao.events.get(0);
        assertEquals("Show", e.getTitle());
        assertEquals("Venue", e.getVenue());
        Files.deleteIfExists(file);
    }

    @Test
    public void importWithBadFormatThrows() throws IOException {
        InMemoryEventDao dao = new InMemoryEventDao();
        EventService service = new EventService(dao);
        Path file = Files.createTempFile("events", ".dat");
        Files.write(file, List.of("BadLine"));
        assertThrows(Exception.class, () -> service.importEventsFromFile(file.toString()));
        Files.deleteIfExists(file);
    }

    @Test
    public void importEmptyFileAddsNothing() throws IOException, SQLException {
        InMemoryEventDao dao = new InMemoryEventDao();
        EventService service = new EventService(dao);
        Path file = Files.createTempFile("events", ".dat");
        service.importEventsFromFile(file.toString());
        assertTrue(dao.events.isEmpty());
        Files.deleteIfExists(file);
    }
}