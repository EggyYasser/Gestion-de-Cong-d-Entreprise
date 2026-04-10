package dao;

import database.DatabaseConnection;
import model.Employee;
import model.LeaveBalance;

import util.LeaveAccrualCalculator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
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

    public Optional<LeaveBalance> findByEmployeeIdAndYear(long employeeId, int year) {
        final String sql = """
                SELECT id, employee_id, year, earned_days, used_days, remaining_days
                FROM leave_balances
                WHERE employee_id = ? AND year = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, employeeId);
            statement.setInt(2, year);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load leave balance for year " + year, e);
        }
        return Optional.empty();
    }

    /**
     * Recomputes earned days (Algerian 2.5/month, max 30/year) and keeps existing used days.
     */
    public void syncAccrualForEmployeeYear(long employeeId, int year, LocalDate hireDate) {
        if (hireDate == null) {
            return;
        }
        LocalDate asOf = LocalDate.now();
        double earned = LeaveAccrualCalculator.earnedDaysForYear(hireDate, year, asOf);
        double used = findByEmployeeIdAndYear(employeeId, year).map(LeaveBalance::getUsedDays).orElse(0.0);
        saveManualBalance(employeeId, year, earned, used);
    }

    /**
     * Syncs every civil year from hire year through current year.
     */
    public void syncAccrualForEmployeeAllYears(long employeeId, LocalDate hireDate) {
        if (hireDate == null) {
            return;
        }
        int from = hireDate.getYear();
        int to = LocalDate.now().getYear();
        for (int y = from; y <= to; y++) {
            syncAccrualForEmployeeYear(employeeId, y, hireDate);
        }
        recalculateRemainingForEmployee(employeeId);
    }

    /**
     * Total leave days still available: sum of earned across all years minus sum of used (unlimited carryover).
     */
    public double getTotalAvailableDays(long employeeId) {
        final String sql = """
                SELECT COALESCE(SUM(earned_days), 0) - COALESCE(SUM(used_days), 0) AS total
                FROM leave_balances
                WHERE employee_id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, employeeId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return round2(rs.getDouble("total"));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to compute total leave balance", e);
        }
        return 0.0;
    }

    /**
     * Keeps {@code remaining_days = earned_days - used_days} per row (may be negative if that year's used
     * includes days drawn from earlier years' carryover).
     */
    public void recalculateRemainingForEmployee(long employeeId) {
        final String sql = """
                UPDATE leave_balances
                SET remaining_days = earned_days - used_days
                WHERE employee_id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, employeeId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to recalculate remaining days", e);
        }
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
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

    /**
     * Adds approved leave days to {@code used_days} for {@code year}, using accrual based on {@code hireDate}
     * (30 days max per year, 2.5 per month worked in that year).
     */
    public void applyApprovedLeaveDays(long employeeId, int year, double approvedDays, LocalDate hireDate) {
        LocalDate asOf = LocalDate.now();
        double earned = hireDate == null
                ? 0.0
                : LeaveAccrualCalculator.earnedDaysForYear(hireDate, year, asOf);
        final String sql = """
                INSERT INTO leave_balances (employee_id, year, earned_days, used_days, remaining_days)
                VALUES (?, ?, ?, ?, ? - ?)
                ON DUPLICATE KEY UPDATE
                    earned_days = VALUES(earned_days),
                    used_days = used_days + VALUES(used_days),
                    remaining_days = VALUES(earned_days) - (used_days + VALUES(used_days))
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, employeeId);
            statement.setInt(2, year);
            statement.setDouble(3, earned);
            statement.setDouble(4, approvedDays);
            statement.setDouble(5, earned);
            statement.setDouble(6, approvedDays);
            statement.executeUpdate();
            recalculateRemainingForEmployee(employeeId);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update leave balance after approval", e);
        }
    }

    public void saveManualBalance(long employeeId, int year, double earnedDays, double usedDays) {
        final String sql = """
                INSERT INTO leave_balances (employee_id, year, earned_days, used_days, remaining_days)
                VALUES (?, ?, ?, ?, ? - ?)
                ON DUPLICATE KEY UPDATE
                    earned_days = VALUES(earned_days),
                    used_days = VALUES(used_days),
                    remaining_days = VALUES(earned_days) - VALUES(used_days)
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

