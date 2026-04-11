package util;

import enums.EmployeeStatus;
import model.Employee;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class EmployeeExcelImporter {

    private static final String DEFAULT_DEPARTMENT = "Liste nominative";
    private static final DataFormatter DATA_FORMATTER = new DataFormatter();
    private static final DateTimeFormatter FRENCH_DATE_STRICT = DateTimeFormatter.ofPattern("d/M/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    private EmployeeExcelImporter() {
    }

    public record ImportResult(int successCount, int skippedRows, List<String> errors) {
    }

    public static ImportResult importFromFile(Path file, EmployeeInserter inserter) throws IOException {
        if (file == null || inserter == null) {
            throw new NullPointerException();
        }
        if (!Files.isRegularFile(file)) {
            throw new IOException("Not a file: " + file);
        }

        List<String> errors = new ArrayList<>();
        int success = 0;
        int skipped = 0;

        try (Workbook workbook = WorkbookFactory.create(file.toFile())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                errors.add("The workbook has no sheets.");
                return new ImportResult(0, 0, errors);
            }

            Layout layout = detectLayout(sheet);
            if (layout == null) {
                errors.add("Could not find a header row with NOM and PRENOM (or Last Name / First Name).");
                return new ImportResult(0, 0, errors);
            }

            String departmentFallback = guessDepartmentFromSheetTop(sheet);
            int lastRow = sheet.getLastRowNum();

            for (int r = layout.firstDataRow; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    skipped++;
                    continue;
                }
                String lastName = cellString(row, layout.cols.lastNameCol);
                String firstName = cellString(row, layout.cols.firstNameCol);
                if (isBlank(lastName) && isBlank(firstName)) {
                    skipped++;
                    continue;
                }
                if (isBlank(lastName) || isBlank(firstName)) {
                    errors.add("Row " + (r + 1) + ": both Nom and Prenom are required.");
                    continue;
                }

                String position = layout.cols.positionCol >= 0 ? cellString(row, layout.cols.positionCol) : null;
                if (position != null && position.isBlank()) {
                    position = null;
                }

                LocalDate birthDate = null;
                if (layout.cols.birthDateCol >= 0) {
                    birthDate = parseFrenchDate(cellString(row, layout.cols.birthDateCol));
                }
                LocalDate hireDate = null;
                if (layout.cols.hireDateCol >= 0) {
                    hireDate = parseFrenchDate(cellString(row, layout.cols.hireDateCol));
                }
                if (hireDate == null) {
                    hireDate = LocalDate.now();
                }

                String department = FonctionDepartmentMapper.departmentForFonction(position, departmentFallback);
                Employee employee = buildEmployeeWithoutEmail(
                        firstName.trim(),
                        lastName.trim(),
                        position,
                        birthDate,
                        hireDate,
                        department);

                for (int dup = 0; dup < 30; dup++) {
                    employee.setEmail(gmailAddress(firstName.trim(), lastName.trim(), dup));
                    try {
                        inserter.insert(employee);
                        success++;
                        break;
                    } catch (Exception ex) {
                        if (isDuplicateKey(ex) && dup < 29) {
                            continue;
                        }
                        errors.add("Row " + (r + 1) + ": " + ex.getMessage());
                        break;
                    }
                }
            }
        }

        return new ImportResult(success, skipped, errors);
    }

    @FunctionalInterface
    public interface EmployeeInserter {
        void insert(Employee employee) throws Exception;
    }

    private static Employee buildEmployeeWithoutEmail(String firstName, String lastName, String position,
                                                      LocalDate birthDate, LocalDate hireDate, String department) {
        Employee e = new Employee();
        e.setFirstName(firstName);
        e.setLastName(lastName);
        e.setPosition(position);
        e.setDepartment(department);
        e.setHireDate(hireDate);
        e.setBirthDate(birthDate);
        e.setPhone(null);
        e.setStatus(EmployeeStatus.ACTIVE);
        return e;
    }

    private static String gmailAddress(String firstName, String lastName, int duplicateIndex) {
        String a = sanitizeLocalPart(firstName);
        String b = sanitizeLocalPart(lastName);
        if (a.isEmpty()) {
            a = "prenom";
        }
        if (b.isEmpty()) {
            b = "nom";
        }
        String local = a + "." + b;
        if (duplicateIndex > 0) {
            local = local + "." + (duplicateIndex + 1);
        }
        return local + "@gmail.com";
    }

    private static String sanitizeLocalPart(String s) {
        if (s == null || s.isBlank()) {
            return "";
        }
        String t = Normalizer.normalize(s.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        t = t.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "");
        if (t.length() > 40) {
            t = t.substring(0, 40);
        }
        return t;
    }

    private static boolean isDuplicateKey(Exception ex) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (t instanceof SQLIntegrityConstraintViolationException) {
                return true;
            }
        }
        return false;
    }

    private static final class ColumnIndices {
        int firstNameCol = -1;
        int lastNameCol = -1;
        int positionCol = -1;
        int birthDateCol = -1;
        int hireDateCol = -1;
    }

    private static final class Layout {
        final ColumnIndices cols;
        final int firstDataRow;

        Layout(ColumnIndices cols, int firstDataRow) {
            this.cols = cols;
            this.firstDataRow = firstDataRow;
        }
    }

    private static Layout detectLayout(Sheet sheet) {
        int lastScan = Math.min(sheet.getLastRowNum(), 40);
        for (int r = 0; r <= lastScan; r++) {
            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
            ColumnIndices cols = parseHeaderRow(row);
            if (cols.lastNameCol < 0 || cols.firstNameCol < 0) {
                continue;
            }

            Row sub = sheet.getRow(r + 1);
            boolean listeNominative = sub != null && subHeaderLooksLikeListeNominative(sub);

            if (listeNominative) {
                List<Integer> dateCols = findColumnsWithExactHeader(row, "date");
                if (dateCols.size() >= 2) {
                    cols.birthDateCol = dateCols.get(0);
                    cols.hireDateCol = dateCols.get(1);
                } else if (dateCols.size() == 1) {
                    cols.hireDateCol = dateCols.get(0);
                }
                int entreeCol = findColumnContaining(sub, "entree");
                if (entreeCol >= 0) {
                    cols.hireDateCol = entreeCol;
                }
                int naissanceDateCol = findNaissanceDateColumn(row, sub);
                if (naissanceDateCol >= 0) {
                    cols.birthDateCol = naissanceDateCol;
                }
                return new Layout(cols, r + 2);
            }

            return new Layout(cols, r + 1);
        }
        return null;
    }

    private static boolean subHeaderLooksLikeListeNominative(Row sub) {
        int last = sub.getLastCellNum();
        boolean naissance = false;
        boolean entree = false;
        for (int c = 0; c < last; c++) {
            String raw = cellString(sub, c);
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String n = normalizeHeader(raw);
            if (n.contains("naissance")) {
                naissance = true;
            }
            if (n.contains("entree")) {
                entree = true;
            }
        }
        return naissance && entree;
    }

    private static int findNaissanceDateColumn(Row headerRow, Row subRow) {
        List<Integer> dateCols = findColumnsWithExactHeader(headerRow, "date");
        if (dateCols.isEmpty()) {
            return -1;
        }
        for (int col : dateCols) {
            String sub = cellString(subRow, col);
            if (sub != null && normalizeHeader(sub).contains("naissance")) {
                return col;
            }
        }
        return dateCols.get(0);
    }

    private static int findColumnContaining(Row row, String fragmentNorm) {
        int last = row.getLastCellNum();
        for (int c = 0; c < last; c++) {
            String raw = cellString(row, c);
            if (raw == null) {
                continue;
            }
            if (normalizeHeader(raw).contains(fragmentNorm)) {
                return c;
            }
        }
        return -1;
    }

    private static List<Integer> findColumnsWithExactHeader(Row row, String normalizedTarget) {
        List<Integer> out = new ArrayList<>();
        int last = row.getLastCellNum();
        for (int c = 0; c < last; c++) {
            String raw = cellString(row, c);
            if (raw == null || raw.isBlank()) {
                continue;
            }
            if (normalizeHeader(raw).equals(normalizedTarget)) {
                out.add(c);
            }
        }
        return out;
    }

    private static ColumnIndices parseHeaderRow(Row headerRow) {
        ColumnIndices c = new ColumnIndices();
        int last = headerRow.getLastCellNum();
        for (int col = 0; col < last; col++) {
            String raw = cellString(headerRow, col);
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String norm = normalizeHeader(raw);
            if (matchesLastName(norm)) {
                c.lastNameCol = col;
            } else if (matchesFirstName(norm)) {
                c.firstNameCol = col;
            } else if (matchesPosition(norm)) {
                c.positionCol = col;
            }
        }
        return c;
    }

    private static boolean matchesLastName(String norm) {
        return norm.equals("nom")
                || norm.equals("last name")
                || norm.equals("lastname")
                || norm.equals("family name")
                || norm.equals("surname");
    }

    private static boolean matchesFirstName(String norm) {
        return norm.equals("prenom")
                || norm.equals("first name")
                || norm.equals("firstname")
                || norm.equals("given name");
    }

    private static boolean matchesPosition(String norm) {
        return norm.equals("poste")
                || norm.equals("position")
                || norm.equals("job")
                || norm.equals("title")
                || norm.equals("fonction");
    }

    private static String guessDepartmentFromSheetTop(Sheet sheet) {
        for (int r = 0; r <= Math.min(3, sheet.getLastRowNum()); r++) {
            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
            String cell0 = cellString(row, 0);
            if (cell0 == null || cell0.isBlank()) {
                continue;
            }
            String t = cell0.trim();
            String u = t.toUpperCase(Locale.ROOT);
            if (t.length() >= 3 && (u.contains("SARL") || u.contains("SPA") || u.contains("EURL"))) {
                return t;
            }
        }
        return DEFAULT_DEPARTMENT;
    }

    private static LocalDate parseFrenchDate(String s) {
        if (isBlank(s)) {
            return null;
        }
        String t = s.trim();
        if (t.contains("00/00") || t.startsWith("00/")) {
            return null;
        }
        try {
            return LocalDate.parse(t, FRENCH_DATE_STRICT);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDate.parse(t, DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT));
        } catch (DateTimeParseException ignored) {
        }
        return null;
    }

    private static String normalizeHeader(String raw) {
        String t = raw.trim().toLowerCase(Locale.ROOT);
        t = t.replace("é", "e").replace("è", "e").replace("ê", "e").replace("à", "a");
        t = t.replace("°", "o");
        return t.replaceAll("\\s+", " ").trim();
    }

    private static String cellString(Row row, int col) {
        if (row == null || col < 0) {
            return null;
        }
        Cell cell = row.getCell(col);
        if (cell == null) {
            return null;
        }
        String s = DATA_FORMATTER.formatCellValue(cell);
        return s == null ? null : s.trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
