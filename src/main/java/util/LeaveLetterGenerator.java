package util;

import com.gestionconges.Main;
import model.Admin;
import model.Employee;
import model.LeaveRequest;
import model.LeaveType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class LeaveLetterGenerator {

    private static final String TEMPLATE_PATH = "/templates/leave-letter-template.docx";
    private static final DateTimeFormatter LETTER_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public Path generateApprovedLeaveLetter(LeaveRequest request, LocalDate approvalDate) {
        if (request == null || request.getStatus() == null || !request.getStatus().name().equals("APPROVED")) {
            throw new IllegalArgumentException("Only approved leave requests can generate a letter.");
        }
        Path outputPath;
        try {
            outputPath = Files.createTempFile(buildFilePrefix(request), ".docx");
            outputPath.toFile().deleteOnExit();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not prepare temporary leave letter file", exception);
        }
        Map<String, String> placeholders = buildPlaceholderMap(request, approvalDate);

        try (InputStream inputStream = Main.class.getResourceAsStream(TEMPLATE_PATH)) {
            if (inputStream == null) {
                throw new IllegalStateException("Template not found: " + TEMPLATE_PATH);
            }

            try (XWPFDocument document = new XWPFDocument(inputStream);
                 OutputStream outputStream = Files.newOutputStream(outputPath)) {
                replaceInDocument(document, placeholders);
                document.write(outputStream);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not generate leave letter", exception);
        }

        openFile(outputPath);
        return outputPath;
    }

    private Map<String, String> buildPlaceholderMap(LeaveRequest request, LocalDate approvalDate) {
        Employee employee = request.getEmployee();
        LeaveType leaveType = request.getLeaveType();
        Admin admin = request.getProcessedBy();

        Map<String, String> placeholders = new LinkedHashMap<>();
        placeholders.put("{reference}", formatReference(request));
        placeholders.put("{request_date}", formatDate(request.getRequestDate()));
        placeholders.put("{leave_type}", safeValue(leaveType != null ? leaveType.getName() : null));
        placeholders.put("{employee_name}", buildEmployeeName(employee));
        placeholders.put("{birth_date}", formatDate(employee != null ? employee.getBirthDate() : null));
        placeholders.put("{position}", safeValue(employee != null ? employee.getPosition() : null));
        placeholders.put("{duration}", String.valueOf(request.calculateDuration()));
        placeholders.put("{start_date}", formatDate(request.getStartDate()));
        placeholders.put("{end_date}", formatDate(request.getEndDate()));
        placeholders.put("{approval_date}", formatDate(approvalDate != null ? approvalDate : LocalDate.now()));
        placeholders.put("{admin_name}", buildAdminName(admin));
        return placeholders;
    }

    private void replaceInDocument(XWPFDocument document, Map<String, String> placeholders) {
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            replaceInParagraph(paragraph, placeholders);
        }
        for (XWPFTable table : document.getTables()) {
            replaceInTable(table, placeholders);
        }
        for (XWPFHeader header : document.getHeaderList()) {
            for (XWPFParagraph paragraph : header.getParagraphs()) {
                replaceInParagraph(paragraph, placeholders);
            }
            for (XWPFTable table : header.getTables()) {
                replaceInTable(table, placeholders);
            }
        }
        for (XWPFFooter footer : document.getFooterList()) {
            for (XWPFParagraph paragraph : footer.getParagraphs()) {
                replaceInParagraph(paragraph, placeholders);
            }
            for (XWPFTable table : footer.getTables()) {
                replaceInTable(table, placeholders);
            }
        }
    }

    private void replaceInTable(XWPFTable table, Map<String, String> placeholders) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph paragraph : cell.getParagraphs()) {
                    replaceInParagraph(paragraph, placeholders);
                }
                for (XWPFTable nestedTable : cell.getTables()) {
                    replaceInTable(nestedTable, placeholders);
                }
            }
        }
    }

    private void replaceInParagraph(XWPFParagraph paragraph, Map<String, String> placeholders) {
        String originalText = paragraph.getText();
        if (originalText == null || originalText.isBlank()) {
            return;
        }

        String updatedText = originalText;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            updatedText = updatedText.replace(entry.getKey(), entry.getValue());
        }
        if (updatedText.equals(originalText)) {
            return;
        }

        XWPFRun templateRun = paragraph.getRuns().isEmpty() ? null : paragraph.getRuns().get(0);
        CTRPr copiedStyle = null;
        if (templateRun != null && templateRun.getCTR().isSetRPr()) {
            copiedStyle = (CTRPr) templateRun.getCTR().getRPr().copy();
        }

        while (!paragraph.getRuns().isEmpty()) {
            paragraph.removeRun(0);
        }

        XWPFRun newRun = paragraph.createRun();
        if (copiedStyle != null) {
            newRun.getCTR().setRPr(copiedStyle);
        }
        newRun.setText(updatedText, 0);
    }

    private void openFile(Path filePath) {
        if (!Desktop.isDesktopSupported()) {
            return;
        }
        try {
            Desktop.getDesktop().open(filePath.toFile());
        } catch (IOException ignored) {
        }
    }

    private String buildFilePrefix(LeaveRequest request) {
        String employeeCode = request.getEmployee() != null && request.getEmployee().getEmployeeCode() != null
                ? request.getEmployee().getEmployeeCode().trim()
                : "EMP";
        return "leave_letter_" + employeeCode + "_REQ" + request.getId() + "_"
                + FILE_DATE_FORMAT.format(LocalDateTime.now()) + "_";
    }

    private String formatReference(LeaveRequest request) {
        Long id = request.getId();
        return id == null ? "0000" : String.format("%04d", id);
    }

    private String buildEmployeeName(Employee employee) {
        if (employee == null) {
            return "";
        }
        return (safeValue(employee.getFirstName()) + " " + safeValue(employee.getLastName())).trim();
    }

    private String buildAdminName(Admin admin) {
        if (admin == null) {
            return "";
        }
        return (safeValue(admin.getFirstName()) + " " + safeValue(admin.getLastName())).trim();
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : LETTER_DATE_FORMAT.format(date);
    }

    private String safeValue(String value) {
        return value == null ? "" : value.trim();
    }
}
