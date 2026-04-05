package dao;

import database.DatabaseConnection;
import enums.EmployeeStatus;
import model.Employee;

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
import java.util.UUID;

public class EmployeeDao {

    private static final String SELECT_BASE = """
            SELECT id, employee_code, first_name, last_name, birth_date, hire_date,
                   department, position, email, phone, status
            FROM employees
            """;

    public Optional<Employee> findById(long id) {
        final String sql = SELECT_BASE + " WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load employee " + id, e);
        }
        return Optional.empty();
    }

    public List<Employee> findAll() {
        final String sql = SELECT_BASE + " ORDER BY id";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<Employee> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list employees", e);
        }
    }

    public long insert(Employee employee) {
        final boolean assignAutoCode = employee.getEmployeeCode() == null || employee.getEmployeeCode().isBlank();
        if (assignAutoCode) {
            employee.setEmployeeCode("T" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        }
        final String sql = """
                INSERT INTO employees (employee_code, first_name, last_name, birth_date, hire_date,
                    department, position, email, phone, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindWritable(statement, employee);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    if (assignAutoCode) {
                        String finalCode = "EMP" + String.format("%04d", id);
                        try (PreparedStatement update = connection.prepareStatement(
                                "UPDATE employees SET employee_code = ? WHERE id = ?")) {
                            update.setString(1, finalCode);
                            update.setLong(2, id);
                            update.executeUpdate();
                        }
                        employee.setEmployeeCode(finalCode);
                    }
                    employee.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to insert employee", e);
        }
        throw new IllegalStateException("Insert succeeded but no generated key returned");
    }

    public boolean update(Employee employee) {
        if (employee.getId() == null) {
            return false;
        }
        final String sql = """
                UPDATE employees SET employee_code = ?, first_name = ?, last_name = ?, birth_date = ?, hire_date = ?,
                    department = ?, position = ?, email = ?, phone = ?, status = ?
                WHERE id = ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindWritable(statement, employee);
            statement.setLong(11, employee.getId());
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update employee", e);
        }
    }

    public boolean delete(long id) {
        final String sql = "DELETE FROM employees WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete employee", e);
        }
    }

    private void bindWritable(PreparedStatement statement, Employee employee) throws SQLException {
        int i = 1;
        statement.setString(i++, employee.getEmployeeCode());
        statement.setString(i++, employee.getFirstName());
        statement.setString(i++, employee.getLastName());
        statement.setDate(i++, toSqlDate(employee.getBirthDate()));
        statement.setDate(i++, toSqlDate(employee.getHireDate()));
        statement.setString(i++, employee.getDepartment());
        statement.setString(i++, employee.getPosition());
        statement.setString(i++, employee.getEmail());
        if (employee.getPhone() == null || employee.getPhone().isBlank()) {
            statement.setNull(i++, java.sql.Types.VARCHAR);
        } else {
            statement.setString(i++, employee.getPhone());
        }
        statement.setString(i++, employee.getStatus() != null ? employee.getStatus().name() : EmployeeStatus.ACTIVE.name());
    }

    private static Date toSqlDate(LocalDate date) {
        return date == null ? null : Date.valueOf(date);
    }

    private static Employee mapRow(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setId(rs.getLong("id"));
        e.setEmployeeCode(rs.getString("employee_code"));
        e.setFirstName(rs.getString("first_name"));
        e.setLastName(rs.getString("last_name"));
        Date bd = rs.getDate("birth_date");
        e.setBirthDate(bd != null ? bd.toLocalDate() : null);
        Date hd = rs.getDate("hire_date");
        e.setHireDate(hd != null ? hd.toLocalDate() : null);
        e.setDepartment(rs.getString("department"));
        e.setPosition(rs.getString("position"));
        e.setEmail(rs.getString("email"));
        e.setPhone(rs.getString("phone"));
        String st = rs.getString("status");
        e.setStatus(st != null ? EmployeeStatus.valueOf(st) : EmployeeStatus.ACTIVE);
        return e;
    }
}
