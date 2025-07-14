package model;

/**
 * Represents an application user with login credentials.
 */
public class User {
	private String username;
	private String password;
	private String preferredName;
	private boolean isAdmin;

	public User(String username, String password, String preferredName) {
		this.username = username;
		this.password = password;
		this.preferredName = preferredName;
		if (username.equals("admin")) this.isAdmin = true;
		else this.isAdmin = false;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getPreferredName() { return preferredName; }

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPreferredName(String preferredName) { this.preferredName = preferredName; }

	public void setPassword(String password) {
		this.password = password;
	}

}
