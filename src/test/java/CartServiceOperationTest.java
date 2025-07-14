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

public class CartServiceOperationTest {
    private static class StubCartDao implements CartDao {
        CartItem stored;
        boolean success = true;
        StubCartDao(){}
        @Override public void setup(){}
        @Override public List<CartItem> getCartByUsername(String username){return stored==null?List.of():List.of(stored);}
        @Override public boolean addOrUpdateCartItem(CartItem item){stored=item;return success;}
        @Override public boolean removeCartItem(String username,int eventId){return true;}
        @Override public boolean clearCart(String username){return true;}
    }
    private static class StubEventDao implements EventDao {
        Event event;
        @Override public void setup(){}
        @Override public boolean addEvent(Event e){event=e;return true;}
        @Override public List<Event> getAllActiveEvents(){return List.of(event);}
        @Override public List<Event> getAllEvents(){return List.of(event);}
        @Override public boolean updateEvent(Event e){event=e;return true;}
        @Override public boolean disableEvent(int id){return true;}
        @Override public boolean enableEvent(int id){return true;}
        @Override public List<EventGroup> getGroupedEvents(){return new ArrayList<>();}
        @Override public boolean deleteEvent(int id){return true;}
        @Override public Event getEventById(int id){return event;}
        @Override public Event getEventByTitleVenueDay(String t,String v,String d){return event!=null && event.getTitle().equals(t)&&event.getVenue().equals(v)&&event.getDay().equals(d)?event:null;}
        @Override public void disableExpiredEvents(){}
    }

    @Test
    public void addItemFailsWhenEventMissing() {
        StubCartDao cart = new StubCartDao();
        EventService es = new EventService(new StubEventDao());
        CartService service = new CartService(cart, es);
        Result<String> r = service.addOrUpdateCartItem("u", 1, 1);
        assertFalse(r.isSuccess());
    }

    @Test
    public void addItemFailsWhenQuantityTooHigh() {
        StubCartDao cart = new StubCartDao();
        StubEventDao evDao = new StubEventDao();
        evDao.event = new Event(1,"T","V","Mon",1.0,5,0,true);
        CartService service = new CartService(cart, new EventService(evDao));
        Result<String> r = service.addOrUpdateCartItem("u", 1, 10);
        assertFalse(r.isSuccess());
    }

    @Test
    public void addItemSuccess() {
        StubCartDao cart = new StubCartDao();
        StubEventDao evDao = new StubEventDao();
        evDao.event = new Event(1,"T","V","Mon",1.0,5,0,true);
        CartService service = new CartService(cart, new EventService(evDao));
        Result<String> r = service.addOrUpdateCartItem("u", 1, 2);
        assertTrue(r.isSuccess());
        assertNotNull(cart.stored);
    }

    @Test
    public void addItemAtMaxQuantity() {
        StubCartDao cart = new StubCartDao();
        StubEventDao evDao = new StubEventDao();
        evDao.event = new Event(1,"T","V","Mon",1.0,2,0,true);
        CartService service = new CartService(cart, new EventService(evDao));
        Result<String> r = service.addOrUpdateCartItem("u", 1, 2);
        assertTrue(r.isSuccess());
    }

    @Test
    public void updateItemValidatesInputs() {
        StubCartDao cart = new StubCartDao();
        StubEventDao evDao = new StubEventDao();
        evDao.event = new Event(1,"T","V","Mon",1.0,5,0,true);
        CartService service = new CartService(cart, new EventService(evDao));
        CartItem item = new CartItem("u", evDao.event, 0);
        Result<String> r = service.updateCartItem(item);
        assertFalse(r.isSuccess());

        item = new CartItem("u", evDao.event, 10);
        r = service.updateCartItem(item);
        assertFalse(r.isSuccess());

        evDao.event.setActive(false);
        item = new CartItem("u", evDao.event, 1);
        r = service.updateCartItem(item);
        assertFalse(r.isSuccess());
    }

    @Test
    public void updateItemSuccess() {
        StubCartDao cart = new StubCartDao();
        StubEventDao evDao = new StubEventDao();
        evDao.event = new Event(1,"T","V","Mon",1.0,5,0,true);
        CartService service = new CartService(cart, new EventService(evDao));
        CartItem item = new CartItem("u", evDao.event, 1);
        Result<String> r = service.updateCartItem(item);
        assertTrue(r.isSuccess());
        assertEquals(1, cart.stored.getQuantity());
    }
}