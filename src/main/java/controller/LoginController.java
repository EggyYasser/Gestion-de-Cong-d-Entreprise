package controller;

import dao.AdminDao;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import model.Admin;
import util.SessionContext;
import util.ViewNavigator;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private Button exitButton;

    @FXML
    private Button loginButton;

    @FXML
    private HBox mainPanel;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void initialize() {
        loginButton.setOnAction(event -> handleLogin());
    }

    @FXML
    private void exit() {
        System.exit(0);
    }

    private void handleLogin() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            ViewNavigator.showInformation("Login", "Please enter your email and password.");
            return;
        }

        AdminDao adminDao = new AdminDao();
        adminDao.findByEmailAndPassword(email, password).ifPresentOrElse(
                (Admin admin) -> {
                    SessionContext.setCurrentAdmin(admin);
                    ViewNavigator.switchScene(loginButton, "/view/dashboard-view.fxml", "Dashboard");
                },
                () -> ViewNavigator.showInformation("Login", "Invalid credentials. Please try again.")
        );
    }
}
