package util;

import com.gestionconges.Main;
import com.gestionconges.SessionContext;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public final class ViewNavigator {

    private static final String LOGIN_FXML = "/view/login-view.fxml";
    private static final double LOGIN_WIDTH = 1100;
    private static final double LOGIN_HEIGHT = 700;
    private static final double MAIN_APP_WIDTH = 1400;
    private static final double MAIN_APP_HEIGHT = 700;

    private ViewNavigator() {
    }

    public static void logout(Node sourceNode) {
        SessionContext.clear();
        switchScene(sourceNode, LOGIN_FXML, "Login");
    }

    public static void switchScene(Node sourceNode, String fxmlPath, String title) {
        Parent root = loadView(fxmlPath);
        Stage stage = (Stage) sourceNode.getScene().getWindow();

        double previousWidth = stage.getScene().getWidth();
        double width = resolveSceneWidth(fxmlPath, previousWidth);
        double height = resolveSceneHeight(fxmlPath, stage.getScene().getHeight());
        boolean maximized = stage.isMaximized();

        configureStageForView(stage, fxmlPath);
        Scene newScene = new Scene(root, width, height);
        stage.setScene(newScene);
        stage.setTitle(title);
        stage.setMaximized(maximized);

        if (!maximized && isMainAppView(fxmlPath) && width > previousWidth + 1) {
            stage.centerOnScreen();
        }
    }

    public static void openModal(Node sourceNode, String fxmlPath, String title) {
        Parent root = loadView(fxmlPath);
        Stage modalStage = new Stage();
        modalStage.initOwner(sourceNode.getScene().getWindow());
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle(title);
        modalStage.setScene(new Scene(root));
        modalStage.setResizable(false);
        modalStage.showAndWait();
    }

    public static void showInformation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static Parent loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxmlPath));
            return loader.load();
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load view: " + fxmlPath, exception);
        }
    }

    private static boolean isMainAppView(String fxmlPath) {
        return !LOGIN_FXML.equals(fxmlPath);
    }

    private static double resolveSceneWidth(String fxmlPath, double currentWidth) {
        if (LOGIN_FXML.equals(fxmlPath)) {
            return LOGIN_WIDTH;
        }
        return Math.max(currentWidth, MAIN_APP_WIDTH);
    }

    private static double resolveSceneHeight(String fxmlPath, double currentHeight) {
        if (LOGIN_FXML.equals(fxmlPath)) {
            return LOGIN_HEIGHT;
        }
        return Math.max(currentHeight, MAIN_APP_HEIGHT);
    }

    private static void configureStageForView(Stage stage, String fxmlPath) {
        stage.setResizable(true);
        if (LOGIN_FXML.equals(fxmlPath)) {
            stage.setMinWidth(700);
            stage.setMinHeight(400);
            return;
        }
        stage.setMinWidth(MAIN_APP_WIDTH);
        stage.setMinHeight(MAIN_APP_HEIGHT);
    }
}
