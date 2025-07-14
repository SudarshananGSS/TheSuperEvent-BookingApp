package controller;

import config.AppConfig;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.cell.PropertyValueFactory;
import model.CartItem;
import model.Event;
import model.User;
import service.CartService;
import service.EventService;
import service.UserService;
import utils.NavigationManager;
import utils.UserSession;
import utils.Result;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.sql.SQLException;
import java.util.List;

/**
 * Controller for the main dashboard showing available events and navigation options.
 */
public class HomeController {
	@FXML private Label welcomeLabel;
	@FXML private TableView<Event> eventTable;
	@FXML private TableColumn<Event, String> titleCol;
	@FXML private TableColumn<Event, String> venueCol;
	@FXML private TableColumn<Event, String> dayCol;
	@FXML private TableColumn<Event, Double> priceCol;
	@FXML private TableColumn<Event, Integer> availableCol;
	@FXML private TableColumn<Event, Integer> totalCol;
	@FXML private TableColumn<Event, Integer> soldCol;
	@FXML private Button addToCartButton;
	@FXML private Button cartButton;
	@FXML private Button checkoutButton;
	@FXML private Button ordersButton;
	@FXML private Button adminButton;
	@FXML private TextField searchField;

	private CartService cartService;
	private EventService eventService;
	private String username;
	private UserService userService;

	public void setCartService(CartService cartService) {
		this.cartService = cartService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	public void setEventService(EventService eventService) {
		this.eventService = eventService;
		loadEvents();
	}

	@FXML public void initialize() {
		username = UserSession.getCurrentUser();
		if (username != null) {
			welcomeLabel.setText("Welcome, " + username);
		}
		adminButton.setVisible(UserSession.isLoggedIn() && UserSession.getCurrentUser().equals("admin"));

		// Set cart icon programmatically with consistent size
		Image cartImage = new Image(getClass().getResourceAsStream("/icons/cart.png"));
		ImageView cartView = new ImageView(cartImage);
		cartView.setFitWidth(AppConfig.ICON_SIZE);
		cartView.setFitHeight(AppConfig.ICON_SIZE);
		addToCartButton.setGraphic(cartView);
	}

	private void loadEvents() {
		try {
			List<Event> events = eventService.getAllActiveEvents();
			ObservableList<Event> eventList = FXCollections.observableArrayList(events);
			FilteredList<Event> filtered = new FilteredList<>(eventList, p -> true);
			eventTable.setItems(filtered);
			eventTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

			if (searchField != null) {
				searchField.textProperty().addListener((obs, old, text) -> {
					final String lower = text.toLowerCase();
					filtered.setPredicate(ev -> ev.getTitle().toLowerCase().contains(lower));
				});
			}
			titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
			venueCol.setCellValueFactory(new PropertyValueFactory<>("venue"));
			dayCol.setCellValueFactory(new PropertyValueFactory<>("day"));
			priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
			availableCol.setCellValueFactory(cellData -> {
				Event e = cellData.getValue();
				int available = e.getTotalTickets() - e.getSoldTickets();
				return new ReadOnlyObjectWrapper<>(available);
			});
			soldCol.setCellValueFactory(new PropertyValueFactory<>("soldTickets"));
			totalCol.setCellValueFactory(new PropertyValueFactory<>("totalTickets"));

			// Custom comparator for day column
			dayCol.setComparator((day1, day2) -> {
				// Map day names to order
				List<String> dayOrder = List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");
				int index1 = dayOrder.indexOf(day1);
				int index2 = dayOrder.indexOf(day2);
				// Handle unknown days by assigning a high index
				if (index1 == -1) index1 = 8;
				if (index2 == -1) index2 = 8;
				return Integer.compare(index1, index2);
			});
		} catch (SQLException e) {
			e.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Error", "Could not load events.");
		}
	}

	@FXML private void handleLogout() {
		UserSession.clear();
		NavigationManager.logout();
	}

	@FXML private void handleViewProfile() {
		try {
			Result<User> result = userService.getUserByUsername(username);
			if (result.isSuccess() && result.getData() != null) {
				User user = result.getData();
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("Profile");
				alert.setHeaderText("User Profile");
				alert.setContentText("Username: " + user.getUsername() + "\nPreferred Name: " + user.getPreferredName());
				alert.showAndWait();
			} else {
				showAlert(Alert.AlertType.ERROR, "Error", result.getMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Error", "Could not load user profile.");
		}
	}

	@FXML private void handleUpdateProfile() {
		NavigationManager.loadScene("/view/ProfileView.fxml", "Profile");
	}

	@FXML private void handleCart() {
		NavigationManager.loadScene("/view/CartView.fxml", "Cart");
	}

	@FXML private void handleOrders() {
		NavigationManager.loadScene("/view/OrdersView.fxml", "Orders");
	}

	@FXML private void handleAdmin() {
		NavigationManager.loadScene("/view/AdminView1.fxml", "Admin");
	}

	@FXML private void handleAddToCart() {
		Event selectedEvent = eventTable.getSelectionModel().getSelectedItem();
		if (selectedEvent == null) {
			showAlert(Alert.AlertType.WARNING, "No Event Selected", "Please select an event to add to cart.");
			return;
		}

		TextInputDialog dialog = new TextInputDialog("1");
		dialog.setTitle("Select Quantity");
		dialog.setHeaderText("Add Event to Cart");
		dialog.setContentText("Enter quantity:");

		dialog.showAndWait().ifPresent(quantityStr -> {
			try {
				int quantity = Integer.parseInt(quantityStr);
				if (quantity <= 0) {
					showAlert(Alert.AlertType.WARNING, "Invalid Quantity", "Please enter a positive quantity.");
					return;
				}

				List<CartItem> currentCart = cartService.getCartItems(username);
				CartItem existingItem = currentCart.stream()
						.filter(item -> item.getEvent().getEventId() == selectedEvent.getEventId())
						.findFirst()
						.orElse(null);
				int existingQty = existingItem != null ? existingItem.getQuantity() : 0;
				int totalRequested = existingQty + quantity;

				if (totalRequested > selectedEvent.getAvailableTickets()) {
					showAlert(Alert.AlertType.WARNING, "Exceeds Availability",
							"You already have " + existingQty + " tickets. Only " +
									(selectedEvent.getAvailableTickets() - existingQty) + " more available.");
					return;
				}

				CartItem newItem = new CartItem(username, selectedEvent, totalRequested);
				Result<String> result = cartService.updateCartItem(newItem);
				if (result.isSuccess()) {
					showAlert(Alert.AlertType.INFORMATION, "Cart Updated", result.getMessage());
				} else {
					showAlert(Alert.AlertType.ERROR, "Error", result.getMessage());
				}

			} catch (NumberFormatException e) {
				showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please enter a valid number.");
			}
		});
	}

	private void showAlert(Alert.AlertType type, String title, String content) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		if (content == null || content.trim().isEmpty()) {
			content = type == Alert.AlertType.INFORMATION
					? "Operation completed successfully." : "";
		}
		alert.setContentText(content);
		alert.showAndWait();
	}
}
