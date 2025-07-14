import model.CartItem;
import model.Event;
import org.junit.jupiter.api.Test;
import service.CartService;
import service.EventService;
import utils.Result;
import dao.CartDao;
import dao.EventDao;
import model.EventGroup;

import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CartServiceValidationTest {
    private static class StubCartDao implements CartDao {
        List<CartItem> items;
        StubCartDao(List<CartItem> items){this.items=items;}
        @Override public void setup(){}
        @Override public List<CartItem> getCartByUsername(String username){return items;}
        @Override public boolean addOrUpdateCartItem(CartItem item){return false;}
        @Override public boolean removeCartItem(String username,int eventId){return false;}
        @Override public boolean clearCart(String username){return false;}
    }
    private static class StubEventDao implements EventDao {
        Map<Integer,Event> map=new HashMap<>();
        @Override public void setup(){}
        @Override public boolean addEvent(Event e){map.put(e.getEventId(),e);return true;}
        @Override public List<Event> getAllActiveEvents(){return new ArrayList<>(map.values());}
        @Override public List<Event> getAllEvents(){return new ArrayList<>(map.values());}
        @Override public boolean updateEvent(Event e){map.put(e.getEventId(),e);return true;}
        @Override public boolean disableEvent(int id){return true;}
        @Override public boolean enableEvent(int id){return true;}
        @Override public List<EventGroup> getGroupedEvents(){return new ArrayList<>();}
        @Override public boolean deleteEvent(int id){return map.remove(id)!=null;}
        @Override public Event getEventById(int id){return map.get(id);}
        @Override public Event getEventByTitleVenueDay(String t,String v,String d){return map.values().stream().filter(e->e.getTitle().equals(t)&&e.getVenue().equals(v)&&e.getDay().equals(d)).findFirst().orElse(null);}
        @Override public void disableExpiredEvents(){}
    }

    @Test
    public void detectsChangedEventDetails() {
        Event oldEvent = new Event(1, "TitleA", "V1", "Mon", 10.0, 100, 0, true);
        Event updated = new Event(1, "TitleB", "V1", "Mon", 10.0, 100, 0, true);
        StubEventDao dao = new StubEventDao();
        dao.addEvent(updated);
        EventService eventService = new EventService(dao);
        List<CartItem> items = List.of(new CartItem("u", oldEvent, 1));
        CartService service = new CartService(new StubCartDao(items), eventService);
        Result<String> res = service.validateCartBeforeCheckout("u");
        assertFalse(res.isSuccess());
        assertTrue(res.getMessage().contains("changed"));
    }

    @Test
    public void validCartReturnsSuccess() {
        Event event = new Event(2, "Title", "V", "Tue", 5.0, 10, 0, true);
        StubEventDao dao = new StubEventDao();
        dao.addEvent(event);
        EventService eventService = new EventService(dao);
        List<CartItem> items = List.of(new CartItem("u", event, 1));
        CartService service = new CartService(new StubCartDao(items), eventService);
        Result<String> res = service.validateCartBeforeCheckout("u");
        assertTrue(res.isSuccess());
    }

    @Test
    public void quantityAtLimitSucceeds() {
        Event event = new Event(3, "Title", "V", "Wed", 5.0, 1, 0, true);
        StubEventDao dao = new StubEventDao();
        dao.addEvent(event);
        EventService eventService = new EventService(dao);
        List<CartItem> items = List.of(new CartItem("u", event, 1));
        CartService service = new CartService(new StubCartDao(items), eventService);
        Result<String> res = service.validateCartBeforeCheckout("u");
        assertTrue(res.isSuccess());
    }
}