package org.example.chat_application.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    private static Stage stg;

    @Override
    public void start(Stage primaryStage) throws Exception {
        stg = primaryStage;
        primaryStage.setResizable(false);

        // Initialize Hibernate SessionFactory early to catch any configuration issues
        try {
            System.out.println("Initializing Hibernate SessionFactory...");
            org.example.chat_application.util.HibernateUtil.getSessionFactory();
            System.out.println("Hibernate SessionFactory initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing Hibernate: " + e.getMessage());
            e.printStackTrace();
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/chat_application/view/login.fxml"));
        try {
            Parent root = fxmlLoader.load();
            primaryStage.setTitle("Chat Application - Login");
            primaryStage.setScene(new Scene(root, 850, 500));
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Error loading FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
