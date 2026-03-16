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

public class MenuEmployeeController {

    @FXML
    private Button abandonnementlButton;

    @FXML
    private Button ajouterEmployeesButton;

    @FXML
    private Button congeesButton;

    @FXML
    private Button dashboardButton;

    @FXML
    private Text employeePageTitleText;

    @FXML
    private Button employeesButton;

    @FXML
    private ListView<String> employeesListView;

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
    private Button nextPageButton;

    @FXML
    private Button notificationButton;

    @FXML
    private HBox paginationHBox;

    @FXML
    private Label paginationLabel;

    @FXML
    private Button prevPageButton;

    @FXML
    private ComboBox<String> profileComboBox;

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
        profileComboBox.setItems(FXCollections.observableArrayList("Admin / DRH"));
        profileComboBox.getSelectionModel().selectFirst();

        employeesListView.setItems(FXCollections.observableArrayList(
                "EMP001 - Ahmed Benali - HR",
                "EMP002 - Sara Boussaid - Finance",
                "EMP003 - Yacine Merabet - IT",
                "EMP004 - Lina Kaci - Marketing"
        ));
        employeePageTitleText.setText("Employees (" + employeesListView.getItems().size() + ")");
        paginationLabel.setText("1-" + employeesListView.getItems().size() + " of " + employeesListView.getItems().size());

        dashboardButton.setOnAction(event -> ViewNavigator.switchScene(dashboardButton, "/view/dashboard-view.fxml", "Dashboard"));
        congeesButton.setOnAction(event -> ViewNavigator.switchScene(congeesButton, "/view/menu-conges-view.fxml", "Leave Requests"));
        employeesButton.setOnAction(event -> ViewNavigator.switchScene(employeesButton, "/view/menu-emlpoyees-view.fxml", "Employees"));
        supportButton.setOnAction(event -> ViewNavigator.openModal(supportButton, "/view/support-view.fxml", "Support"));
        logoutButton.setOnAction(event -> ViewNavigator.switchScene(logoutButton, "/view/login-view.fxml", "Login"));
        ajouterEmployeesButton.setOnAction(event -> ViewNavigator.openModal(ajouterEmployeesButton, "/view/add-employee.fxml", "Add Employee"));
        abandonnementlButton.setOnAction(event -> ViewNavigator.showInformation("Abandonment", "This module is outside version 1."));
        notificationButton.setOnAction(event -> ViewNavigator.showInformation("Notifications", "No notifications available."));
        prevPageButton.setOnAction(event -> ViewNavigator.showInformation("Pagination", "You are already on the first page."));
        nextPageButton.setOnAction(event -> ViewNavigator.showInformation("Pagination", "There are no more pages yet."));
    }
}
