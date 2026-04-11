package controller;

import com.gestionconges.Main;
import com.gestionconges.SessionContext;
import dao.EmployeeDao;
import dao.LeaveBalanceDao;
import dao.LeaveHistoryDao;
import dao.LeaveRequestDao;
import dao.LeaveTypeDao;
import enums.LeaveRequestStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Employee;
import model.LeaveBalance;
import model.LeaveHistory;
import model.LeaveRequest;
import model.LeaveType;
import util.ViewNavigator;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class MenuCongesController {

    private final LeaveRequestDao leaveRequestDao = new LeaveRequestDao();
    private final LeaveHistoryDao leaveHistoryDao = new LeaveHistoryDao();
    private final LeaveBalanceDao leaveBalanceDao = new LeaveBalanceDao();
    private final EmployeeDao employeeDao = new EmployeeDao();
    private final LeaveTypeDao leaveTypeDao = new LeaveTypeDao();

    private final List<LeaveRequest> masterList = new ArrayList<>();
    private final ObservableList<LeaveRequest> congesDisplayedItems = FXCollections.observableArrayList();
    private LeaveRequestStatus statusFilter;

    @FXML
    private Button abonnementsButton;
    @FXML
    private Button addRequestButton;
    @FXML
    private Button allFilterButton;
    @FXML
    private Button approvedFilterButton;
    @FXML
    private ListView<LeaveRequest> congesListView;
    @FXML
    private HBox contentAreaHBox;
    @FXML
    private HBox headerHBox;
    @FXML
    private HBox topBarHBox;
    @FXML
    private Button congeesButton;
    @FXML
    private Button dashboardButton;
    @FXML
    private Button employeesButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Button notificationButton;
    @FXML
    private Button pendingFilterButton;
    @FXML
    private Button rejectedFilterButton;
    @FXML
    private Button supportButton;
    @FXML
    private Label filtersLabel;
    @FXML
    private VBox filtersVBox;
    @FXML
    private VBox mainContentVBox;
    @FXML
    private ImageView logoImageView;
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private Text pageTitleText;
    @FXML
    private ComboBox<String> profileComboBox;
    @FXML
    private TextField searchTextField;

    @FXML
    private void initialize() {
        profileComboBox.setItems(FXCollections.observableArrayList("Admin"));
        profileComboBox.getSelectionModel().selectFirst();

        setupCustomListView();
        congesListView.setItems(congesDisplayedItems);
        reloadFromDatabase();

        searchTextField.textProperty().addListener((o, a, b) -> applyFilterAndSearch());

        dashboardButton.setOnAction(event -> ViewNavigator.switchScene(dashboardButton, "/view/dashboard-view.fxml", "Dashboard"));
        employeesButton.setOnAction(event -> ViewNavigator.switchScene(employeesButton, "/view/menu-emlpoyees-view.fxml", "Employees"));
        congeesButton.setOnAction(event -> ViewNavigator.switchScene(congeesButton, "/view/menu-conges-view.fxml", "Leave Requests"));
        supportButton.setOnAction(event -> ViewNavigator.openModal(supportButton, "/view/support-view.fxml", "Support"));
        logoutButton.setOnAction(event -> ViewNavigator.logout(logoutButton));

        allFilterButton.setOnAction(event -> {
            statusFilter = null;
            applyFilterAndSearch();
        });
        pendingFilterButton.setOnAction(event -> {
            statusFilter = LeaveRequestStatus.PENDING;
            applyFilterAndSearch();
        });
        approvedFilterButton.setOnAction(event -> {
            statusFilter = LeaveRequestStatus.APPROVED;
            applyFilterAndSearch();
        });
        rejectedFilterButton.setOnAction(event -> {
            statusFilter = LeaveRequestStatus.REJECTED;
            applyFilterAndSearch();
        });

        addRequestButton.setOnAction(event -> {
            openCreateRequestDialogFallback();
            reloadFromDatabase();
        });

        // Not in current scope: keep visible, but no popup.
        abonnementsButton.setDisable(true);
        notificationButton.setOnAction(event -> {
            long n = leaveRequestDao.findByStatus(LeaveRequestStatus.PENDING).size();
            ViewNavigator.showInformation("Notifications", n + " leave request(s) pending.");
        });
    }

    private void reloadFromDatabase() {
        masterList.clear();
        masterList.addAll(leaveRequestDao.findAll());
        applyFilterAndSearch();
    }

    private void applyFilterAndSearch() {
        String q = searchTextField.getText() == null ? "" : searchTextField.getText().trim().toLowerCase(Locale.ROOT);
        List<LeaveRequest> base = masterList.stream()
                .filter(r -> statusFilter == null || r.getStatus() == statusFilter)
                .filter(r -> matchesSearch(r, q))
                .toList();
        congesDisplayedItems.setAll(base);
        pageTitleText.setText("Gestion Des Conges (" + base.size() + ")");
    }

    private static boolean matchesSearch(LeaveRequest r, String q) {
        if (q.isEmpty()) {
            return true;
        }
        Employee e = r.getEmployee();
        LeaveType t = r.getLeaveType();
        return (e != null && e.getFirstName() != null && e.getFirstName().toLowerCase(Locale.ROOT).contains(q))
                || (e != null && e.getLastName() != null && e.getLastName().toLowerCase(Locale.ROOT).contains(q))
                || (e != null && e.getEmployeeCode() != null && e.getEmployeeCode().toLowerCase(Locale.ROOT).contains(q))
                || (t != null && t.getName() != null && t.getName().toLowerCase(Locale.ROOT).contains(q))
                || (r.getStatus() != null && r.getStatus().name().toLowerCase(Locale.ROOT).contains(q));
    }

    private void setupCustomListView() {
        congesListView.setCellFactory(param -> new ListCell<>() {
            private final HBox root = new HBox(15);
            private final Label label = new Label();
            private final Region spacer = new Region();
            private final Button actionBtn = new Button("Action");
            private final Button modifyBtn = new Button("Modify");
            private final Button deleteBtn = new Button("Delete");

            {
                root.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(spacer, Priority.ALWAYS);
                root.setPadding(new Insets(5, 10, 5, 10));
                actionBtn.setId("action-button");
                modifyBtn.setId("modify-button");
                deleteBtn.setId("delete-button");
                root.getChildren().addAll(label, spacer, actionBtn, modifyBtn, deleteBtn);

                deleteBtn.setOnAction(event -> {
                    LeaveRequest item = getItem();
                    if (item == null || item.getId() == null) {
                        return;
                    }
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete request");
                    confirm.setHeaderText(null);
                    confirm.setContentText("Delete this leave request permanently?");
                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        if (leaveRequestDao.delete(item.getId())) {
                            reloadFromDatabase();
                        } else {
                            ViewNavigator.showInformation("Leave requests", "Could not delete request.");
                        }
                    }
                });

                actionBtn.setOnAction(event -> {
                    LeaveRequest item = getItem();
                    if (item != null) {
                        handleAction(item);
                    }
                });

                modifyBtn.setOnAction(event -> {
                    LeaveRequest item = getItem();
                    if (item != null) {
                        handleModify(item);
                    }
                });
            }

            @Override
            protected void updateItem(LeaveRequest item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null);
                    Employee e = item.getEmployee();
                    LeaveType t = item.getLeaveType();
                    String emp = e == null ? "?" : e.getEmployeeCode() + " " + e.getFirstName() + " " + e.getLastName();
                    String typeName = t == null ? "?" : t.getName();
                    label.setText("REQ" + item.getId() + " — " + emp + " — " + typeName + " — " + item.getStatus());
                    setGraphic(root);
                }
            }
        });
    }

    private void handleAction(LeaveRequest request) {
        if (request.getStatus() != LeaveRequestStatus.PENDING) {
            showReadOnlyActions(request);
            return;
        }
        Long adminId = SessionContext.getCurrentAdmin()
                .map(a -> a.getId())
                .orElse(null);
        if (adminId == null) {
            ViewNavigator.showInformation("Session", "No logged-in admin. Please log in again.");
            return;
        }

        ButtonType approve = new ButtonType("Approve", ButtonBar.ButtonData.OK_DONE);
        ButtonType reject = new ButtonType("Reject", ButtonBar.ButtonData.NO);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Process request");
        alert.setHeaderText(null);
        alert.setContentText("Approve or reject this leave request?");
        alert.getButtonTypes().setAll(approve, reject, cancel);
        Optional<ButtonType> choice = alert.showAndWait();
        if (choice.isEmpty() || choice.get().equals(cancel)) {
            return;
        }
        if (choice.get().equals(approve)) {
            long empId = request.getEmployee().getId();
            long duration = request.calculateDuration();
            LocalDate hire = request.getEmployee().getHireDate();
            if (hire == null) {
                hire = employeeDao.findById(empId).map(Employee::getHireDate).orElse(null);
            }
            if (hire != null) {
                leaveBalanceDao.syncAccrualForEmployeeAllYears(empId, hire);
            }
            if (duration > 0) {
                double available = leaveBalanceDao.getTotalAvailableDays(empId);
                if (duration > available + 1e-6) {
                    ViewNavigator.showInformation("Leave balance",
                            "Not enough leave days. Available (all years, carryover included): "
                                    + String.format("%.2f", available)
                                    + ". Requested: " + duration + ".");
                    return;
                }
            }
            if (leaveRequestDao.updateStatus(request.getId(), LeaveRequestStatus.APPROVED, adminId, null)) {
                if (duration > 0) {
                    leaveBalanceDao.applyApprovedLeaveDays(
                            empId,
                            request.getStartDate().getYear(),
                            duration,
                            hire
                    );
                }
                recordHistory(request, "REQUEST_APPROVED", "Admin approved the leave request");
                reloadFromDatabase();
                ViewNavigator.showInformation("Leave request", "Request approved.");
            }
        } else if (choice.get().equals(reject)) {
            TextInputDialog commentDlg = new TextInputDialog();
            commentDlg.setTitle("Reject request");
            commentDlg.setHeaderText(null);
            commentDlg.setContentText("Rejection comment (optional):");
            Optional<String> comment = commentDlg.showAndWait();
            String c = comment.map(String::trim).filter(s -> !s.isEmpty()).orElse("Rejected by admin");
            if (leaveRequestDao.updateStatus(request.getId(), LeaveRequestStatus.REJECTED, adminId, c)) {
                recordHistory(request, "REQUEST_REJECTED", c);
                reloadFromDatabase();
                ViewNavigator.showInformation("Leave request", "Request rejected.");
            }
        }
    }

    private void showReadOnlyActions(LeaveRequest request) {
        ButtonType viewBalance = new ButtonType("View Balance");
        ButtonType viewHistory = new ButtonType("View History");
        ButtonType close = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Leave request");
        info.setHeaderText(null);
        info.setContentText("This request is " + request.getStatus() + ".");
        info.getButtonTypes().setAll(viewBalance, viewHistory, close);
        Optional<ButtonType> choice = info.showAndWait();
        if (choice.isEmpty()) {
            return;
        }
        if (choice.get().equals(viewBalance)) {
            openBalanceModal(request.getEmployee());
        } else if (choice.get().equals(viewHistory)) {
            openHistoryModal(request.getEmployee());
        }
    }

    private void openBalanceModal(Employee employee) {
        LocalDate hire = employee.getHireDate();
        if (hire == null) {
            hire = employeeDao.findById(employee.getId()).map(Employee::getHireDate).orElse(null);
        }
        if (hire != null) {
            leaveBalanceDao.syncAccrualForEmployeeAllYears(employee.getId(), hire);
        }
        List<LeaveBalance> balances = leaveBalanceDao.findByEmployeeId(employee.getId());
        if (balances.isEmpty()) {
            ViewNavigator.showInformation("Leave balance",
                    hire == null
                            ? "No leave balance yet. Set the employee hire date to compute annual leave (30 days/year, 2.5 per month)."
                            : "No leave balance found for this employee.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/view/leave-balance-view.fxml"));
            Parent root = loader.load();
            LeaveBalanceController controller = loader.getController();
            controller.initData(employee, balances);
            Stage modal = new Stage();
            modal.setTitle("Leave balance");
            modal.initOwner(mainBorderPane.getScene().getWindow());
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setScene(new javafx.scene.Scene(root));
            modal.setResizable(false);
            modal.showAndWait();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot open leave balance view", e);
        }
    }

    private void openHistoryModal(Employee employee) {
        List<LeaveHistory> history = leaveHistoryDao.findByEmployeeId(employee.getId());
        if (history.isEmpty()) {
            ViewNavigator.showInformation("Leave history", "No leave history found for this employee.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/view/leave-history-view.fxml"));
            Parent root = loader.load();
            LeaveHistoryController controller = loader.getController();
            controller.initData(employee, history);
            Stage modal = new Stage();
            modal.setTitle("Leave history");
            modal.initOwner(mainBorderPane.getScene().getWindow());
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setScene(new javafx.scene.Scene(root));
            modal.setResizable(false);
            modal.showAndWait();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot open leave history view", e);
        }
    }

    private void openCreateRequestDialogFallback() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("New leave request");
        dialog.setHeaderText(null);

        ComboBox<Employee> employeeCombo = new ComboBox<>(FXCollections.observableArrayList(employeeDao.findAll()));
        employeeCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Employee item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getEmployeeCode() + " — " + item.getFirstName() + " " + item.getLastName());
            }
        });
        employeeCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Employee item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getEmployeeCode() + " — " + item.getFirstName() + " " + item.getLastName());
            }
        });

        ComboBox<LeaveType> typeCombo = new ComboBox<>(FXCollections.observableArrayList(leaveTypeDao.findAll()));
        typeCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(LeaveType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        typeCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(LeaveType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        DatePicker startPicker = new DatePicker();
        DatePicker endPicker = new DatePicker();
        TextArea reasonArea = new TextArea();
        reasonArea.setPrefRowCount(3);
        reasonArea.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Employee"), employeeCombo);
        grid.addRow(1, new Label("Leave type"), typeCombo);
        grid.addRow(2, new Label("Start date"), startPicker);
        grid.addRow(3, new Label("End date"), endPicker);
        grid.addRow(4, new Label("Reason"), reasonArea);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        Employee emp = employeeCombo.getSelectionModel().getSelectedItem();
        LeaveType type = typeCombo.getSelectionModel().getSelectedItem();
        LocalDate start = startPicker.getValue();
        LocalDate end = endPicker.getValue();
        if (emp == null || type == null || start == null || end == null) {
            ViewNavigator.showInformation("Leave request", "Please fill all required fields.");
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
        req.setProcessedBy(null);
        req.setRejectionComment(null);

        long newId = leaveRequestDao.insert(req);
        LeaveHistory history = new LeaveHistory();
        history.setEmployee(emp);
        LeaveRequest ref = new LeaveRequest();
        ref.setId(newId);
        history.setLeaveRequest(ref);
        history.setAction("REQUEST_CREATED");
        history.setActionDate(LocalDate.now());
        history.setNote("Leave request created from leave page fallback dialog");
        leaveHistoryDao.insert(history);
        ViewNavigator.showInformation("Leave request", "Request submitted.");
    }

    private void handleModify(LeaveRequest request) {
        if (request.getStatus() != LeaveRequestStatus.PENDING) {
            ViewNavigator.showInformation("Modify", "Only pending requests can be edited.");
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modify leave request");
        dialog.setHeaderText(null);

        ComboBox<LeaveType> typeCombo = new ComboBox<>(FXCollections.observableArrayList(leaveTypeDao.findAll()));
        typeCombo.getSelectionModel().select(request.getLeaveType());
        typeCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(LeaveType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        typeCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(LeaveType item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        DatePicker start = new DatePicker(request.getStartDate());
        DatePicker end = new DatePicker(request.getEndDate());
        TextArea reason = new TextArea(request.getReason() == null ? "" : request.getReason());
        reason.setPrefRowCount(3);
        reason.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Leave type"), typeCombo);
        grid.addRow(1, new Label("Start"), start);
        grid.addRow(2, new Label("End"), end);
        grid.addRow(3, new Label("Reason"), reason);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }
        LeaveType selected = typeCombo.getSelectionModel().getSelectedItem();
        LocalDate s = start.getValue();
        LocalDate e = end.getValue();
        if (selected == null || s == null || e == null) {
            ViewNavigator.showInformation("Modify", "Please fill all fields.");
            return;
        }
        if (e.isBefore(s)) {
            ViewNavigator.showInformation("Modify", "End date cannot be before start date.");
            return;
        }
        String r = reason.getText() == null ? "" : reason.getText().trim();
        if (leaveRequestDao.updatePendingDetails(request.getId(), selected.getId(), s, e, r.isEmpty() ? null : r)) {
            recordHistory(request, "REQUEST_UPDATED", "Request details were updated");
            reloadFromDatabase();
            ViewNavigator.showInformation("Modify", "Request updated.");
        } else {
            ViewNavigator.showInformation("Modify", "Could not update (request may no longer be pending).");
        }
    }

    private void recordHistory(LeaveRequest request, String action, String note) {
        LeaveHistory h = new LeaveHistory();
        h.setEmployee(request.getEmployee());
        LeaveRequest ref = new LeaveRequest();
        ref.setId(request.getId());
        h.setLeaveRequest(ref);
        h.setAction(action);
        h.setActionDate(LocalDate.now());
        h.setNote(note);
        leaveHistoryDao.insert(h);
    }
}
