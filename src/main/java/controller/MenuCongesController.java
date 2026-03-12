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

public class MenuCongesController {

    @FXML
    private Button abandonButton;

    @FXML
    private Button addRequestButton;

    @FXML
    private Button allFilterButton;

    @FXML
    private Button approvedFilterButton;

    @FXML
    private Button congesButton;

    @FXML
    private ListView<?> congesListView;

    @FXML
    private HBox contentAreaHBox;

    @FXML
    private Button dashboardButton;

    @FXML
    private Label filtersLabel;

    @FXML
    private VBox filtersVBox;

    @FXML
    private HBox headerHBox;

    @FXML
    private ImageView logoImageView;

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
    private ComboBox<?> profileComboBox;

    @FXML
    private Button rejectedFilterButton;

    @FXML
    private Button retardButton;

    @FXML
    private TextField searchTextField;

    @FXML
    private VBox sidebarVBox;

    @FXML
    private HBox topBarHBox;

}
