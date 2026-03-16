package controller;

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

    @FXML
    private void initialize() {
        comboProfile.setItems(FXCollections.observableArrayList("Admin / DRH"));
        comboProfile.getSelectionModel().selectFirst();

        valTotalEmployees.setText("12");
        valActiveEmployees.setText("10");
        valPendingLeaves.setText("3");
        valStatPending.setText("3");
        valStatApproved.setText("7");
        valStatRejected.setText("1");
        lblTrendTotal.setText("+2 this month");
        lblTrendActive.setText("+1 this week");
        lblTrendPending.setText("3 waiting");

        btnDashboard.setOnAction(event -> ViewNavigator.switchScene(btnDashboard, "/view/dashboard-view.fxml", "Dashboard"));
        btnEmployees.setOnAction(event -> ViewNavigator.switchScene(btnEmployees, "/view/menu-emlpoyees-view.fxml", "Employees"));
        congesButton.setOnAction(event -> ViewNavigator.switchScene(congesButton, "/view/menu-conges-view.fxml", "Leave Requests"));
        btnSupport.setOnAction(event -> ViewNavigator.openModal(btnSupport, "/view/support-view.fxml", "Support"));
        btnLogout.setOnAction(event -> ViewNavigator.switchScene(btnLogout, "/view/login-view.fxml", "Login"));
        abonnementsButton.setOnAction(event -> ViewNavigator.showInformation("Abonnement", "This module is not included in version 1."));
        btnNotifications.setOnAction(event -> ViewNavigator.showInformation("Notifications", "No new notifications for now."));
    }
}
