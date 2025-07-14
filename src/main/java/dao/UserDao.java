package dao;

import java.sql.SQLException;
import java.util.List;

import model.User;

/**
 * A data access object (DAO) is a pattern that provides an abstract interface 
 * to a database or other persistence mechanism. 
 * the DAO maps application calls to the persistence layer and provides some specific data operations 
 * without exposing details of the database. 
 */
public interface UserDao {
	/** Create the users table if necessary. */
	void setup() throws SQLException;
	/** Retrieve a user by username. */
	User getUser(String username) throws SQLException;
	/** Create a new user account. */
	User createUser(String username, String password, String preferredName) throws SQLException;
	/** Update an existing user's password. */
	boolean updatePassword(String username, String newPassword) throws SQLException;
	/** Update an existing user's preferred name. */
	boolean updatePreferredName(String username, String newPreferredName) throws SQLException;
	/** Fetch all users. */
	List<User> getAllUsers() throws SQLException;
}
