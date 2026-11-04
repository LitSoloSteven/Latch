module ni.edu.uam.latch {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires java.sql;
    requires java.naming;

    opens ni.edu.uam.latch to javafx.fxml;
    // Permite a Hibernate acceder a la entidad mediante reflexión
    opens ni.edu.uam.latch.modelo to org.hibernate.orm.core;

    exports ni.edu.uam.latch;
    exports ni.edu.uam.latch.Application;
    opens ni.edu.uam.latch.Application to javafx.fxml;
    exports ni.edu.uam.latch.Controllers;
    opens ni.edu.uam.latch.Controllers to javafx.fxml;
}
