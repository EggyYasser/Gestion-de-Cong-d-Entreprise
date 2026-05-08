package dao;

import database.DatabaseConnection;
import model.Admin;
import util.PasswordSecurity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class AdminDao {

    public Optional<Admin> findByEmailAndPassword(String email, String password) {
        final String sql = "SELECT id, first_name, last_name, email, password FROM admins WHERE email = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    if (!PasswordSecurity.verify(password, storedPassword)) {
                        return Optional.empty();
                    }
                    Admin admin = new Admin();
                    admin.setId(rs.getLong("id"));
                    admin.setFirstName(rs.getString("first_name"));
                    admin.setLastName(rs.getString("last_name"));
                    admin.setEmail(rs.getString("email"));
                    admin.setPassword(storedPassword);
                    if (!PasswordSecurity.isHashed(storedPassword)) {
                        updatePasswordHash(connection, admin.getId(), PasswordSecurity.hashPassword(password));
                    }
                    return Optional.of(admin);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed", e);
        }
        return Optional.empty();
    }

    private void updatePasswordHash(Connection connection, long adminId, String hashedPassword) throws SQLException {
        final String sql = "UPDATE admins SET password = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, hashedPassword);
            statement.setLong(2, adminId);
            statement.executeUpdate();
        }
    }
}
