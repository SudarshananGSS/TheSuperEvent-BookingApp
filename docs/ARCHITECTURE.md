# Architecture Overview

The application is organized using the Model–View–Controller pattern:

- **model** – Plain Java objects representing events, orders and users.
- **dao** – Data access interfaces and SQLite implementations.
- **service** – Business logic that coordinates DAOs and provides data to controllers.
- **controller** – JavaFX controllers driving the FXML views.

Additional design patterns:

- **Singleton** – `Database` and `DaoFactory` ensure only one database connection and DAO provider.
- **Factory** – `DaoFactory` encapsulates creation of DAO implementations.
- **Result wrapper** – encapsulates success/failure states for service calls.

Unit tests reside under `src/test/java` and use stub DAOs to isolate business logic.