package dao;

import model.User;
import utils.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite-based implementation of {@link UserDao}.
 */
public class UserDaoImpl implements UserDao {
	private final String TABLE_NAME = "users";

	public UserDaoImpl() {
	}

	@Override
	public void setup() throws SQLException {
		Connection connection = Database.getInstance().getConnection();

		String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
				+ "username VARCHAR(10) NOT NULL, "
				+ "preferred_name VARCHAR(20) NOT NULL, "
				+ "password VARCHAR(64) NOT NULL, "
				+ "PRIMARY KEY (username))";

		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		}

		String adminUsername = "admin";
		String adminPassword = PasswordUtil.encrypt("Admin321");
		String preferredName = "Administrator";

		String checkSql = "SELECT username FROM users WHERE username = ?";
		try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
			checkStmt.setString(1, adminUsername);
			try (ResultSet rs = checkStmt.executeQuery()) {
				if (!rs.next()) {
					String insertSql = "INSERT INTO users (username, password, preferred_name) VALUES (?, ?, ?)";
					try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
						insertStmt.setString(1, adminUsername);
						insertStmt.setString(2, adminPassword);
						insertStmt.setString(3, preferredName);
						insertStmt.executeUpdate();
					}
				}
			}
		}
	}

	@Override
	public User getUser(String username) throws SQLException {
		Connection connection = Database.getInstance().getConnection();
		String sql = "SELECT * FROM users WHERE username = ?";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, username);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return new User(
							rs.getString("username"),
							rs.getString("password"),
							rs.getString("preferred_name")
					);
				}
			}
		}
		return null;
	}

	@Override
	public User createUser(String username, String password, String preferredName) throws SQLException {
		if (userExists(username)) {
			return null; // Username already exists
		}

		Connection connection = Database.getInstance().getConnection();
		String sql = "INSERT INTO " + TABLE_NAME + " (username, password, preferred_name) VALUES (?, ?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, username);
			stmt.setString(2, password);
			stmt.setString(3, preferredName);
			stmt.executeUpdate();
			return new User(username, password, preferredName);
		}
	}

	private boolean userExists(String username) throws SQLException {
		Connection connection = Database.getInstance().getConnection();
		String sql = "SELECT username FROM " + TABLE_NAME + " WHERE username = ?";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, username);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next();  // true if found
			}
		}
	}

	@Override
	public boolean updatePassword(String username, String newPassword) {
		Connection connection;
		try {
			connection = Database.getInstance().getConnection();
			String sql = "UPDATE users SET password = ? WHERE username = ?";
			try (PreparedStatement ps = connection.prepareStatement(sql)) {
				ps.setString(1, newPassword);
				ps.setString(2, username);
				return ps.executeUpdate() > 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean updatePreferredName(String username, String newPreferredName) throws SQLException {
		Connection connection = Database.getInstance().getConnection();
		String sql = "UPDATE users SET preferred_name = ? WHERE username = ?";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, newPreferredName);
			stmt.setString(2, username);
			return stmt.executeUpdate() > 0;
		}
	}

	@Override
	public List<User> getAllUsers() {
		List<User> users = new ArrayList<>();
		Connection connection;
		try {
			connection = Database.getInstance().getConnection();
			String sql = "SELECT * FROM users";
			try (Statement stmt = connection.createStatement();
				 ResultSet rs = stmt.executeQuery(sql)) {
				while (rs.next()) {
					users.add(new User(
							rs.getString("username"),
							rs.getString("password"),
							rs.getString("preferred_name")
					));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return users;
	}
}
