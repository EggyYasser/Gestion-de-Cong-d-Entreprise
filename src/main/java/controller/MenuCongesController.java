package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import util.ViewNavigator;

public class MenuCongesController {

    @FXML private Button abonnementsButton, addRequestButton, allFilterButton, approvedFilterButton, congeesButton;
    @FXML private ListView<String> congesListView;
    @FXML private HBox contentAreaHBox, headerHBox, topBarHBox;
    @FXML private Button dashboardButton, employeesButton, logoutButton, notificationButton, pendingFilterButton, rejectedFilterButton, supportButton;
    @FXML private Label filtersLabel;
    @FXML private VBox filtersVBox, mainContentVBox, sidebarVBox;
    @FXML private ImageView logoImageView;
    @FXML private BorderPane mainBorderPane;
    @FXML private Text pageTitleText;
    @FXML private ComboBox<String> profileComboBox;
    @FXML private TextField searchTextField;

    @FXML
    private void initialize() {
        profileComboBox.setItems(FXCollections.observableArrayList("Admin"));
        profileComboBox.getSelectionModel().selectFirst();


        setupCustomListView();

        showAllRequests();


        dashboardButton.setOnAction(event -> ViewNavigator.switchScene(dashboardButton, "/view/dashboard-view.fxml", "Dashboard"));
        employeesButton.setOnAction(event -> ViewNavigator.switchScene(employeesButton, "/view/menu-emlpoyees-view.fxml", "Employees"));
        congeesButton.setOnAction(event -> ViewNavigator.switchScene(congeesButton, "/view/menu-conges-view.fxml", "Leave Requests"));
        supportButton.setOnAction(event -> ViewNavigator.openModal(supportButton, "/view/support-view.fxml", "Support"));
        logoutButton.setOnAction(event -> ViewNavigator.switchScene(logoutButton, "/view/login-view.fxml", "Login"));

        allFilterButton.setOnAction(event -> showAllRequests());
        pendingFilterButton.setOnAction(event -> congesListView.setItems(FXCollections.observableArrayList(
                "REQ001 - Ahmed Benali - Annual Leave - Pending",
                "REQ003 - Lina Kaci - Sick Leave - Pending"
        )));
    }

    private void setupCustomListView() {
        congesListView.setCellFactory(param -> new ListCell<String>() {
            private final HBox root = new HBox(15);
            private final Label label = new Label();
            private final Region spacer = new Region();
            private final Button actionBtn = new Button("Action");
            private final Button modifyBtn = new Button("Modify");
            private final Button deleteBtn = new Button("Delete");

            {
                root.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(spacer, Priority.ALWAYS);
                root.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));


                actionBtn.setId("action-button");
                modifyBtn.setId("modify-button");
                deleteBtn.setId("delete-button");

                root.getChildren().addAll(label, spacer, actionBtn, modifyBtn, deleteBtn);

                deleteBtn.setOnAction(event -> {
                    String item = getItem();
                    if (item != null) {
                        getListView().getItems().remove(item);
                    }
                });

                actionBtn.setOnAction(event -> {
                    // to complete
                });

                modifyBtn.setOnAction(event -> {
                    String item = getItem();
                    ViewNavigator.showInformation("Modify Request", "Editing: " + item);
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

    private void showAllRequests() {
        congesListView.setItems(FXCollections.observableArrayList(
                "REQ001 - Ahmed Benali - Annual Leave - Pending",
                "REQ002 - Sara Boussaid - Annual Leave - Approved",
                "REQ003 - Lina Kaci - Sick Leave - Pending",
                "REQ004 - Yacine Merabet - Exceptional Leave - Rejected"
        ));
    }
}