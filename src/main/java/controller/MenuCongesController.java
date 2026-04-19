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
import util.LeaveLetterGenerator;
import util.ViewNavigator;

import java.io.IOException;
import java.nio.file.Path;
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
    private final LeaveLetterGenerator leaveLetterGenerator = new LeaveLetterGenerator();

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

        abonnementsButton.setDisable(true);
        notificationButton.setOnAction(event -> {
            long count = leaveRequestDao.findByStatus(LeaveRequestStatus.PENDING).size();
            ViewNavigator.showInformation("Notifications", count + " leave request(s) pending.");
        });
    }

    private void reloadFromDatabase() {
        masterList.clear();
        masterList.addAll(leaveRequestDao.findAll());
        applyFilterAndSearch();
    }

    private void applyFilterAndSearch() {
        String query = searchTextField.getText() == null ? "" : searchTextField.getText().trim().toLowerCase(Locale.ROOT);
        List<LeaveRequest> filtered = masterList.stream()
                .filter(request -> statusFilter == null || request.getStatus() == statusFilter)
                .filter(request -> matchesSearch(request, query))
                .toList();
        congesDisplayedItems.setAll(filtered);
        pageTitleText.setText("Gestion Des Conges (" + filtered.size() + ")");
    }

    private static boolean matchesSearch(LeaveRequest request, String query) {
        if (query.isEmpty()) {
            return true;
        }
        Employee employee = request.getEmployee();
        LeaveType leaveType = request.getLeaveType();
        return (employee != null && employee.getFirstName() != null && employee.getFirstName().toLowerCase(Locale.ROOT).contains(query))
                || (employee != null && employee.getLastName() != null && employee.getLastName().toLowerCase(Locale.ROOT).contains(query))
                || (employee != null && employee.getEmployeeCode() != null && employee.getEmployeeCode().toLowerCase(Locale.ROOT).contains(query))
                || (leaveType != null && leaveType.getName() != null && leaveType.getName().toLowerCase(Locale.ROOT).contains(query))
                || (request.getStatus() != null && request.getStatus().name().toLowerCase(Locale.ROOT).contains(query));
    }

    private void setupCustomListView() {
        congesListView.setCellFactory(param -> new ListCell<>() {
            private final HBox root = new HBox(15);
            private final Label label = new Label();
            private final Region spacer = new Region();
            private final Button actionBtn = new Button("Action");
            private final Button generateLetterBtn = new Button("Generate Letter");
            private final Button modifyBtn = new Button("Modify");
            private final Button deleteBtn = new Button("Delete");

            {
                root.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(spacer, Priority.ALWAYS);
                root.setPadding(new Insets(5, 10, 5, 10));
                actionBtn.setId("action-button");
                generateLetterBtn.setId("action-button");
                modifyBtn.setId("modify-button");
                deleteBtn.setId("delete-button");
                root.getChildren().addAll(label, spacer, actionBtn, generateLetterBtn, modifyBtn, deleteBtn);

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

                generateLetterBtn.setOnAction(event -> {
                    LeaveRequest item = getItem();
                    if (item != null) {
                        handleGenerateLetter(item);
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
                    return;
                }

                setText(null);
                Employee employee = item.getEmployee();
                LeaveType leaveType = item.getLeaveType();
                String employeeLabel = employee == null
                        ? "?"
                        : employee.getEmployeeCode() + " - " + employee.getFirstName() + " " + employee.getLastName();
                String typeLabel = leaveType == null ? "?" : leaveType.getName();
                label.setText("REQ" + item.getId() + " - " + employeeLabel + " - " + typeLabel + " - " + item.getStatus());

                boolean approved = item.getStatus() == LeaveRequestStatus.APPROVED;
                generateLetterBtn.setManaged(approved);
                generateLetterBtn.setVisible(approved);
                setGraphic(root);
            }
        });
    }

    private void handleGenerateLetter(LeaveRequest request) {
        if (request.getStatus() != LeaveRequestStatus.APPROVED) {
            ViewNavigator.showInformation("Leave letter", "A letter can only be generated for approved requests.");
            return;
        }

        LocalDate approvalDate = leaveHistoryDao
                .findLatestActionDateByRequestId(request.getId(), "REQUEST_APPROVED")
                .orElse(LocalDate.now());
        try {
            leaveLetterGenerator.generateApprovedLeaveLetter(request, approvalDate);
        } catch (IllegalStateException exception) {
            ViewNavigator.showInformation("Leave letter", exception.getMessage());
        }
    }

    private void handleAction(LeaveRequest request) {
        if (request.getStatus() != LeaveRequestStatus.PENDING) {
            showReadOnlyActions(request);
            return;
        }
        Long adminId = SessionContext.getCurrentAdmin()
                .map(admin -> admin.getId())
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
            long employeeId = request.getEmployee().getId();
            long duration = request.calculateDuration();
            LocalDate hireDate = request.getEmployee().getHireDate();
            if (hireDate == null) {
                hireDate = employeeDao.findById(employeeId).map(Employee::getHireDate).orElse(null);
            }
            if (hireDate != null) {
                leaveBalanceDao.syncAccrualForEmployeeAllYears(employeeId, hireDate);
            }
            if (duration > 0) {
                double available = leaveBalanceDao.getTotalAvailableDays(employeeId);
                if (duration > available + 1e-6) {
                    ViewNavigator.showInformation(
                            "Leave balance",
                            "Not enough leave days. Available (all years, carryover included): "
                                    + String.format("%.2f", available)
                                    + ". Requested: " + duration + "."
                    );
                    return;
                }
            }
            if (leaveRequestDao.updateStatus(request.getId(), LeaveRequestStatus.APPROVED, adminId, null)) {
                if (duration > 0) {
                    leaveBalanceDao.applyApprovedLeaveDays(
                            employeeId,
                            request.getStartDate().getYear(),
                            duration,
                            hireDate
                    );
                }
                recordHistory(request, "REQUEST_APPROVED", "Admin approved the leave request");
                reloadFromDatabase();
                ViewNavigator.showInformation("Leave request", "Request approved.");
            }
        } else if (choice.get().equals(reject)) {
            TextInputDialog commentDialog = new TextInputDialog();
            commentDialog.setTitle("Reject request");
            commentDialog.setHeaderText(null);
            commentDialog.setContentText("Rejection comment (optional):");
            Optional<String> comment = commentDialog.showAndWait();
            String finalComment = comment.map(String::trim).filter(text -> !text.isEmpty()).orElse("Rejected by admin");
            if (leaveRequestDao.updateStatus(request.getId(), LeaveRequestStatus.REJECTED, adminId, finalComment)) {
                recordHistory(request, "REQUEST_REJECTED", finalComment);
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
        LocalDate hireDate = employee.getHireDate();
        if (hireDate == null) {
            hireDate = employeeDao.findById(employee.getId()).map(Employee::getHireDate).orElse(null);
        }
        if (hireDate != null) {
            leaveBalanceDao.syncAccrualForEmployeeAllYears(employee.getId(), hireDate);
        }
        List<LeaveBalance> balances = leaveBalanceDao.findByEmployeeId(employee.getId());
        if (balances.isEmpty()) {
            ViewNavigator.showInformation(
                    "Leave balance",
                    hireDate == null
                            ? "No leave balance yet. Set the employee hire date to compute annual leave (30 days/year, 2.5 per month)."
                            : "No leave balance found for this employee."
            );
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
        employeeCombo.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Employee item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getEmployeeCode() + " - " + item.getFirstName() + " " + item.getLastName());
            }
        });
        employeeCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Employee item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getEmployeeCode() + " - " + item.getFirstName() + " " + item.getLastName());
            }
        });

        ComboBox<LeaveType> typeCombo = new ComboBox<>(FXCollections.observableArrayList(leaveTypeDao.findAll()));
        typeCombo.setCellFactory(listView -> new ListCell<>() {
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

        Employee employee = employeeCombo.getSelectionModel().getSelectedItem();
        LeaveType leaveType = typeCombo.getSelectionModel().getSelectedItem();
        LocalDate start = startPicker.getValue();
        LocalDate end = endPicker.getValue();
        if (employee == null || leaveType == null || start == null || end == null) {
            ViewNavigator.showInformation("Leave request", "Please fill all required fields.");
            return;
        }
        if (end.isBefore(start)) {
            ViewNavigator.showInformation("Leave request", "End date cannot be before start date.");
            return;
        }

        LeaveRequest request = new LeaveRequest();
        request.setEmployee(employee);
        request.setLeaveType(leaveType);
        request.setRequestDate(LocalDate.now());
        request.setStartDate(start);
        request.setEndDate(end);
        String reason = reasonArea.getText() == null ? "" : reasonArea.getText().trim();
        request.setReason(reason.isEmpty() ? null : reason);
        request.setStatus(LeaveRequestStatus.PENDING);
        request.setProcessedBy(null);
        request.setRejectionComment(null);

        long newId = leaveRequestDao.insert(request);
        LeaveHistory history = new LeaveHistory();
        history.setEmployee(employee);
        LeaveRequest reference = new LeaveRequest();
        reference.setId(newId);
        history.setLeaveRequest(reference);
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
        typeCombo.setCellFactory(listView -> new ListCell<>() {
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
        TextArea reasonArea = new TextArea(request.getReason() == null ? "" : request.getReason());
        reasonArea.setPrefRowCount(3);
        reasonArea.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Leave type"), typeCombo);
        grid.addRow(1, new Label("Start"), start);
        grid.addRow(2, new Label("End"), end);
        grid.addRow(3, new Label("Reason"), reasonArea);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        LeaveType selected = typeCombo.getSelectionModel().getSelectedItem();
        LocalDate startDate = start.getValue();
        LocalDate endDate = end.getValue();
        if (selected == null || startDate == null || endDate == null) {
            ViewNavigator.showInformation("Modify", "Please fill all fields.");
            return;
        }
        if (endDate.isBefore(startDate)) {
            ViewNavigator.showInformation("Modify", "End date cannot be before start date.");
            return;
        }

        String reason = reasonArea.getText() == null ? "" : reasonArea.getText().trim();
        if (leaveRequestDao.updatePendingDetails(request.getId(), selected.getId(), startDate, endDate, reason.isEmpty() ? null : reason)) {
            recordHistory(request, "REQUEST_UPDATED", "Request details were updated");
            reloadFromDatabase();
            ViewNavigator.showInformation("Modify", "Request updated.");
        } else {
            ViewNavigator.showInformation("Modify", "Could not update (request may no longer be pending).");
        }
    }

    private void recordHistory(LeaveRequest request, String action, String note) {
        LeaveHistory history = new LeaveHistory();
        history.setEmployee(request.getEmployee());
        LeaveRequest reference = new LeaveRequest();
        reference.setId(request.getId());
        history.setLeaveRequest(reference);
        history.setAction(action);
        history.setActionDate(LocalDate.now());
        history.setNote(note);
        leaveHistoryDao.insert(history);
    }
}
