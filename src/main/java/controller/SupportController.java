package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import util.ViewNavigator;

public class SupportController {

    @FXML
    private TextArea descriptionTextArea;

    @FXML
    private Button sendRequestButton;

    @FXML
    private ComboBox<String> subjectComboBox;

    @FXML
    private void initialize() {
        subjectComboBox.setItems(FXCollections.observableArrayList(
                "Technical difficulties",
                "Account issue",
                "General question"
        ));
        subjectComboBox.getSelectionModel().selectFirst();
        sendRequestButton.setOnAction(event -> sendRequest());
    }

    private void sendRequest() {
        String description = descriptionTextArea.getText() == null ? "" : descriptionTextArea.getText().trim();

        if (description.isEmpty()) {
            ViewNavigator.showInformation("Support", "Please describe your request before sending.");
            return;
        }

        ViewNavigator.showInformation("Support", "Your support request has been sent.");
        descriptionTextArea.clear();
    }
}
