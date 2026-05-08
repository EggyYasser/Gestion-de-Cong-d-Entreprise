package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import dao.EmployeeDao;
import dao.LeaveRequestDao;
import enums.EmployeeStatus;
import enums.LeaveRequestStatus;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import model.Employee;
import model.LeaveRequest;
import model.LeaveType;
import util.ViewNavigator;
import java.time.YearMonth;
import java.util.List;

public class DashboardController {

    private final EmployeeDao employeeDao = new EmployeeDao();
    private final LeaveRequestDao leaveRequestDao = new LeaveRequestDao();

    @FXML
    private Button btnDashboard;

    @FXML
    private Button btnEmployees;

    @FXML
    private Button btnLogout;

    @FXML
    private Button btnNotifications;

    @FXML
    private Button btnSupport;
    @FXML
    private Button recentAllButton;
    @FXML
    private Button recentPendingButton;
    @FXML
    private Button recentApprovedButton;
    @FXML
    private Button recentRejectedButton;

    @FXML
    private VBox cardActiveEmployees;

    @FXML
    private VBox cardLeaveStats;

    @FXML
    private VBox cardPendingLeaves;

    @FXML
    private VBox cardRecentRequests;

    @FXML
    private VBox cardTotalEmployees;

    @FXML
    private ComboBox<String> comboProfile;

    @FXML
    private Button congesButton;

    @FXML
    private Label lblTrendActive;

    @FXML
    private Label lblTrendPending;

    @FXML
    private Label lblTrendTotal;

    @FXML
    private StackPane logoIcon;

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private VBox mainContentVBox;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox sidebarVBox;

    @FXML
    private HBox topBarHBox;

    @FXML
    private Text txtEmptyState;

    @FXML
    private Text txtPageTitle;

    @FXML
    private Text txtWelcomeMsg;

    @FXML
    private VBox recentRequestsList;
    @FXML
    private ScrollPane recentRequestsScrollPane;

    @FXML
    private Label valActiveEmployees;

    @FXML
    private Label valPendingLeaves;

    @FXML
    private Label valStatApproved;

    @FXML
    private Label valStatRejected;

    @FXML
    private Label valTotalEmployees;

    @FXML
    private Label valApprovedThisMonth;

    @FXML
    private Label valRejectedThisMonth;

    private Timeline statsAutoRefreshTimeline;
    private LeaveRequestStatus recentStatusFilter;

    private void refreshStats() {
        try {
            var employees = employeeDao.findAll();
            long total = employees.size();
            long active = employees.stream().filter(e -> e.getStatus() == EmployeeStatus.ACTIVE).count();
            valTotalEmployees.setText(String.valueOf(total));
            valActiveEmployees.setText(String.valueOf(active));

            long pending = leaveRequestDao.findByStatus(LeaveRequestStatus.PENDING).size();
            long approved = leaveRequestDao.findByStatus(LeaveRequestStatus.APPROVED).size();
            long rejected = leaveRequestDao.findByStatus(LeaveRequestStatus.REJECTED).size();
            long approvedThisMonth = leaveRequestDao.countByStatusInRequestMonth(LeaveRequestStatus.APPROVED, YearMonth.now());
            long rejectedThisMonth = leaveRequestDao.countByStatusInRequestMonth(LeaveRequestStatus.REJECTED, YearMonth.now());
            valPendingLeaves.setText(String.valueOf(pending));
            valStatApproved.setText(String.valueOf(approved));
            valStatRejected.setText(String.valueOf(rejected));
            valApprovedThisMonth.setText(String.valueOf(approvedThisMonth));
            valRejectedThisMonth.setText(String.valueOf(rejectedThisMonth));
            lblTrendPending.setText(pending + " waiting");
            lblTrendTotal.setText(total + " employees");
            lblTrendActive.setText(active + " active");

            List<LeaveRequest> recent = leaveRequestDao.findRecent(20);
            updateRecentRequestsCard(recent);
        } catch (RuntimeException ex) {
            valTotalEmployees.setText("—");
            valActiveEmployees.setText("—");
            valPendingLeaves.setText("—");
            valStatApproved.setText("—");
            valStatRejected.setText("—");
            valApprovedThisMonth.setText("—");
            valRejectedThisMonth.setText("—");
            lblTrendTotal.setText("DB error");
            lblTrendActive.setText("");
            lblTrendPending.setText(ex.getMessage() != null ? ex.getMessage() : "Could not load stats");
            updateRecentRequestsCard(List.of());
        }
    }

    private void updateRecentRequestsCard(List<LeaveRequest> recentRequests) {
        recentRequestsList.getChildren().clear();
        List<LeaveRequest> filtered = recentRequests == null
                ? List.of()
                : recentRequests.stream()
                .filter(request -> recentStatusFilter == null || request.getStatus() == recentStatusFilter)
                .limit(6)
                .toList();
        boolean hasData = !filtered.isEmpty();
        txtEmptyState.setVisible(!hasData);
        txtEmptyState.setManaged(!hasData);
        if (!hasData) {
            return;
        }
        for (LeaveRequest request : filtered) {
            HBox row = new HBox(10);
            row.getStyleClass().add("recent-request-row");
            VBox textBox = new VBox(2);
            Label title = new Label(buildRecentRequestTitle(request));
            title.getStyleClass().add("recent-request-title");
            Label meta = new Label(buildRecentRequestMeta(request));
            meta.getStyleClass().add("recent-request-meta");
            textBox.getChildren().addAll(title, meta);
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            HBox.setHgrow(textBox, Priority.ALWAYS);

            Label status = new Label(request.getStatus() == null ? "UNKNOWN" : request.getStatus().name());
            status.getStyleClass().add("recent-request-status");
            if (request.getStatus() == LeaveRequestStatus.APPROVED) {
                status.getStyleClass().add("recent-status-approved");
            } else if (request.getStatus() == LeaveRequestStatus.REJECTED) {
                status.getStyleClass().add("recent-status-rejected");
            } else {
                status.getStyleClass().add("recent-status-pending");
            }
            row.getChildren().addAll(textBox, spacer, status);
            row.setOnMouseClicked(event ->
                    ViewNavigator.switchScene(congesButton, "/view/menu-conges-view.fxml", "Leave Requests"));
            recentRequestsList.getChildren().add(row);
        }
    }

    private String buildRecentRequestTitle(LeaveRequest request) {
        Employee employee = request.getEmployee();
        LeaveType leaveType = request.getLeaveType();
        String employeeName = employee == null ? "Unknown employee" : ((nullToEmpty(employee.getFirstName()) + " " + nullToEmpty(employee.getLastName())).trim());
        String type = leaveType != null && leaveType.getName() != null ? leaveType.getName() : "Leave";
        return employeeName + " - " + type;
    }

    private String buildRecentRequestMeta(LeaveRequest request) {
        Employee employee = request.getEmployee();
        String employeeCode = employee != null && employee.getEmployeeCode() != null ? employee.getEmployeeCode() : "EMP";
        String requestCode = request.getId() == null ? "REQ" : ("REQ" + request.getId());
        String date = request.getStartDate() != null ? request.getStartDate().toString() : "n/a";
        return requestCode + " | " + employeeCode + " | Start: " + date;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void setRecentFilter(LeaveRequestStatus filter) {
        this.recentStatusFilter = filter;
        updateRecentFilterButtonStyles();
        updateRecentRequestsCard(leaveRequestDao.findRecent(20));
    }

    private void updateRecentFilterButtonStyles() {
        recentAllButton.getStyleClass().setAll("recent-filter-button");
        recentPendingButton.getStyleClass().setAll("recent-filter-button");
        recentApprovedButton.getStyleClass().setAll("recent-filter-button");
        recentRejectedButton.getStyleClass().setAll("recent-filter-button");
        if (recentStatusFilter == null) {
            recentAllButton.getStyleClass().add("recent-filter-active");
        } else if (recentStatusFilter == LeaveRequestStatus.PENDING) {
            recentPendingButton.getStyleClass().add("recent-filter-active");
        } else if (recentStatusFilter == LeaveRequestStatus.APPROVED) {
            recentApprovedButton.getStyleClass().add("recent-filter-active");
        } else if (recentStatusFilter == LeaveRequestStatus.REJECTED) {
            recentRejectedButton.getStyleClass().add("recent-filter-active");
        }
    }

    @FXML
    private void initialize() {
        comboProfile.setItems(FXCollections.observableArrayList("Admin / DRH"));
        comboProfile.getSelectionModel().selectFirst();
        recentAllButton.setOnAction(event -> setRecentFilter(null));
        recentPendingButton.setOnAction(event -> setRecentFilter(LeaveRequestStatus.PENDING));
        recentApprovedButton.setOnAction(event -> setRecentFilter(LeaveRequestStatus.APPROVED));
        recentRejectedButton.setOnAction(event -> setRecentFilter(LeaveRequestStatus.REJECTED));
        updateRecentFilterButtonStyles();

        refreshStats();
        // Keep dashboard counters up to date while user stays on this page.
        statsAutoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> refreshStats()));
        statsAutoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        statsAutoRefreshTimeline.play();

        btnDashboard.setOnAction(event -> ViewNavigator.switchScene(btnDashboard, "/view/dashboard-view.fxml", "Dashboard"));
        btnEmployees.setOnAction(event -> ViewNavigator.switchScene(btnEmployees, "/view/menu-emlpoyees-view.fxml", "Employees"));
        congesButton.setOnAction(event -> ViewNavigator.switchScene(congesButton, "/view/menu-conges-view.fxml", "Leave Requests"));
        btnSupport.setOnAction(event -> ViewNavigator.openModal(btnSupport, "/view/support-view.fxml", "Support"));
        btnLogout.setOnAction(event -> ViewNavigator.logout(btnLogout));
        btnNotifications.setOnAction(event -> {
            long pending = leaveRequestDao.findByStatus(LeaveRequestStatus.PENDING).size();
            ViewNavigator.showInformation("Notifications", pending + " leave request(s) waiting for approval.");
        });
    }
}
