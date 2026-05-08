package dao;

import database.DatabaseConnection;
import enums.EmployeeStatus;
import enums.LeaveRequestStatus;
import model.Admin;
import model.Employee;
import model.LeaveRequest;
import model.LeaveType;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class LeaveRequestDao {
    private static volatile boolean attachmentColumnEnsured = false;

    private static final String JOINED_SELECT = """
            SELECT
              lr.id AS lr_id,
              lr.employee_id,
              lr.leave_type_id,
              lr.processed_by,
              lr.request_date,
              lr.start_date,
              lr.end_date,
              lr.reason,
              lr.attachment_path,
              lr.status,
              lr.rejection_comment,
              e.id AS e_id,
              e.employee_code,
              e.first_name AS e_first_name,
              e.last_name AS e_last_name,
              e.birth_date AS e_birth_date,
              e.hire_date AS e_hire_date,
              e.department AS e_department,
              e.position AS e_position,
              e.email AS e_email,
              e.phone AS e_phone,
              e.status AS e_status,
              lt.id AS lt_id,
              lt.name AS lt_name,
              lt.description AS lt_description,
              lt.max_days AS lt_max_days,
              a.id AS a_id,
              a.first_name AS a_first_name,
              a.last_name AS a_last_name,
              a.email AS a_email
            FROM leave_requests lr
            INNER JOIN employees e ON lr.employee_id = e.id
            INNER JOIN leave_types lt ON lr.leave_type_id = lt.id
            LEFT JOIN admins a ON lr.processed_by = a.id
            """;

    public List<LeaveRequest> findAll() {
        ensureAttachmentColumnExists();
        final String sql = JOINED_SELECT + " ORDER BY lr.request_date DESC, lr.id DESC";
        return queryList(sql, null);
    }

    public List<LeaveRequest> findByStatus(LeaveRequestStatus status) {
        ensureAttachmentColumnExists();
        final String sql = JOINED_SELECT + " WHERE lr.status = ? ORDER BY lr.request_date DESC, lr.id DESC";
        return queryList(sql, status);
    }

    public List<LeaveRequest> findRecent(int limit) {
        ensureAttachmentColumnExists();
        int safeLimit = Math.max(1, limit);
        final String sql = JOINED_SELECT + " ORDER BY lr.request_date DESC, lr.id DESC LIMIT ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, safeLimit);
            try (ResultSet rs = statement.executeQuery()) {
                List<LeaveRequest> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapJoinedRow(rs));
                }
                return list;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to list recent leave requests", exception);
        }
    }

    private List<LeaveRequest> queryList(String sql, LeaveRequestStatus filterStatus) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (filterStatus != null) {
                statement.setString(1, filterStatus.name());
            }
            try (ResultSet rs = statement.executeQuery()) {
                List<LeaveRequest> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapJoinedRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list leave requests", e);
        }
    }

    public long insert(LeaveRequest request) {
        ensureAttachmentColumnExists();
        final String sql = """
                INSERT INTO leave_requests (employee_id, leave_type_id, processed_by, request_date, start_date, end_date,
                    reason, attachment_path, status, rejection_comment)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, request.getEmployee().getId());
            statement.setLong(2, request.getLeaveType().getId());
            if (request.getProcessedBy() != null && request.getProcessedBy().getId() != null) {
                statement.setLong(3, request.getProcessedBy().getId());
            } else {
                statement.setNull(3, java.sql.Types.BIGINT);
            }
            LocalDate reqDate = request.getRequestDate() != null ? request.getRequestDate() : LocalDate.now();
            statement.setDate(4, Date.valueOf(reqDate));
            statement.setDate(5, Date.valueOf(request.getStartDate()));
            statement.setDate(6, Date.valueOf(request.getEndDate()));
            if (request.getReason() == null || request.getReason().isBlank()) {
                statement.setNull(7, java.sql.Types.VARCHAR);
            } else {
                statement.setString(7, request.getReason());
            }
            if (request.getAttachmentPath() == null || request.getAttachmentPath().isBlank()) {
                statement.setNull(8, java.sql.Types.VARCHAR);
            } else {
                statement.setString(8, request.getAttachmentPath());
            }
            LeaveRequestStatus st = request.getStatus() != null ? request.getStatus() : LeaveRequestStatus.PENDING;
            statement.setString(9, st.name());
            if (request.getRejectionComment() == null) {
                statement.setNull(10, java.sql.Types.VARCHAR);
            } else {
                statement.setString(10, request.getRejectionComment());
            }
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to insert leave request", e);
        }
        throw new IllegalStateException("Insert succeeded but no generated key returned");
    }

    public boolean updateStatus(long requestId, LeaveRequestStatus status, Long processedByAdminId, String rejectionComment) {
        final String sql = """
                UPDATE leave_requests SET status = ?, processed_by = ?, rejection_comment = ?
                WHERE id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            if (processedByAdminId != null) {
                statement.setLong(2, processedByAdminId);
            } else {
                statement.setNull(2, java.sql.Types.BIGINT);
            }
            if (rejectionComment != null) {
                statement.setString(3, rejectionComment);
            } else {
                statement.setNull(3, java.sql.Types.VARCHAR);
            }
            statement.setLong(4, requestId);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update leave request status", e);
        }
    }

    public boolean updatePendingDetails(long requestId, long leaveTypeId, LocalDate start, LocalDate end, String reason) {
        final String sql = """
                UPDATE leave_requests SET leave_type_id = ?, start_date = ?, end_date = ?, reason = ?
                WHERE id = ? AND status = 'PENDING'
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, leaveTypeId);
            statement.setDate(2, Date.valueOf(start));
            statement.setDate(3, Date.valueOf(end));
            statement.setString(4, reason);
            statement.setLong(5, requestId);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update pending leave request", e);
        }
    }

    public boolean delete(long id) {
        final String sql = "DELETE FROM leave_requests WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete leave request", e);
        }
    }

    public long countByStatusInRequestMonth(LeaveRequestStatus status, YearMonth month) {
        ensureAttachmentColumnExists();
        final String sql = """
                SELECT COUNT(*) AS c
                FROM leave_requests
                WHERE status = ?
                  AND request_date >= ?
                  AND request_date < ?
                """;
        LocalDate monthStart = month.atDay(1);
        LocalDate nextMonthStart = month.plusMonths(1).atDay(1);
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setDate(2, Date.valueOf(monthStart));
            statement.setDate(3, Date.valueOf(nextMonthStart));
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getLong("c") : 0L;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to count leave requests by month", exception);
        }
    }

    public long countApprovedStartingInNextDays(int days) {
        ensureAttachmentColumnExists();
        final String sql = """
                SELECT COUNT(*) AS c
                FROM leave_requests
                WHERE status = 'APPROVED'
                  AND start_date >= ?
                  AND start_date <= ?
                """;
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(Math.max(days, 0));
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(today));
            statement.setDate(2, Date.valueOf(limit));
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getLong("c") : 0L;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to count upcoming approved leaves", exception);
        }
    }

    private static LeaveRequest mapJoinedRow(ResultSet rs) throws SQLException {
        Employee emp = new Employee();
        emp.setId(rs.getLong("e_id"));
        emp.setEmployeeCode(rs.getString("employee_code"));
        emp.setFirstName(rs.getString("e_first_name"));
        emp.setLastName(rs.getString("e_last_name"));
        Date ebd = rs.getDate("e_birth_date");
        emp.setBirthDate(ebd != null ? ebd.toLocalDate() : null);
        Date ehd = rs.getDate("e_hire_date");
        emp.setHireDate(ehd != null ? ehd.toLocalDate() : null);
        emp.setDepartment(rs.getString("e_department"));
        emp.setPosition(rs.getString("e_position"));
        emp.setEmail(rs.getString("e_email"));
        emp.setPhone(rs.getString("e_phone"));
        String es = rs.getString("e_status");
        emp.setStatus(es != null ? EmployeeStatus.valueOf(es) : EmployeeStatus.ACTIVE);

        LeaveType lt = new LeaveType();
        lt.setId(rs.getLong("lt_id"));
        lt.setName(rs.getString("lt_name"));
        lt.setDescription(rs.getString("lt_description"));
        lt.setMaxDays(rs.getInt("lt_max_days"));

        Admin admin = null;
        long adminId = rs.getLong("a_id");
        if (!rs.wasNull()) {
            admin = new Admin();
            admin.setId(adminId);
            admin.setFirstName(rs.getString("a_first_name"));
            admin.setLastName(rs.getString("a_last_name"));
            admin.setEmail(rs.getString("a_email"));
        }

        LeaveRequest lr = new LeaveRequest();
        lr.setId(rs.getLong("lr_id"));
        lr.setEmployee(emp);
        lr.setLeaveType(lt);
        lr.setProcessedBy(admin);
        Date rd = rs.getDate("request_date");
        lr.setRequestDate(rd != null ? rd.toLocalDate() : null);
        Date sd = rs.getDate("start_date");
        lr.setStartDate(sd != null ? sd.toLocalDate() : null);
        Date ed = rs.getDate("end_date");
        lr.setEndDate(ed != null ? ed.toLocalDate() : null);
        lr.setReason(rs.getString("reason"));
        lr.setAttachmentPath(rs.getString("attachment_path"));
        String st = rs.getString("status");
        lr.setStatus(st != null ? LeaveRequestStatus.valueOf(st) : LeaveRequestStatus.PENDING);
        lr.setRejectionComment(rs.getString("rejection_comment"));
        return lr;
    }

    private void ensureAttachmentColumnExists() {
        if (attachmentColumnEnsured) {
            return;
        }
        synchronized (LeaveRequestDao.class) {
            if (attachmentColumnEnsured) {
                return;
            }
            final String addColumnSql = "ALTER TABLE leave_requests ADD COLUMN attachment_path VARCHAR(255) NULL";
            try (Connection connection = DatabaseConnection.getConnection()) {
                if (!hasColumn(connection, "leave_requests", "attachment_path")) {
                    try (PreparedStatement statement = connection.prepareStatement(addColumnSql)) {
                        statement.executeUpdate();
                    }
                }
                attachmentColumnEnsured = true;
            } catch (SQLException exception) {
                throw new IllegalStateException("Failed to verify leave request attachment column", exception);
            }
        }
    }

    private boolean hasColumn(Connection connection, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String catalog = connection.getCatalog();
        try (ResultSet rs = metaData.getColumns(catalog, null, tableName, columnName)) {
            if (rs.next()) {
                return true;
            }
        }
        try (ResultSet rs = metaData.getColumns(catalog, null, tableName.toUpperCase(), columnName.toUpperCase())) {
            if (rs.next()) {
                return true;
            }
        }
        try (ResultSet rs = metaData.getColumns(null, null, tableName, columnName)) {
            if (rs.next()) {
                return true;
            }
        }
        try (ResultSet rs = metaData.getColumns(null, null, tableName.toUpperCase(), columnName.toUpperCase())) {
            return rs.next();
        }
    }
}
