package controller;

import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

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
    private void exit(){
        System.exit(0);
    }

}

