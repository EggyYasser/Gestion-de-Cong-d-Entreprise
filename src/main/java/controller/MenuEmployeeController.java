package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import util.ViewNavigator;

import java.io.IOException;

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

        setupCustomListView();

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

    private void setupCustomListView() {
        employeesListView.setCellFactory(param -> new ListCell<String>() {
            private final HBox root = new HBox(15);
            private final Label label = new Label();
            private final Region spacer = new Region();
            private final Button actionBtn = new Button("Action");
            private final Button deleteBtn = new Button("Delete");

            {
                root.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(spacer, Priority.ALWAYS);
                root.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));


                actionBtn.setId("action-button");
                deleteBtn.setId("delete-button");

                root.getChildren().addAll(label, spacer, actionBtn, deleteBtn);

                deleteBtn.setOnAction(event -> {
                    String item = getItem();
                    if (item != null) {
                        getListView().getItems().remove(item);
                    }
                });

                actionBtn.setOnAction(event -> {
                    try {

                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/employee-view.fxml"));
                        Parent root = loader.load();


                        Stage detailsStage = new Stage();
                        detailsStage.setTitle("Employee Details");


                        detailsStage.initModality(Modality.APPLICATION_MODAL);
                        detailsStage.initOwner(actionBtn.getScene().getWindow());


                        Scene scene = new Scene(root);
                        detailsStage.setScene(scene);
                        detailsStage.showAndWait();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    label.setText(item);
                    setGraphic(root);
                }
            }
        });
}
}
