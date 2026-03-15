package controller;

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
    private ListView<?> employeesListView;

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
    private ComboBox<?> profileComboBox;

    @FXML
    private TextField searchTextField;

    @FXML
    private VBox sidebarVBox;

    @FXML
    private Button supportButton;

    @FXML
    private HBox topBarHBox;

}
