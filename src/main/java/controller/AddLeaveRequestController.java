package controller;

import dao.EmployeeDao;
import dao.LeaveHistoryDao;
import dao.LeaveRequestDao;
import dao.LeaveTypeDao;
import enums.LeaveRequestStatus;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Employee;
import model.LeaveHistory;
import model.LeaveRequest;
import model.LeaveType;
import util.ViewNavigator;

import java.time.LocalDate;

public class AddLeaveRequestController {

    private final EmployeeDao employeeDao = new EmployeeDao();
    private final LeaveTypeDao leaveTypeDao = new LeaveTypeDao();
    private final LeaveRequestDao leaveRequestDao = new LeaveRequestDao();
    private final LeaveHistoryDao leaveHistoryDao = new LeaveHistoryDao();

    @FXML
    private Button cancelButton;

    @FXML
    private ComboBox<Employee> employeeCombo;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private ComboBox<LeaveType> leaveTypeCombo;

    @FXML
    private TextArea reasonArea;

    @FXML
    private VBox rootVBox;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private Button submitButton;

    @FXML
    private void initialize() {
        employeeCombo.setItems(FXCollections.observableArrayList(employeeDao.findAll()));
        leaveTypeCombo.setItems(FXCollections.observableArrayList(leaveTypeDao.findAll()));
        employeeCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Employee item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : item.getEmployeeCode() + " — " + item.getFirstName() + " " + item.getLastName());
            }
        });
        employeeCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Employee item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : item.getEmployeeCode() + " — " + item.getFirstName() + " " + item.getLastName());
            }
        });
        leaveTypeCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(LeaveType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        leaveTypeCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(LeaveType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
    }

    @FXML
    private void onCancel() {
        close();
    }

    @FXML
    private void onSubmit() {
        Employee emp = employeeCombo.getSelectionModel().getSelectedItem();
        LeaveType type = leaveTypeCombo.getSelectionModel().getSelectedItem();
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        if (emp == null || type == null || start == null || end == null) {
            ViewNavigator.showInformation("Leave request", "Please select employee, leave type, start date, and end date.");
            return;
        }
        if (end.isBefore(start)) {
            ViewNavigator.showInformation("Leave request", "End date cannot be before start date.");
            return;
        }
        LeaveRequest req = new LeaveRequest();
        req.setEmployee(emp);
        req.setLeaveType(type);
        req.setRequestDate(LocalDate.now());
        req.setStartDate(start);
        req.setEndDate(end);
        String reason = reasonArea.getText() == null ? "" : reasonArea.getText().trim();
        req.setReason(reason.isEmpty() ? null : reason);
        req.setStatus(LeaveRequestStatus.PENDING);
        req.setRejectionComment(null);
        req.setProcessedBy(null);
        try {
            long newId = leaveRequestDao.insert(req);
            LeaveRequest ref = new LeaveRequest();
            ref.setId(newId);
            LeaveHistory history = new LeaveHistory();
            history.setEmployee(emp);
            history.setLeaveRequest(ref);
            history.setAction("REQUEST_CREATED");
            history.setActionDate(LocalDate.now());
            history.setNote("Leave request created from application");
            leaveHistoryDao.insert(history);
            ViewNavigator.showInformation("Leave request", "Request submitted.");
            close();
        } catch (RuntimeException ex) {
            ViewNavigator.showInformation("Leave request", "Could not save: " + ex.getMessage());
        }
    }

    private void close() {
        Stage stage = (Stage) rootVBox.getScene().getWindow();
        stage.close();
    }
}
