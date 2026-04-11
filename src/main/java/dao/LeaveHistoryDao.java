package dao;

import database.DatabaseConnection;
import model.Employee;
import model.LeaveHistory;
import model.LeaveRequest;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class LeaveHistoryDao {

    public List<LeaveHistory> findByEmployeeId(long employeeId) {
        final String sql = """
                SELECT id, employee_id, leave_request_id, action, action_date, note
                FROM leave_history
                WHERE employee_id = ?
                ORDER BY action_date DESC, id DESC
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, employeeId);
            try (ResultSet rs = statement.executeQuery()) {
                List<LeaveHistory> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list leave history", e);
        }
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

    private LeaveHistory mapRow(ResultSet rs) throws SQLException {
        LeaveHistory h = new LeaveHistory();
        h.setId(rs.getLong("id"));
        Employee e = new Employee();
        e.setId(rs.getLong("employee_id"));
        h.setEmployee(e);
        long requestId = rs.getLong("leave_request_id");
        if (!rs.wasNull()) {
            LeaveRequest request = new LeaveRequest();
            request.setId(requestId);
            h.setLeaveRequest(request);
        }
        h.setAction(rs.getString("action"));
        Date ad = rs.getDate("action_date");
        h.setActionDate(ad != null ? ad.toLocalDate() : null);
        h.setNote(rs.getString("note"));
        return h;
    }
}
