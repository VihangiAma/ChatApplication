package org.example.chat_application.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ChatBubbleController {

    @FXML
    private Label userName;

    @FXML
    private Label messageText;

    @FXML
    private ImageView profileImage;

    public void setData(String name, String message, String imagePath) {
        userName.setText(name);
        messageText.setText(message);

        try {
            // Handle different types of image paths
            if (imagePath != null && !imagePath.isEmpty()) {
                // If it's already a valid URL (http:, https:, file:, jar:)
                if (imagePath.startsWith("http:") || imagePath.startsWith("https:") 
                    || imagePath.startsWith("file:") || imagePath.startsWith("jar:")) {
                    profileImage.setImage(new Image(imagePath));
                } 
                // If it's a file path (could start with drive letter like C:, D:, E:)
                else if (imagePath.contains(":") || imagePath.startsWith("/") || imagePath.startsWith("\\")) {
                    // Convert to proper file URL
                    profileImage.setImage(new Image("file:///" + imagePath.replace("\\", "/")));
                }
                // If it's a resource path
                else if (imagePath.startsWith("/org/example")) {
                    profileImage.setImage(new Image(getClass().getResource(imagePath).toExternalForm()));
                }
                // Default case - try as is
                else {
                    profileImage.setImage(new Image(imagePath));
                }
            } else {
                // Use a default image if path is null or empty
                profileImage.setImage(new Image(getClass().getResource("/org/example/chat_application/images/user1.png").toExternalForm()));
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            // Use default image on error
            try {
                profileImage.setImage(new Image(getClass().getResource("/org/example/chat_application/images/user1.png").toExternalForm()));
            } catch (Exception ex) {
                // If even the default image fails, just log the error
                System.err.println("Error loading default image: " + ex.getMessage());
            }
        }
    }
}
