package dao;

import database.DatabaseConnection;
import model.Employee;
import model.LeaveBalance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LeaveBalanceDao {

    public Optional<LeaveBalance> findById(long id) {
        final String sql = """
                SELECT id, employee_id, year, earned_days, used_days, remaining_days
                FROM leave_balances
                WHERE id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load leave balance " + id, e);
        }
        return Optional.empty();
    }

    public List<LeaveBalance> findAll() {
        final String sql = """
                SELECT id, employee_id, year, earned_days, used_days, remaining_days
                FROM leave_balances
                ORDER BY year DESC, employee_id ASC
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<LeaveBalance> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list leave balances", e);
        }
    }

    public List<LeaveBalance> findByEmployeeId(long employeeId) {
        final String sql = """
                SELECT id, employee_id, year, earned_days, used_days, remaining_days
                FROM leave_balances
                WHERE employee_id = ?
                ORDER BY year DESC
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, employeeId);
            try (ResultSet rs = statement.executeQuery()) {
                List<LeaveBalance> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list employee leave balances", e);
        }
    }

    public long insert(LeaveBalance balance) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public boolean update(LeaveBalance balance) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public boolean delete(long id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void applyApprovedLeaveDays(long employeeId, int year, double approvedDays) {
        final String sql = """
                INSERT INTO leave_balances (employee_id, year, earned_days, used_days, remaining_days)
                VALUES (?, ?, 30, ?, GREATEST(30 - ?, 0))
                ON DUPLICATE KEY UPDATE
                    used_days = used_days + VALUES(used_days),
                    remaining_days = GREATEST(earned_days - (used_days + VALUES(used_days)), 0)
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, employeeId);
            statement.setInt(2, year);
            statement.setDouble(3, approvedDays);
            statement.setDouble(4, approvedDays);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update leave balance after approval", e);
        }
    }

    public void saveManualBalance(long employeeId, int year, double earnedDays, double usedDays) {
        final String sql = """
                INSERT INTO leave_balances (employee_id, year, earned_days, used_days, remaining_days)
                VALUES (?, ?, ?, ?, GREATEST(? - ?, 0))
                ON DUPLICATE KEY UPDATE
                    earned_days = VALUES(earned_days),
                    used_days = VALUES(used_days),
                    remaining_days = VALUES(remaining_days)
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, employeeId);
            statement.setInt(2, year);
            statement.setDouble(3, earnedDays);
            statement.setDouble(4, usedDays);
            statement.setDouble(5, earnedDays);
            statement.setDouble(6, usedDays);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save leave balance", e);
        }
    }

    private LeaveBalance mapRow(ResultSet rs) throws SQLException {
        LeaveBalance b = new LeaveBalance();
        b.setId(rs.getLong("id"));
        Employee e = new Employee();
        e.setId(rs.getLong("employee_id"));
        b.setEmployee(e);
        b.setYear(rs.getInt("year"));
        b.setEarnedDays(rs.getDouble("earned_days"));
        b.setUsedDays(rs.getDouble("used_days"));
        b.setRemainingDays(rs.getDouble("remaining_days"));
        return b;
    }
}

