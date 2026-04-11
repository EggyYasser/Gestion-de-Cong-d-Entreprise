package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import dao.LeaveBalanceDao;
import model.Employee;
import model.LeaveBalance;

import java.util.List;
import java.util.Optional;

public class LeaveBalanceController {

    private final LeaveBalanceDao leaveBalanceDao = new LeaveBalanceDao();
    private Employee employee;
    private List<LeaveBalance> balances;

    @FXML
    private Button closeButton;

    @FXML
    private Button editButton;

    @FXML
    private Label headerLabel;

    @FXML
    private Label totalSummaryLabel;

    @FXML
    private ListView<LeaveBalance> balanceListView;

    @FXML
    private VBox rootBox;

    public void initData(Employee employee, List<LeaveBalance> balances) {
        this.employee = employee;
        this.balances = balances;
        headerLabel.setText("Leave Balance - " + employee.getEmployeeCode() + " " + employee.getFirstName() + " " + employee.getLastName());
        refreshList();
    }

    private void refreshList() {
        if (employee != null) {
            balances = leaveBalanceDao.findByEmployeeId(employee.getId());
            double total = leaveBalanceDao.getTotalAvailableDays(employee.getId());
            totalSummaryLabel.setText("Total available (all years, carryover included): "
                    + String.format("%.2f", total) + " days");
        }
        balanceListView.setItems(FXCollections.observableArrayList(balances));
        balanceListView.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(LeaveBalance item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    double net = item.getEarnedDays() - item.getUsedDays();
                    setText("Year " + item.getYear()
                            + " | Earned: " + item.getEarnedDays()
                            + " | Used: " + item.getUsedDays()
                            + " | Net (this year): " + net);
                }
            }
        });
    }

    @FXML
    private void onEditSelected() {
        LeaveBalance selected = balanceListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            selected = balances.isEmpty() ? null : balances.get(0);
        }
        if (selected == null) {
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit leave balance");
        dialog.setHeaderText(null);

        TextField yearField = new TextField(String.valueOf(selected.getYear()));
        TextField earnedField = new TextField(String.valueOf(selected.getEarnedDays()));
        TextField usedField = new TextField(String.valueOf(selected.getUsedDays()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Year"), yearField);
        grid.addRow(1, new Label("Earned days"), earnedField);
        grid.addRow(2, new Label("Used days"), usedField);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }
        try {
            int year = Integer.parseInt(yearField.getText().trim());
            double earned = Double.parseDouble(earnedField.getText().trim());
            double used = Double.parseDouble(usedField.getText().trim());
            if (earned < 0 || used < 0) {
                return;
            }
            leaveBalanceDao.saveManualBalance(employee.getId(), year, earned, used);
            leaveBalanceDao.recalculateRemainingForEmployee(employee.getId());
            refreshList();
        } catch (NumberFormatException ignored) {
        }
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) rootBox.getScene().getWindow();
        stage.close();
    }
}
