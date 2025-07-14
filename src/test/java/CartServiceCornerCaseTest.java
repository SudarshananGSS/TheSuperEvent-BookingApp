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

public class CartServiceCornerCaseTest {
    private static class StubCartDao implements CartDao {
        List<CartItem> items;
        StubCartDao(List<CartItem> items){this.items=items;}
        @Override public void setup(){}
        @Override public List<CartItem> getCartByUsername(String username) throws SQLException {return items;}
        @Override public boolean addOrUpdateCartItem(CartItem item){return true;}
        @Override public boolean removeCartItem(String username,int eventId){return false;}
        @Override public boolean clearCart(String username){return true;}
    }
    private static class ThrowingCartDao extends StubCartDao {
        ThrowingCartDao(){super(List.of());}
        @Override public List<CartItem> getCartByUsername(String u) throws SQLException { throw new SQLException("db"); }
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
    public void emptyCartFailsValidation() {
        CartService service = new CartService(new StubCartDao(List.of()), new EventService(new StubEventDao()));
        Result<String> r = service.validateCartBeforeCheckout("u");
        assertFalse(r.isSuccess());
        assertTrue(r.getMessage().contains("empty"));
    }

    @Test
    public void quantityExceededFailsValidation() {
        Event e = new Event(1,"A","V","Mon",1.0,5,0,true);
        StubEventDao dao = new StubEventDao();
        dao.addEvent(e);
        List<CartItem> items = List.of(new CartItem("u", e, 10));
        CartService service = new CartService(new StubCartDao(items), new EventService(dao));
        Result<String> r = service.validateCartBeforeCheckout("u");
        assertFalse(r.isSuccess());
        assertTrue(r.getMessage().contains("Requested"));
    }

    @Test
    public void inactiveEventFailsValidation() {
        Event e = new Event(1,"A","V","Mon",1.0,5,0,false);
        StubEventDao dao = new StubEventDao();
        dao.addEvent(e);
        List<CartItem> items = List.of(new CartItem("u", e, 1));
        CartService service = new CartService(new StubCartDao(items), new EventService(dao));
        Result<String> r = service.validateCartBeforeCheckout("u");
        assertFalse(r.isSuccess());
        assertTrue(r.getMessage().contains("no longer"));
    }

    @Test
    public void sqlExceptionReturnsFailure() {
        EventService es = new EventService(new StubEventDao());
        CartService service = new CartService(new ThrowingCartDao(), es);
        Result<String> r = service.validateCartBeforeCheckout("u");
        assertFalse(r.isSuccess());
        assertTrue(r.getMessage().contains("Validation error"));
    }

    @Test
    public void validCartSucceeds() {
        Event e = new Event(1,"A","V","Mon",1.0,5,0,true);
        StubEventDao dao = new StubEventDao();
        dao.addEvent(e);
        List<CartItem> items = List.of(new CartItem("u", e, 1));
        CartService service = new CartService(new StubCartDao(items), new EventService(dao));
        Result<String> r = service.validateCartBeforeCheckout("u");
        assertTrue(r.isSuccess());
    }
}