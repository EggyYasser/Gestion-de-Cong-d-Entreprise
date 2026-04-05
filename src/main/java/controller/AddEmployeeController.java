package controller;

import dao.EmployeeDao;
import enums.EmployeeStatus;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Employee;
import util.ViewNavigator;

import java.sql.SQLIntegrityConstraintViolationException;

public class AddEmployeeController {

    private final EmployeeDao employeeDao = new EmployeeDao();

    @FXML
    private Button addEAddAnotherButton;

    @FXML
    private Button addEApproveButton;

    @FXML
    private Label addEBirthDateLabel;

    @FXML
    private DatePicker addEBirthDatePicker;

    @FXML
    private HBox addEButtonBox;

    @FXML
    private Button addECancelButton;

    @FXML
    private ComboBox<String> addEDepartmentCombo;

    @FXML
    private Label addEDepartmentLabel;

    @FXML
    private TextField addEEmailField;

    @FXML
    private Label addEEmailLabel;

    @FXML
    private TextField addEFirstNameField;

    @FXML
    private Label addEFirstNameLabel;

    @FXML
    private GridPane addEGridPane;

    @FXML
    private Label addEHeaderLabel;

    @FXML
    private Label addEHireDateLabel;

    @FXML
    private DatePicker addEHireDatePicker;

    @FXML
    private TextField addELastNameField;

    @FXML
    private Label addELastNameLabel;

    @FXML
    private TextField addEPhoneField;

    @FXML
    private Label addEPhoneLabel;

    @FXML
    private TextField addEPositionField;

    @FXML
    private Label addEPositionLabel;

    @FXML
    private VBox addEVBox;

    @FXML
    private void initialize() {
        addEDepartmentCombo.setItems(FXCollections.observableArrayList(
                "Human Resources",
                "Finance",
                "IT",
                "Marketing"
        ));

        addECancelButton.setOnAction(event -> closeWindow());
        addEAddAnotherButton.setOnAction(event -> {
            clearForm();
            ViewNavigator.showInformation("Add Employee", "Form cleared. You can add another employee.");
        });
        addEApproveButton.setOnAction(event -> handleApprove());
    }

    private void handleApprove() {
        if (isBlank(addEFirstNameField) || isBlank(addELastNameField) || isBlank(addEEmailField)) {
            ViewNavigator.showInformation("Add Employee", "Please fill in first name, last name, and email.");
            return;
        }
        if (addEDepartmentCombo.getSelectionModel().getSelectedItem() == null) {
            ViewNavigator.showInformation("Add Employee", "Please select a department.");
            return;
        }

        Employee employee = new Employee();
        employee.setFirstName(addEFirstNameField.getText().trim());
        employee.setLastName(addELastNameField.getText().trim());
        employee.setBirthDate(addEBirthDatePicker.getValue());
        employee.setHireDate(addEHireDatePicker.getValue());
        employee.setDepartment(addEDepartmentCombo.getSelectionModel().getSelectedItem());
        employee.setPosition(trimOrNull(addEPositionField.getText()));
        employee.setEmail(addEEmailField.getText().trim());
        employee.setPhone(trimOrNull(addEPhoneField.getText()));
        employee.setStatus(EmployeeStatus.ACTIVE);

        try {
            employeeDao.insert(employee);
            ViewNavigator.showInformation("Add Employee", "Employee saved as " + employee.getEmployeeCode() + ".");
            closeWindow();
        } catch (IllegalStateException ex) {
            if (isDuplicateKeyError(ex)) {
                ViewNavigator.showInformation("Add Employee", "Email or employee code already exists. Use a different email.");
            } else {
                ViewNavigator.showInformation("Add Employee", "Could not save: " + ex.getMessage());
            }
        }
    }

    private static boolean isDuplicateKeyError(IllegalStateException ex) {
        for (Throwable t = ex.getCause(); t != null; t = t.getCause()) {
            if (t instanceof SQLIntegrityConstraintViolationException) {
                return true;
            }
        }
        return false;
    }

    private static String trimOrNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private boolean isBlank(TextField textField) {
        return textField.getText() == null || textField.getText().trim().isEmpty();
    }

    private void clearForm() {
        addEFirstNameField.clear();
        addELastNameField.clear();
        addEBirthDatePicker.setValue(null);
        addEHireDatePicker.setValue(null);
        addEDepartmentCombo.getSelectionModel().clearSelection();
        addEPositionField.clear();
        addEEmailField.clear();
        addEPhoneField.clear();
    }

    private void closeWindow() {
        Stage stage = (Stage) addEVBox.getScene().getWindow();
        stage.close();
    }
}
