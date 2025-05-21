package org.example.chat_application.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class EventBubbleController {
    @FXML
    private Label eventText;

    public void setText(String text) {
        eventText.setText(text);
    }
}
