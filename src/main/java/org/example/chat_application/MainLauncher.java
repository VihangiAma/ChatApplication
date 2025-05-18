package org.example.chat_application;

import javafx.application.Application;
//import org.example.chat_application.app;
import org.example.chat_application.app.MainApp;
import org.example.chat_application.app.RMIServerLauncher;

public class MainLauncher {
    public static void main(String[] args) {
        // Start RMI Server in a background thread
        new Thread(() -> {
            try {
                RMIServerLauncher.launchRMIRegistry();
            } catch (Exception e) {
                System.err.println("Failed to start RMI server: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();

        // Launch the JavaFX client UI
        Application.launch(MainApp.class, args);
    }
}
