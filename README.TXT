# TheSuperEvent Booking App

A JavaFX + SQLite desktop application for event discovery, ticket booking, checkout, and order management, with separate user and admin flows.

## Features

- User signup and login
- Password hashing and password updates
- Browse active events by day
- Add events to cart and modify quantities
- Checkout with 6-digit confirmation code validation
- Automatic sold-ticket updates after successful checkout
- View order history
- Export orders to CSV
- Admin dashboard for event and order management
- Admin can add, update, disable/enable, and delete events

## Tech Stack

- Java
- JavaFX (FXML-based UI)
- SQLite (JDBC)
- JUnit 5
- MVC architecture with DAO + Service layers

## Project Structure

- `src/main/java/model`: domain models (`Event`, `Order`, `User`, etc.)
- `src/main/java/dao`: persistence layer (SQLite implementations)
- `src/main/java/service`: business logic and validations
- `src/main/java/controller`: JavaFX controllers
- `src/main/resources/view`: FXML views, styles, and seed event data
- `src/test/java`: unit tests
- `docs/ARCHITECTURE.md`: architecture summary
- `docs/CODE_OVERVIEW.md`: package/class reference

## Prerequisites

- JDK 11 or newer
- IntelliJ IDEA (recommended for this project layout)
- JavaFX SDK configured in IntelliJ
- SQLite JDBC driver configured in IntelliJ project libraries
- JUnit Jupiter configured in IntelliJ project libraries

Note: This repository does not include a Maven or Gradle build file. It is currently configured as an IntelliJ module (`Assignment2.iml`).

## Setup and Run

1. Open the project in IntelliJ IDEA.
2. Ensure module libraries are available:
   - `sqlite-jdbc-3.49.1.0`
   - `javafx-swt`
   - `junit.jupiter`
3. Set the run configuration main class to `Main`.
4. Run the application.

On first run, the app creates `mydb.db` in the project root and creates required tables.

## Default Admin Account

- Username: `admin`
- Password: `Admin321`

The admin user is auto-created in `UserDaoImpl.setup()` if it does not already exist.

## Tests

Unit tests are under `src/test/java`.

To run tests in IntelliJ:

1. Right-click `src/test/java`.
2. Select **Run 'All Tests'**.

## Important Notes

- Database location is defined in `src/main/java/dao/Database.java`:
  - `jdbc:sqlite:mydb.db`
- Initial event import currently uses a hardcoded absolute path in `src/main/java/Main.java`.
  - Update `importEventsIfEmpty()` to use a local relative/resource path on your machine.

## Architecture

The app follows MVC with additional patterns:

- Singleton: `Database`, `DaoFactory`
- Factory: `DaoFactory`
- Result wrapper pattern: `utils.Result<T>` for service success/failure handling

See:

- `docs/ARCHITECTURE.md`
- `docs/CODE_OVERVIEW.md`
