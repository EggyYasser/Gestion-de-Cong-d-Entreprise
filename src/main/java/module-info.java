module com.gestionconges.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.gestionconges to javafx.fxml;
    exports com.gestionconges;
    exports controller;
    opens controller to javafx.fxml;
}