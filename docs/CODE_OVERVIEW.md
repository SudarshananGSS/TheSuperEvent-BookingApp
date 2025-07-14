# Code Overview

This document summarizes the main packages and classes in the application.
It is intended as a high level reference when navigating the source code.

## Packages

### `model`
Data representation objects used throughout the system.

- **Event** – Single performance of a show including venue, day, price and ticket counts.
- **EventGroup** – Helper for admin views that groups multiple `Event` instances of the same title.
- **CartItem** – Item currently in a user's cart linking an event with the selected quantity.
- **Order** – Persisted order consisting of multiple `OrderItem` records.
- **OrderItem** – Line item within an order referencing an event and quantity.
- **User** – Application user details and encrypted password.

### `dao`
Data access layer responsible for persistence. Implementations use SQLite via `Database` singleton.

- **Database** – Maintains the single connection to the SQLite database.
- **DaoFactory** – Singleton factory producing DAO instances. Supports swapping implementations for testing.
- **UserDao/UserDaoImpl** – CRUD operations for `User` records.
- **EventDao/EventDaoImpl** – Manipulates `Event` data including enable/disable logic and grouping.
- **OrderDao/OrderDaoImpl** – Persists `Order` and `OrderItem` data and generates order ids.
- **CartDao/CartDaoImpl** – Stores temporary cart items per user.

### `service`
Business logic sitting between controllers and DAOs. Each service returns `Result` objects
representing success or failure.

- **UserService** – Handles login, signup and profile operations.
- **EventService** – Provides event import, querying and ticket updates.
- **CartService** – Manages the shopping cart and validates availability.
- **OrderService** – Performs checkout, validation and exporting of orders.

### `controller`
JavaFX controllers backing the FXML views.

- **LoginController** – Handles user authentication.
- **SignupController** – Creates new user accounts.
- **HomeController** – Displays the dashboard of events and routes to other views.
- **CartController** – Allows editing items in the cart.
- **CheckoutController** – Final confirmation and payment step.
- **OrdersController** – Shows past orders and triggers export.
- **ProfileController** – Allows password change and profile updates.
- **AdminController** – Admin only features for managing events and viewing all orders.

### `utils`
Utility classes used across the project.

- **NavigationManager** – Central place for scene transitions.
- **UserSession** – Tracks the currently logged-in user.
- **PasswordUtil** – Provides SHA‑256 based password hashing and validation.
- **Result** – Generic wrapper used by services for returning success/failure states.

### `Main`
Entry point that initializes DAOs, creates services and launches the JavaFX application.