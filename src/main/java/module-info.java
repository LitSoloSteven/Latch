module ni.edu.uam.latch {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.naming;
    requires org.postgresql.jdbc;

    opens ni.edu.uam.latch to javafx.fxml;
    exports ni.edu.uam.latch;
}
