package dao;

import database.DatabaseConnection;
import model.Admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class AdminDao {

	public Optional<Admin> findByEmailAndPassword(String email, String password) {
		final String sql = "SELECT id, first_name, last_name, email, password FROM admins WHERE email = ? AND password = ?";
		try (Connection connection = DatabaseConnection.getConnection();
		     PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, email);
			statement.setString(2, password);
			try (ResultSet rs = statement.executeQuery()) {
				if (rs.next()) {
					Admin admin = new Admin();
					admin.setId(rs.getLong("id"));
					admin.setFirstName(rs.getString("first_name"));
					admin.setLastName(rs.getString("last_name"));
					admin.setEmail(rs.getString("email"));
					// Do not expose password beyond this point in real apps
					admin.setPassword(rs.getString("password"));
					return Optional.of(admin);
				}
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Failed to authenticate admin", e);
		}
		return Optional.empty();
	}
}

