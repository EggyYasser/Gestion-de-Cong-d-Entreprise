module com.gestionconges.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    opens com.gestionconges to javafx.fxml;
    exports com.gestionconges;
    exports controller;
    opens controller to javafx.fxml;
}