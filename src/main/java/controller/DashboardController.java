package controller;

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
    private ComboBox<?> comboProfile;

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

}
