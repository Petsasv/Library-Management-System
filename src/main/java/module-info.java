module org.example.bookmngmntsys {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;
    requires javafx.graphics;
    requires javafx.base;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;

    opens org.example.bookmngmntsys to javafx.fxml;
    exports org.example.bookmngmntsys;
}