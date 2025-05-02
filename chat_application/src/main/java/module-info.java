module org.example.chat_application {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.rmi;

    opens org.example.chat_application.controller to javafx.fxml;
    exports org.example.chat_application.controller;

    exports org.example.chat_application.app;
    exports org.example.chat_application.rmi;
    exports org.example.chat_application.rmi.client;
}
