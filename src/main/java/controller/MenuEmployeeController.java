package controller;

import com.gestionconges.Main;
import dao.EmployeeDao;
import dao.LeaveRequestDao;
import enums.LeaveRequestStatus;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import enums.EmployeeStatus;
import model.Employee;
import util.EmployeeExcelImporter;
import util.ViewNavigator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class MenuEmployeeController {

    private static final int PAGE_SIZE = 10;
    private static final String RED_BUTTON_STYLE =
            "-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;";
    private static final String GREEN_BUTTON_STYLE =
            "-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;";

    private final EmployeeDao employeeDao = new EmployeeDao();
    private final LeaveRequestDao leaveRequestDao = new LeaveRequestDao();

    private final List<Employee> masterList = new ArrayList<>();
    private int currentPage = 0;

    @FXML
    private Button ajouterEmployeesButton;

    @FXML
    private Button uploadEmployeesButton;

    @FXML
    private Button congeesButton;

    @FXML
    private Button dashboardButton;

    @FXML
    private Text employeePageTitleText;

    @FXML
    private Button employeesButton;

    @FXML
    private ListView<Employee> employeesListView;

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
        reloadFromDatabase();

        searchTextField.textProperty().addListener((obs, o, n) -> {
            currentPage = 0;
            applyView();
        });

        dashboardButton.setOnAction(event -> ViewNavigator.switchScene(dashboardButton, "/view/dashboard-view.fxml", "Dashboard"));
        congeesButton.setOnAction(event -> ViewNavigator.switchScene(congeesButton, "/view/menu-conges-view.fxml", "Leave Requests"));
        employeesButton.setOnAction(event -> ViewNavigator.switchScene(employeesButton, "/view/menu-emlpoyees-view.fxml", "Employees"));
        supportButton.setOnAction(event -> ViewNavigator.openModal(supportButton, "/view/support-view.fxml", "Support"));
        logoutButton.setOnAction(event -> ViewNavigator.logout(logoutButton));
        ajouterEmployeesButton.setOnAction(event -> {
            ViewNavigator.openModal(ajouterEmployeesButton, "/view/add-employee.fxml", "Add Employee");
            reloadFromDatabase();
        });
        uploadEmployeesButton.setOnAction(event -> importEmployeesFromExcel());
        notificationButton.setOnAction(event -> {
            long pending = leaveRequestDao.findByStatus(LeaveRequestStatus.PENDING).size();
            ViewNavigator.showInformation("Notifications", pending + " leave request(s) pending approval.");
        });
        prevPageButton.setOnAction(event -> {
            if (currentPage > 0) {
                currentPage--;
                applyView();
            }
        });
        nextPageButton.setOnAction(event -> {
            currentPage++;
            applyView();
        });
    }

    private void importEmployeesFromExcel() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import employees from Excel");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel", "*.xlsx", "*.xls"));
        var file = chooser.showOpenDialog(employeesListView.getScene().getWindow());
        if (file == null) {
            return;
        }
        try {
            EmployeeExcelImporter.ImportResult result = EmployeeExcelImporter.importFromFile(
                    file.toPath(),
                    emp -> {
                        employeeDao.insert(emp);
                    });
            StringBuilder msg = new StringBuilder();
            msg.append("Imported ").append(result.successCount()).append(" employee(s).");
            if (result.skippedRows() > 0) {
                msg.append("\nSkipped ").append(result.skippedRows()).append(" empty row(s).");
            }
            if (!result.errors().isEmpty()) {
                msg.append("\n\nIssues:\n");
                int max = Math.min(15, result.errors().size());
                for (int i = 0; i < max; i++) {
                    msg.append("• ").append(result.errors().get(i)).append("\n");
                }
                if (result.errors().size() > max) {
                    msg.append("… and ").append(result.errors().size() - max).append(" more.");
                }
            }
            ViewNavigator.showInformation("Import Excel", msg.toString());
            if (result.successCount() > 0) {
                reloadFromDatabase();
            }
        } catch (IOException e) {
            ViewNavigator.showInformation("Import Excel", "Could not read file: " + e.getMessage());
        }
    }

    private void reloadFromDatabase() {
        masterList.clear();
        masterList.addAll(employeeDao.findAll());
        applyView();
    }

    private void applyView() {
        String q = searchTextField.getText() == null ? "" : searchTextField.getText().trim().toLowerCase(Locale.ROOT);
        List<Employee> filtered = masterList.stream().filter(e -> matchesSearch(e, q)).toList();
        int total = filtered.size();
        int maxPage = total == 0 ? 0 : (total - 1) / PAGE_SIZE;
        if (currentPage > maxPage) {
            currentPage = maxPage;
        }
        int from = total == 0 ? 0 : currentPage * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, total);
        List<Employee> pageItems = from < to ? filtered.subList(from, to) : List.of();
        employeesListView.setItems(FXCollections.observableArrayList(pageItems));
        employeePageTitleText.setText("Employees (" + total + ")");
        if (total == 0) {
            paginationLabel.setText("0 of 0");
        } else {
            paginationLabel.setText((from + 1) + "-" + to + " of " + total);
        }
        prevPageButton.setDisable(currentPage <= 0);
        nextPageButton.setDisable(to >= total);
    }

    private static boolean matchesSearch(Employee e, String q) {
        if (q.isEmpty()) {
            return true;
        }
        return contains(e.getEmployeeCode(), q)
                || contains(e.getFirstName(), q)
                || contains(e.getLastName(), q)
                || contains(e.getEmail(), q)
                || contains(e.getDepartment(), q);
    }

    private static boolean contains(String value, String q) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(q);
    }

    private void setupCustomListView() {
        employeesListView.setCellFactory(param -> new ListCell<>() {
            private final HBox root = new HBox(15);
            private final Label label = new Label();
            private final Region spacer = new Region();
            private final Button actionBtn = new Button("Action");
            private final Button statusBtn = new Button("Deactivate");

            {
                root.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(spacer, Priority.ALWAYS);
                root.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));
                actionBtn.setId("action-button");
                statusBtn.setId("delete-button");
                root.getChildren().addAll(label, spacer, actionBtn, statusBtn);

                statusBtn.setOnAction(event -> {
                    Employee item = getItem();
                    if (item == null) {
                        return;
                    }
                    boolean inactive = item.getStatus() == EmployeeStatus.INACTIVE;
                    String targetStatus = inactive ? "ACTIVE" : "INACTIVE";
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle((inactive ? "Activate" : "Deactivate") + " employee");
                    confirm.setHeaderText(null);
                    confirm.setContentText("Set " + item.getEmployeeCode() + " to " + targetStatus + "?");
                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        boolean updated = inactive ? employeeDao.activate(item.getId()) : employeeDao.deactivate(item.getId());
                        if (updated) {
                            reloadFromDatabase();
                        } else {
                            ViewNavigator.showInformation("Employees", "Could not update employee status.");
                        }
                    }
                });

                actionBtn.setOnAction(event -> {
                    Employee item = getItem();
                    if (item != null) {
                        openEmployeeDetails(item);
                    }
                });
            }

            @Override
            protected void updateItem(Employee item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    String dept = item.getDepartment() == null ? "—" : item.getDepartment();
                    String status = item.getStatus() == null ? "UNKNOWN" : item.getStatus().name();
                    label.setText(item.getEmployeeCode() + " — " + item.getFirstName() + " " + item.getLastName() + " — " + dept + " — " + status);
                    boolean inactive = item.getStatus() == EmployeeStatus.INACTIVE;
                    statusBtn.setText(inactive ? "Activate" : "Deactivate");
                    statusBtn.setStyle(inactive ? GREEN_BUTTON_STYLE : RED_BUTTON_STYLE);
                    setGraphic(root);
                }
            }
        });
    }

    private void openEmployeeDetails(Employee employee) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/view/employee-view.fxml"));
            Parent root = loader.load();
            EmployeeController controller = loader.getController();
            controller.initForEmployee(employee.getId(),
                    msg -> ViewNavigator.showInformation("Employee", msg),
                    this::reloadFromDatabase);
            Stage detailsStage = new Stage();
            detailsStage.setTitle("Employee details");
            detailsStage.initModality(Modality.APPLICATION_MODAL);
            detailsStage.initOwner(actionOwner());
            detailsStage.setScene(new Scene(root));
            detailsStage.showAndWait();
            reloadFromDatabase();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot open employee details", e);
        }
    }

    private javafx.stage.Window actionOwner() {
        return employeesListView.getScene() != null ? employeesListView.getScene().getWindow() : null;
    }
}
