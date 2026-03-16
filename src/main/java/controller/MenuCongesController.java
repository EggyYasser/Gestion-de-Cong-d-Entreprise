package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import util.ViewNavigator;

public class MenuCongesController {

    @FXML
    private Button abonnementsButton;

    @FXML
    private Button addRequestButton;

    @FXML
    private Button allFilterButton;

    @FXML
    private Button approvedFilterButton;

    @FXML
    private Button congeesButton;

    @FXML
    private ListView<String> congesListView;

    @FXML
    private HBox contentAreaHBox;

    @FXML
    private Button dashboardButton;

    @FXML
    private Button employeesButton;

    @FXML
    private Label filtersLabel;

    @FXML
    private VBox filtersVBox;

    @FXML
    private HBox headerHBox;

    @FXML
    private ImageView logoImageView;

    @FXML
    private Button logoutButton;

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private VBox mainContentVBox;

    @FXML
    private Button notificationButton;

    @FXML
    private Text pageTitleText;

    @FXML
    private Button pendingFilterButton;

    @FXML
    private ComboBox<String> profileComboBox;

    @FXML
    private Button rejectedFilterButton;

    @FXML
    private TextField searchTextField;

    @FXML
    private VBox sidebarVBox;

    @FXML
    private Button supportButton;

    @FXML
    private HBox topBarHBox;

    @FXML
    private void initialize() {
        profileComboBox.setItems(FXCollections.observableArrayList("Admin"));
        profileComboBox.getSelectionModel().selectFirst();
        showAllRequests();

        dashboardButton.setOnAction(event -> ViewNavigator.switchScene(dashboardButton, "/view/dashboard-view.fxml", "Dashboard"));
        employeesButton.setOnAction(event -> ViewNavigator.switchScene(employeesButton, "/view/menu-emlpoyees-view.fxml", "Employees"));
        congeesButton.setOnAction(event -> ViewNavigator.switchScene(congeesButton, "/view/menu-conges-view.fxml", "Leave Requests"));
        supportButton.setOnAction(event -> ViewNavigator.openModal(supportButton, "/view/support-view.fxml", "Support"));
        logoutButton.setOnAction(event -> ViewNavigator.switchScene(logoutButton, "/view/login-view.fxml", "Login"));
        notificationButton.setOnAction(event -> ViewNavigator.showInformation("Notifications", "No notifications available."));
        addRequestButton.setOnAction(event -> ViewNavigator.showInformation("New Leave Request", "Leave request creation form will be connected in the next backend step."));
        allFilterButton.setOnAction(event -> showAllRequests());
        pendingFilterButton.setOnAction(event -> congesListView.setItems(FXCollections.observableArrayList(
                "REQ001 - Ahmed Benali - Annual Leave - Pending",
                "REQ003 - Lina Kaci - Sick Leave - Pending"
        )));
        approvedFilterButton.setOnAction(event -> congesListView.setItems(FXCollections.observableArrayList(
                "REQ002 - Sara Boussaid - Annual Leave - Approved"
        )));
        rejectedFilterButton.setOnAction(event -> congesListView.setItems(FXCollections.observableArrayList(
                "REQ004 - Yacine Merabet - Exceptional Leave - Rejected"
        )));
    }

    private void showAllRequests() {
        congesListView.setItems(FXCollections.observableArrayList(
                "REQ001 - Ahmed Benali - Annual Leave - Pending",
                "REQ002 - Sara Boussaid - Annual Leave - Approved",
                "REQ003 - Lina Kaci - Sick Leave - Pending",
                "REQ004 - Yacine Merabet - Exceptional Leave - Rejected"
        ));
    }
}
