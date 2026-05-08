package util;

import model.Employee;
import model.LeaveRequest;
import model.LeaveType;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LeaveRequestCsvExporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public void export(List<LeaveRequest> requests, Path outputFile) {
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            writer.write("Request ID,Employee Code,Employee Name,Leave Type,Start Date,End Date,Status");
            writer.newLine();
            for (LeaveRequest request : requests) {
                Employee employee = request.getEmployee();
                LeaveType type = request.getLeaveType();
                String employeeCode = employee != null ? safe(employee.getEmployeeCode()) : "";
                String employeeName = employee == null
                        ? ""
                        : (safe(employee.getFirstName()) + " " + safe(employee.getLastName())).trim();
                String leaveType = type != null ? safe(type.getName()) : "";
                String line = csv(request.getId() == null ? "" : request.getId().toString()) + ","
                        + csv(employeeCode) + ","
                        + csv(employeeName) + ","
                        + csv(leaveType) + ","
                        + csv(formatDate(request.getStartDate())) + ","
                        + csv(formatDate(request.getEndDate())) + ","
                        + csv(request.getStatus() == null ? "" : request.getStatus().name());
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not export leave requests to CSV.", exception);
        }
    }

    private String formatDate(java.time.LocalDate value) {
        return value == null ? "" : DATE_FORMATTER.format(value);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String csv(String value) {
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
