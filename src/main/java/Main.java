// <Sudarshanan Gujuluwa Sundaram Santharam>
// Y Signup
// Y Login
// Y Read the events from database and display
// Y Events can be booked and bookings can be modified
// Y Bookings are validated
// Y Checkout is validated with confirmation code and day information
// Y Remaining seats of events is updated once an order is made
// Y User can view all orders
// Y User can export orders to file
// Y Admin GUI & admin login implemented
// Y Admin display implemented (no duplicate event titles)
// Y Event disable function implemented
// Y Event adding & deletion functions implemented
// Y Event modification function implemented
// Y Viewing orders of all users implemented
// Y User password update and encryption implemented
// Y Junit test cases included
// Y Design pattern (in addition to MVC)

import javafx.application.Application;
import javafx.stage.Stage;
import service.*;
import dao.*;
import utils.NavigationManager;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Entry point of the event booking application.
 * This class bootstraps DAOs and services and launches the first JavaFX scene via NavigationManager.
 */
public class Main extends Application {

	private UserDao userDao;
	private EventDao eventDao;
	private OrderDao orderDao;
	private CartDao cartDao;

	private CartService cartService;
	private OrderService orderService;
	private UserService userService;
	private EventService eventService;

	@Override
	public void start(Stage primaryStage) {
		try {
			initializeDaos();
			setupDatabaseTables();
			initializeServices();
			importEventsIfEmpty();
			NavigationManager.initialize(primaryStage, cartService, orderService, userService, eventService);
			NavigationManager.loadScene("/view/LoginView.fxml", "Login");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error starting the application.");
		} finally {
			closeDatabase();
		}
	}

	private void initializeDaos() {
		DaoFactory factory = DaoFactory.getInstance();
		userDao = factory.getUserDao();
		eventDao = factory.getEventDao();
		orderDao = factory.getOrderDao();
		cartDao = factory.getCartDao();
	}

	private void setupDatabaseTables() throws SQLException {
		userDao.setup();
		eventDao.setup();
		orderDao.setup();
		cartDao.setup();
	}

	private void initializeServices() {
		userService = new UserService(userDao);
		eventService = new EventService(eventDao);
		cartService = new CartService(cartDao, eventService);
		orderService = new OrderService(orderDao, eventService, cartService);
	}

	private void importEventsIfEmpty() throws SQLException, IOException {
		if (eventService.getAllEvents().isEmpty()) {
			eventService.importEventsFromFile("D:\\RMIT\\Semester2\\AdvancedProgramming\\AdvancedProgramming\\Assignment2\\src\\main\\resources\\view\\events.dat");
		}
	}

	private void closeDatabase() {
		try {
			Database.getInstance().closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
