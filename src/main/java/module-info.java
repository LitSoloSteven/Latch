module ni.edu.uam.latch {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens ni.edu.uam.latch to javafx.fxml;
    exports ni.edu.uam.latch;
    exports ni.edu.uam.latch.modelo;
}