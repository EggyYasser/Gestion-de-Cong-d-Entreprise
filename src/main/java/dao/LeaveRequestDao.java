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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LeaveRequestDao {

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

    public Optional<LeaveRequest> findById(long id) {
        final String sql = JOINED_SELECT + " WHERE lr.id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapJoinedRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load leave request " + id, e);
        }
        return Optional.empty();
    }

    public List<LeaveRequest> findAll() {
        final String sql = JOINED_SELECT + " ORDER BY lr.request_date DESC, lr.id DESC";
        return queryList(sql, null);
    }

    public List<LeaveRequest> findByStatus(LeaveRequestStatus status) {
        final String sql = JOINED_SELECT + " WHERE lr.status = ? ORDER BY lr.request_date DESC, lr.id DESC";
        return queryList(sql, status);
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
        final String sql = """
                INSERT INTO leave_requests (employee_id, leave_type_id, processed_by, request_date, start_date, end_date,
                    reason, status, rejection_comment)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
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
            LeaveRequestStatus st = request.getStatus() != null ? request.getStatus() : LeaveRequestStatus.PENDING;
            statement.setString(8, st.name());
            if (request.getRejectionComment() == null) {
                statement.setNull(9, java.sql.Types.VARCHAR);
            } else {
                statement.setString(9, request.getRejectionComment());
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

    public boolean update(LeaveRequest request) {
        if (request.getId() == null) {
            return false;
        }
        final String sql = """
                UPDATE leave_requests SET employee_id = ?, leave_type_id = ?, processed_by = ?, request_date = ?,
                    start_date = ?, end_date = ?, reason = ?, status = ?, rejection_comment = ?
                WHERE id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int i = 1;
            statement.setLong(i++, request.getEmployee().getId());
            statement.setLong(i++, request.getLeaveType().getId());
            if (request.getProcessedBy() != null && request.getProcessedBy().getId() != null) {
                statement.setLong(i++, request.getProcessedBy().getId());
            } else {
                statement.setNull(i++, java.sql.Types.BIGINT);
            }
            statement.setDate(i++, Date.valueOf(request.getRequestDate()));
            statement.setDate(i++, Date.valueOf(request.getStartDate()));
            statement.setDate(i++, Date.valueOf(request.getEndDate()));
            if (request.getReason() == null || request.getReason().isBlank()) {
                statement.setNull(i++, java.sql.Types.VARCHAR);
            } else {
                statement.setString(i++, request.getReason());
            }
            statement.setString(i++, request.getStatus().name());
            if (request.getRejectionComment() == null) {
                statement.setNull(i++, java.sql.Types.VARCHAR);
            } else {
                statement.setString(i++, request.getRejectionComment());
            }
            statement.setLong(i, request.getId());
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update leave request", e);
        }
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
        String st = rs.getString("status");
        lr.setStatus(st != null ? LeaveRequestStatus.valueOf(st) : LeaveRequestStatus.PENDING);
        lr.setRejectionComment(rs.getString("rejection_comment"));
        return lr;
    }
}
