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

    private ViewNavigator() {
    }

    public static void logout(Node sourceNode) {
        SessionContext.clear();
        switchScene(sourceNode, "/view/login-view.fxml", "Login");
    }

    public static void switchScene(Node sourceNode, String fxmlPath, String title) {
        Parent root = loadView(fxmlPath);

        Stage stage = (Stage) sourceNode.getScene().getWindow();

        double width = stage.getScene().getWidth();
        double height = stage.getScene().getHeight();
        boolean maximized = stage.isMaximized();

        Scene newScene = new Scene(root, width, height);

        stage.setScene(newScene);
        stage.setTitle(title);
        stage.setMaximized(maximized);
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
}
