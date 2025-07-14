package dao;

/**
 * Factory providing singleton access to DAO implementations.
 */
public class DaoFactory {

    private static DaoFactory instance;

    // Singleton instance
    private DaoFactory() {}

    public static DaoFactory getInstance() {
        if (instance == null) {
            instance = new DaoFactory();
        }
        return instance;
    }

    public UserDao getUserDao() {
        return new UserDaoImpl();  // Replace with InMemoryUserDao() for testing
    }

    public EventDao getEventDao() {
        return new EventDaoImpl(); // Replace with InMemoryEventDao() for testing
    }

    public OrderDao getOrderDao() {
        return new OrderDaoImpl();
    }

    public CartDao getCartDao() {
        return new CartDaoImpl();
    }
}
