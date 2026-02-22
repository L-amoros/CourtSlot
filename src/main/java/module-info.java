module org.example.courtslot {

    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;

    // Hibernate + JPA
    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    // Driver MariaDB
    requires org.mariadb.jdbc;

    // Paquete raíz
    exports org.example.courtslot;
    opens   org.example.courtslot to javafx.fxml, org.hibernate.orm.core;

    // Modelos
    exports org.example.courtslot.model;
    opens   org.example.courtslot.model to javafx.fxml, org.hibernate.orm.core;

    // DAO
    exports org.example.courtslot.dao;
    opens   org.example.courtslot.dao to javafx.fxml, org.hibernate.orm.core;

    // Utilidades
    exports org.example.courtslot.util;
    opens   org.example.courtslot.util to javafx.fxml, org.hibernate.orm.core;

    exports org.example.courtslot.controller;
    opens   org.example.courtslot.controller to javafx.fxml, org.hibernate.orm.core;
}