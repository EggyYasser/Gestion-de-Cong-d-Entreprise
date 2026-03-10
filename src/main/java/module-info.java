module com.gestionconges.demo {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.gestionconges to javafx.fxml;
    exports com.gestionconges;
    exports controller;
    opens controller to javafx.fxml;
}