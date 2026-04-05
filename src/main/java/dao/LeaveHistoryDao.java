package dao;

import database.DatabaseConnection;
import model.LeaveHistory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class LeaveHistoryDao {

    public Optional<LeaveHistory> findById(long id) {
        return Optional.empty();
    }

    public List<LeaveHistory> findAll() {
        return Collections.emptyList();
    }

    public long insert(LeaveHistory history) {
        final String sql = """
                INSERT INTO leave_history (employee_id, leave_request_id, action, action_date, note)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, history.getEmployee().getId());
            if (history.getLeaveRequest() != null && history.getLeaveRequest().getId() != null) {
                statement.setLong(2, history.getLeaveRequest().getId());
            } else {
                statement.setNull(2, java.sql.Types.BIGINT);
            }
            statement.setString(3, history.getAction());
            statement.setDate(4, Date.valueOf(history.getActionDate()));
            statement.setString(5, history.getNote());
            statement.executeUpdate();
            try (var keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to insert leave history", e);
        }
        return -1L;
    }

    public boolean update(LeaveHistory history) {
        return false;
    }

    public boolean delete(long id) {
        return false;
    }
}
