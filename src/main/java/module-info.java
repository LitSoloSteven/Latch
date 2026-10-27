module ni.edu.uam.latch {
    requires javafx.controls;
    requires javafx.fxml;


    opens ni.edu.uam.latch to javafx.fxml;
    exports ni.edu.uam.latch;
}