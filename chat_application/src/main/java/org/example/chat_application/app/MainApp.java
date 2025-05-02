package org.example.chat_application.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class MainApp extends Application {
private  static Stage stg;
    @Override
    public void start(Stage primaryStage) throws Exception {
        stg=primaryStage;
        primaryStage.setResizable(false);
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/org/example/chat_application/view/userDashboard.fxml")));
        primaryStage.setTitle("SIGN UP");
        primaryStage.setScene(new Scene(root, 850, 500));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
