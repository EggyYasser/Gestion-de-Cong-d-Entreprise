package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import util.ViewNavigator;

public class LoginController {

    @FXML
    private Hyperlink createAccountLink;

    @FXML
    private TextField emailField;

    @FXML
    private Button exitButton;

    @FXML
    private Hyperlink forgotPasswordLink;

    @FXML
    private Pane infoPanel;

    @FXML
    private Button loginButton;

    @FXML
    private AnchorPane mainPanel;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox rememberMeCheckBox;

    @FXML
    private void initialize() {
        loginButton.setOnAction(event -> handleLogin());
        createAccountLink.setOnAction(event -> ViewNavigator.showInformation(
                "Create Account",
                "Account creation is not available yet in version 1."
        ));
        forgotPasswordLink.setOnAction(event -> ViewNavigator.showInformation(
                "Forgot Password",
                "Password recovery is not connected yet."
        ));
    }

    @FXML
    private void exit(){
        System.exit(0);
    }

    private void handleLogin() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            ViewNavigator.showInformation("Login", "Please enter your email and password.");
            return;
        }

        ViewNavigator.switchScene(loginButton, "/view/dashboard-view.fxml", "Dashboard");
    }
}

