import dao.CartDao;
import dao.EventDao;
import dao.OrderDao;
import model.CartItem;
import model.Event;
import model.EventGroup;
import model.Order;
import model.OrderItem;
import org.junit.jupiter.api.Test;
import service.CartService;
import service.EventService;
import service.OrderService;
import utils.Result;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class OrderServiceCheckoutTest {
    private static class StubOrderDao implements OrderDao {
        boolean added=false; String next="1";
        @Override public void setup(){}
        @Override public boolean addOrder(Order order,List<OrderItem> items){added=true;return true;}
        @Override public String getNextOrderId(){return next;}
        @Override public List<Order> getOrdersByUser(String u){return List.of();}
        @Override public List<Order> getAllOrders(){return List.of();}
        @Override public List<OrderItem> getOrderItems(String id){return List.of();}
    }
    private static class SimpleCartDao implements CartDao {
        List<CartItem> items;
        SimpleCartDao(List<CartItem> items){this.items=items;}
        @Override public void setup(){}
        @Override public List<CartItem> getCartByUsername(String u){return items;}
        @Override public boolean addOrUpdateCartItem(CartItem i){return true;}
        @Override public boolean removeCartItem(String u,int id){return true;}
        @Override public boolean clearCart(String u){return true;}
    }
    private static class SimpleEventDao implements EventDao {
        Event event;
        SimpleEventDao(Event event){this.event=event;}
        @Override public void setup(){}
        @Override public boolean addEvent(Event e){return true;}
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

    private String previousDay() {
        String[] days={"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
        int idx=LocalDateTime.now().getDayOfWeek().getValue();
        return days[(idx+5)%7];
    }

    @Test
    public void invalidCodeFailsCheckout() {
        Event event=new Event(1,"T","V","Mon",1.0,5,0,true);
        OrderService service=new OrderService(new StubOrderDao(), new EventService(new SimpleEventDao(event)),
                new CartService(new SimpleCartDao(List.of(new CartItem("u",event,1))), new EventService(new SimpleEventDao(event))));
        Result<String> r=service.checkout("u","abc");
        assertFalse(r.isSuccess());
        assertTrue(r.getMessage().contains("Invalid"));
    }

    @Test
    public void pastEventPreventsCheckout() {
        String past=previousDay();
        Event event=new Event(1,"T","V",past,1.0,5,0,true);
        SimpleEventDao ed=new SimpleEventDao(event);
        CartService cart=new CartService(new SimpleCartDao(List.of(new CartItem("u",event,1))), new EventService(ed));
        OrderService service=new OrderService(new StubOrderDao(), new EventService(ed), cart);
        Result<String> r=service.checkout("u","123456");
        assertFalse(r.isSuccess());
        assertTrue(r.getMessage().contains("past"));
    }

    @Test
    public void successfulCheckoutClearsCart() {
        Event event=new Event(1,"T","V","Sun",1.0,5,0,true);
        SimpleEventDao ed=new SimpleEventDao(event);
        SimpleCartDao cd=new SimpleCartDao(List.of(new CartItem("u",event,1)));
        CartService cart=new CartService(cd, new EventService(ed));
        StubOrderDao od=new StubOrderDao();
        OrderService service=new OrderService(od, new EventService(ed), cart);
        Result<String> r=service.checkout("u","123456");
        assertTrue(r.isSuccess());
        assertTrue(od.added);
    }

    @Test
    public void emptyCartFailsCheckout() {
        Event event=new Event(1,"T","V","Sun",1.0,5,0,true);
        SimpleEventDao ed=new SimpleEventDao(event);
        CartService cart=new CartService(new SimpleCartDao(List.of()), new EventService(ed));
        OrderService service=new OrderService(new StubOrderDao(), new EventService(ed), cart);
        Result<String> r=service.checkout("u","123456");
        assertFalse(r.isSuccess());
        assertTrue(r.getMessage().contains("empty"));
    }
}