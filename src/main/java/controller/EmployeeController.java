package controller;

import dao.EmployeeDao;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.Employee;
import util.ViewNavigator;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Consumer;

public class EmployeeController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final EmployeeDao employeeDao = new EmployeeDao();

    private long employeeId;
    private Runnable onDataChanged;

    @FXML
    private Label codeLabel;

    @FXML
    private Label deptLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label hireDateLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private Label phoneLabel;

    @FXML
    private Label positionLabel;

    @FXML
    private Label statusBadge;

    /**
     * Called by the host window after load, before {@code show()}.
     */
    public void initForEmployee(long employeeId, Consumer<String> errorConsumer, Runnable onDataChanged) {
        this.employeeId = employeeId;
        this.onDataChanged = onDataChanged;
        employeeDao.findById(employeeId).ifPresentOrElse(this::populate, () -> {
            if (errorConsumer != null) {
                errorConsumer.accept("Employee not found.");
            }
            closeStage();
        });
    }

    private void populate(Employee e) {
        nameLabel.setText(e.getFirstName() + " " + e.getLastName());
        codeLabel.setText(e.getEmployeeCode() != null ? e.getEmployeeCode() : "—");
        statusBadge.setText(e.getStatus() != null ? e.getStatus().name() : "—");
        deptLabel.setText(nullToDash(e.getDepartment()));
        positionLabel.setText(nullToDash(e.getPosition()));
        emailLabel.setText(nullToDash(e.getEmail()));
        phoneLabel.setText(nullToDash(e.getPhone()));
        hireDateLabel.setText(e.getHireDate() != null ? e.getHireDate().format(DATE_FMT) : "—");
    }

    private static String nullToDash(String s) {
        return s == null || s.isBlank() ? "—" : s;
    }

    @FXML
    private void onClose() {
        closeStage();
    }

    @FXML
    private void onDeleteAccount() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete employee");
        confirm.setHeaderText(null);
        confirm.setContentText("Delete this employee and related leave data (per database rules)? This cannot be undone.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }
        if (employeeDao.delete(employeeId)) {
            if (onDataChanged != null) {
                onDataChanged.run();
            }
            ViewNavigator.showInformation("Employees", "Employee deleted.");
            closeStage();
        } else {
            ViewNavigator.showInformation("Employees", "Could not delete employee.");
        }
    }

    private void closeStage() {
        Stage stage = (Stage) nameLabel.getScene().getWindow();
        stage.close();
    }
}
