package util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;

public class LeaveAttachmentService {

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "png", "jpg", "jpeg");
    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final Path ATTACHMENTS_ROOT = Paths.get("uploads", "leave_documents").toAbsolutePath().normalize();

    public void validateFile(File file) {
        if (file == null) {
            throw new IllegalArgumentException("No file selected.");
        }
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Selected file does not exist.");
        }
        String extension = extractExtension(file.getName());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Invalid file format. Accepted: PDF, PNG, JPG, JPEG.");
        }
        if (file.length() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File too large. Maximum size is 5 MB.");
        }
    }

    public String storeFile(File file) {
        validateFile(file);
        try {
            Files.createDirectories(ATTACHMENTS_ROOT);
            String extension = extractExtension(file.getName());
            String storedName = "leave_doc_" + FILE_TS.format(LocalDateTime.now()) + "_" + System.nanoTime() + "." + extension;
            Path destination = ATTACHMENTS_ROOT.resolve(storedName);
            Files.copy(file.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
            return "uploads/leave_documents/" + storedName;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save attachment file.", exception);
        }
    }

    public boolean hasAttachment(String storedPath) {
        return storedPath != null && !storedPath.isBlank();
    }

    public boolean openAttachment(String storedPath) {
        if (!hasAttachment(storedPath)) {
            return false;
        }
        Path filePath = resolveStoredPath(storedPath);
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            return false;
        }
        if (!Desktop.isDesktopSupported()) {
            return false;
        }
        try {
            Desktop.getDesktop().open(filePath.toFile());
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    public void deleteAttachmentQuietly(String storedPath) {
        if (!hasAttachment(storedPath)) {
            return;
        }
        Path filePath = resolveStoredPath(storedPath);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
            // Best-effort cleanup only.
        }
    }

    private Path resolveStoredPath(String storedPath) {
        Path path = Paths.get(storedPath);
        if (path.isAbsolute()) {
            return path.normalize();
        }
        String normalized = storedPath.replace("\\", "/");
        return Paths.get(normalized).toAbsolutePath().normalize();
    }

    private String extractExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        if (idx < 0 || idx >= fileName.length() - 1) {
            return "";
        }
        return fileName.substring(idx + 1).toLowerCase(Locale.ROOT);
    }
}
