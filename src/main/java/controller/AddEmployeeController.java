package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import util.ViewNavigator;

public class AddEmployeeController {

    @FXML
    private Button addEAddAnotherButton;

    @FXML
    private Button addEApproveButton;

    @FXML
    private Label addEBirthDateLabel;

    @FXML
    private DatePicker addEBirthDatePicker;

    @FXML
    private HBox addEButtonBox;

    @FXML
    private Button addECancelButton;

    @FXML
    private ComboBox<String> addEDepartmentCombo;

    @FXML
    private Label addEDepartmentLabel;

    @FXML
    private TextField addEEmailField;

    @FXML
    private Label addEEmailLabel;

    @FXML
    private TextField addEFirstNameField;

    @FXML
    private Label addEFirstNameLabel;

    @FXML
    private GridPane addEGridPane;

    @FXML
    private Label addEHeaderLabel;

    @FXML
    private Label addEHireDateLabel;

    @FXML
    private DatePicker addEHireDatePicker;

    @FXML
    private TextField addELastNameField;

    @FXML
    private Label addELastNameLabel;

    @FXML
    private TextField addEPhoneField;

    @FXML
    private Label addEPhoneLabel;

    @FXML
    private TextField addEPositionField;

    @FXML
    private Label addEPositionLabel;

    @FXML
    private VBox addEVBox;

    @FXML
    private void initialize() {
        addEDepartmentCombo.setItems(FXCollections.observableArrayList(
                "Human Resources",
                "Finance",
                "IT",
                "Marketing"
        ));

        addECancelButton.setOnAction(event -> closeWindow());
        addEAddAnotherButton.setOnAction(event -> {
            clearForm();
            ViewNavigator.showInformation("Add Employee", "Form cleared. You can add another employee.");
        });
        addEApproveButton.setOnAction(event -> handleApprove());
    }

    private void handleApprove() {
        if (isBlank(addEFirstNameField) || isBlank(addELastNameField) || isBlank(addEEmailField)) {
            ViewNavigator.showInformation("Add Employee", "Please fill in first name, last name, and email.");
            return;
        }

        ViewNavigator.showInformation("Add Employee", "Employee information captured successfully.");
        closeWindow();
    }

    private boolean isBlank(TextField textField) {
        return textField.getText() == null || textField.getText().trim().isEmpty();
    }

    private void clearForm() {
        addEFirstNameField.clear();
        addELastNameField.clear();
        addEBirthDatePicker.setValue(null);
        addEHireDatePicker.setValue(null);
        addEDepartmentCombo.getSelectionModel().clearSelection();
        addEPositionField.clear();
        addEEmailField.clear();
        addEPhoneField.clear();
    }

    private void closeWindow() {
        Stage stage = (Stage) addEVBox.getScene().getWindow();
        stage.close();
    }
}
