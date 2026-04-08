package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Employee;
import model.LeaveHistory;

import java.util.List;

public class LeaveHistoryController {

    @FXML
    private Button closeButton;

    @FXML
    private Label headerLabel;

    @FXML
    private ListView<String> historyListView;

    @FXML
    private VBox rootBox;

    public void initData(Employee employee, List<LeaveHistory> historyRows) {
        headerLabel.setText("Leave History - " + employee.getEmployeeCode() + " " + employee.getFirstName() + " " + employee.getLastName());
        List<String> rows = historyRows.stream()
                .map(h -> h.getActionDate() + " | " + h.getAction()
                        + " | " + (h.getNote() == null ? "" : h.getNote()))
                .toList();
        historyListView.setItems(FXCollections.observableArrayList(rows));
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) rootBox.getScene().getWindow();
        stage.close();
    }
}
