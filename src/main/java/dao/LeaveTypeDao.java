package dao;

import database.DatabaseConnection;
import model.LeaveType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LeaveTypeDao {

    private static final String SELECT_BASE = "SELECT id, name, description, max_days FROM leave_types ";

    public Optional<LeaveType> findById(long id) {
        final String sql = SELECT_BASE + "WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load leave type " + id, e);
        }
        return Optional.empty();
    }

    public List<LeaveType> findAll() {
        final String sql = SELECT_BASE + "ORDER BY id";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<LeaveType> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list leave types", e);
        }
    }

    public long insert(LeaveType leaveType) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public boolean update(LeaveType leaveType) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public boolean delete(long id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private static LeaveType mapRow(ResultSet rs) throws SQLException {
        LeaveType t = new LeaveType();
        t.setId(rs.getLong("id"));
        t.setName(rs.getString("name"));
        t.setDescription(rs.getString("description"));
        t.setMaxDays(rs.getInt("max_days"));
        return t;
    }
}
