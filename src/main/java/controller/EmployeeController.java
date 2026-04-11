package controller;

import com.gestionconges.Main;
import dao.EmployeeDao;
import dao.LeaveBalanceDao;
import dao.LeaveHistoryDao;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.LeaveBalance;
import model.LeaveHistory;
import model.Employee;
import enums.EmployeeStatus;
import util.DepartmentOptions;
import util.ViewNavigator;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class EmployeeController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final String RED_BUTTON_STYLE =
            "-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;";
    private static final String GREEN_BUTTON_STYLE =
            "-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;";

    private final EmployeeDao employeeDao = new EmployeeDao();
    private final LeaveBalanceDao leaveBalanceDao = new LeaveBalanceDao();
    private final LeaveHistoryDao leaveHistoryDao = new LeaveHistoryDao();

    private long employeeId;
    private Runnable onDataChanged;
    private Employee currentEmployee;

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

    @FXML
    private Button deleteButton;

    public void initForEmployee(long employeeId, Consumer<String> errorConsumer, Runnable onDataChanged) {
        this.employeeId = employeeId;
        this.onDataChanged = onDataChanged;
        reloadEmployee();
        if (currentEmployee == null) {
            if (errorConsumer != null) {
                errorConsumer.accept("Employee not found.");
            }
            closeStage();
        }
    }

    private void reloadEmployee() {
        currentEmployee = employeeDao.findById(employeeId).orElse(null);
        if (currentEmployee != null) {
            populate(currentEmployee);
        }
    }

    private void populate(Employee e) {
        nameLabel.setText(e.getFirstName() + " " + e.getLastName());
        codeLabel.setText(e.getEmployeeCode() != null ? e.getEmployeeCode() : "—");
        statusBadge.setText(e.getStatus() != null ? e.getStatus().name() : "—");
        if (deleteButton != null) {
            boolean inactive = e.getStatus() == EmployeeStatus.INACTIVE;
            deleteButton.setText(inactive ? "Activate" : "Deactivate");
            deleteButton.setStyle(inactive ? GREEN_BUTTON_STYLE : RED_BUTTON_STYLE);
        }
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
    private void onDeactivateEmployee() {
        if (currentEmployee == null) {
            return;
        }
        boolean inactive = currentEmployee.getStatus() == EmployeeStatus.INACTIVE;
        String targetStatus = inactive ? "ACTIVE" : "INACTIVE";
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle((inactive ? "Activate" : "Deactivate") + " employee");
        confirm.setHeaderText(null);
        confirm.setContentText("Set this employee status to " + targetStatus + "?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }
        boolean updated = inactive ? employeeDao.activate(employeeId) : employeeDao.deactivate(employeeId);
        if (updated) {
            if (onDataChanged != null) {
                onDataChanged.run();
            }
            ViewNavigator.showInformation("Employees", "Employee is now " + targetStatus + ".");
            reloadEmployee();
        } else {
            ViewNavigator.showInformation("Employees", "Could not update employee status.");
        }
    }

    @FXML
    private void onEditEmployee() {
        if (currentEmployee == null) {
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Update employee");
        dialog.setHeaderText(null);

        TextField firstNameField = new TextField(currentEmployee.getFirstName());
        TextField lastNameField = new TextField(currentEmployee.getLastName());
        TextField emailField = new TextField(currentEmployee.getEmail());
        TextField phoneField = new TextField(currentEmployee.getPhone());
        TextField positionField = new TextField(currentEmployee.getPosition());
        ComboBox<String> departmentCombo = new ComboBox<>(
                DepartmentOptions.observableListWithOptionalExtra(currentEmployee.getDepartment()));
        if (currentEmployee.getDepartment() != null) {
            departmentCombo.getSelectionModel().select(currentEmployee.getDepartment());
        }
        DatePicker hireDatePicker = new DatePicker(currentEmployee.getHireDate());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("First name"), firstNameField);
        grid.addRow(1, new Label("Last name"), lastNameField);
        grid.addRow(2, new Label("Email"), emailField);
        grid.addRow(3, new Label("Phone"), phoneField);
        grid.addRow(4, new Label("Department"), departmentCombo);
        grid.addRow(5, new Label("Position"), positionField);
        grid.addRow(6, new Label("Hire date"), hireDatePicker);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }
        if (firstNameField.getText() == null || firstNameField.getText().trim().isEmpty()
                || lastNameField.getText() == null || lastNameField.getText().trim().isEmpty()
                || emailField.getText() == null || emailField.getText().trim().isEmpty()) {
            ViewNavigator.showInformation("Employees", "First name, last name, and email are required.");
            return;
        }
        if (departmentCombo.getSelectionModel().getSelectedItem() == null) {
            ViewNavigator.showInformation("Employees", "Please select a department.");
            return;
        }

        currentEmployee.setFirstName(firstNameField.getText().trim());
        currentEmployee.setLastName(lastNameField.getText().trim());
        currentEmployee.setEmail(emailField.getText().trim());
        currentEmployee.setPhone(trimToNull(phoneField.getText()));
        currentEmployee.setDepartment(departmentCombo.getSelectionModel().getSelectedItem());
        currentEmployee.setPosition(trimToNull(positionField.getText()));
        currentEmployee.setHireDate(hireDatePicker.getValue());

        if (employeeDao.update(currentEmployee)) {
            if (currentEmployee.getHireDate() != null) {
                leaveBalanceDao.syncAccrualForEmployeeAllYears(employeeId, currentEmployee.getHireDate());
            }
            ViewNavigator.showInformation("Employees", "Employee updated.");
            if (onDataChanged != null) {
                onDataChanged.run();
            }
            reloadEmployee();
        } else {
            ViewNavigator.showInformation("Employees", "Could not update employee.");
        }
    }

    @FXML
    private void onViewLeaveBalance() {
        if (currentEmployee == null) {
            return;
        }
        if (currentEmployee.getHireDate() != null) {
            leaveBalanceDao.syncAccrualForEmployeeAllYears(employeeId, currentEmployee.getHireDate());
        }
        List<LeaveBalance> balances = leaveBalanceDao.findByEmployeeId(employeeId);
        if (balances.isEmpty()) {
            ViewNavigator.showInformation("Leave balance",
                    currentEmployee.getHireDate() == null
                            ? "No leave balance yet. Set the employee hire date to compute annual leave (30 days/year, 2.5 per month)."
                            : "No leave balance found for this employee.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/view/leave-balance-view.fxml"));
            Parent root = loader.load();
            LeaveBalanceController controller = loader.getController();
            controller.initData(currentEmployee, balances);
            Stage modal = new Stage();
            modal.setTitle("Leave balance");
            modal.initOwner(nameLabel.getScene().getWindow());
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setScene(new javafx.scene.Scene(root));
            modal.setResizable(false);
            modal.showAndWait();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot open leave balance view", e);
        }
    }

    @FXML
    private void onViewLeaveHistory() {
        if (currentEmployee == null) {
            return;
        }
        List<LeaveHistory> history = leaveHistoryDao.findByEmployeeId(employeeId);
        if (history.isEmpty()) {
            ViewNavigator.showInformation("Leave history", "No leave history found for this employee.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/view/leave-history-view.fxml"));
            Parent root = loader.load();
            LeaveHistoryController controller = loader.getController();
            controller.initData(currentEmployee, history);
            Stage modal = new Stage();
            modal.setTitle("Leave history");
            modal.initOwner(nameLabel.getScene().getWindow());
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setScene(new javafx.scene.Scene(root));
            modal.setResizable(false);
            modal.showAndWait();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot open leave history view", e);
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }

    private void closeStage() {
        Stage stage = (Stage) nameLabel.getScene().getWindow();
        stage.close();
    }
}
