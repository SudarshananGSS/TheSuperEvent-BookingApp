package dao;

/**
 * Singleton wrapper around the JDBC connection used by the application.
 * <p>
 * The instance lazily creates a connection to the bundled SQLite database and
 * returns it via {@link #getConnection()}. Callers should obtain the instance
 * through {@link #getInstance()} rather than creating it directly.
 */


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
	private static Database instance;
	private Connection connection;

	// Update your SQLite DB path here
	private static final String DB_URL = "jdbc:sqlite:mydb.db";

	private Database() throws SQLException {
		// Initialize the connection when the instance is created
		this.connection = DriverManager.getConnection(DB_URL);
	}

	public static synchronized Database getInstance() throws SQLException {
		if (instance == null) {
			instance = new Database();
		} else if (instance.getConnection().isClosed()) {
			instance = new Database();  // Reopen if closed
		}
		return instance;
	}

	public Connection getConnection() {
		return connection;
	}

	public void closeConnection() {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
