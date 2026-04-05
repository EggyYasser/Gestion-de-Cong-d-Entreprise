package controller;

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
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import util.ViewNavigator;

public class DashboardController {

    private final EmployeeDao employeeDao = new EmployeeDao();
    private final LeaveRequestDao leaveRequestDao = new LeaveRequestDao();

    @FXML
    private Button abonnementsButton;

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
    private TextField txtSearch;

    @FXML
    private Text txtWelcomeMsg;

    @FXML
    private Label valActiveEmployees;

    @FXML
    private Label valPendingLeaves;

    @FXML
    private Label valStatApproved;

    @FXML
    private Label valStatPending;

    @FXML
    private Label valStatRejected;

    @FXML
    private Label valTotalEmployees;

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
            valPendingLeaves.setText(String.valueOf(pending));
            valStatPending.setText(String.valueOf(pending));
            valStatApproved.setText(String.valueOf(approved));
            valStatRejected.setText(String.valueOf(rejected));
            lblTrendPending.setText(pending + " waiting");
            lblTrendTotal.setText(total + " employees");
            lblTrendActive.setText(active + " active");
        } catch (RuntimeException ex) {
            valTotalEmployees.setText("—");
            valActiveEmployees.setText("—");
            valPendingLeaves.setText("—");
            valStatPending.setText("—");
            valStatApproved.setText("—");
            valStatRejected.setText("—");
            lblTrendTotal.setText("DB error");
            lblTrendActive.setText("");
            lblTrendPending.setText(ex.getMessage() != null ? ex.getMessage() : "Could not load stats");
        }
    }

    @FXML
    private void initialize() {
        comboProfile.setItems(FXCollections.observableArrayList("Admin / DRH"));
        comboProfile.getSelectionModel().selectFirst();

        refreshStats();

        btnDashboard.setOnAction(event -> ViewNavigator.switchScene(btnDashboard, "/view/dashboard-view.fxml", "Dashboard"));
        btnEmployees.setOnAction(event -> ViewNavigator.switchScene(btnEmployees, "/view/menu-emlpoyees-view.fxml", "Employees"));
        congesButton.setOnAction(event -> ViewNavigator.switchScene(congesButton, "/view/menu-conges-view.fxml", "Leave Requests"));
        btnSupport.setOnAction(event -> ViewNavigator.openModal(btnSupport, "/view/support-view.fxml", "Support"));
        btnLogout.setOnAction(event -> ViewNavigator.logout(btnLogout));
        abonnementsButton.setOnAction(event -> ViewNavigator.showInformation("Abonnement", "This module is not included in version 1."));
        btnNotifications.setOnAction(event -> {
            long pending = leaveRequestDao.findByStatus(LeaveRequestStatus.PENDING).size();
            ViewNavigator.showInformation("Notifications", pending + " leave request(s) waiting for approval.");
        });
    }
}
