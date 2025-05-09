module org.example.chat_application {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.rmi;
    requires java.persistence;
    requires org.hibernate.orm.core;
    requires java.desktop;
    requires java.sql;

    opens org.example.chat_application.controller to javafx.fxml;
    exports org.example.chat_application.controller;

    // Open model package to Hibernate for entity scanning and reflection
    opens org.example.chat_application.model to org.hibernate.orm.core, javax.persistence;
    exports org.example.chat_application.model;

    exports org.example.chat_application.app;
    exports org.example.chat_application.rmi;
    exports org.example.chat_application.rmi.client;
}
