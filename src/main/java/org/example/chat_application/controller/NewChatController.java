package org.example.chat_application.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.chat_application.dao.impl.ChatDAOImpl;
import org.example.chat_application.model.Chat;
import org.example.chat_application.model.User;
import org.example.chat_application.util.AlertUtil;
import org.example.chat_application.util.HibernateUtil;
import org.hibernate.SessionFactory;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class NewChatController {
    @FXML
    private TextField Chatnamefield;

    @FXML
    private DatePicker DateField;

    @FXML
    private TextField descriptionField;

    @FXML
    private Button CreateBtn;

    private final ChatDAOImpl chatDAO;
    private Runnable onChatCreated;
    private User adminUser;
    private int lastCreatedChatId = -1;

    public NewChatController() {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        this.chatDAO = new ChatDAOImpl(sessionFactory);
    }

    public void setOnChatCreated(Runnable onChatCreated) {
        this.onChatCreated = onChatCreated;
    }

    public void setAdminUser(User adminUser) {
        this.adminUser = adminUser;
    }

    public int getLastCreatedChatId() {
        return lastCreatedChatId;
    }

    @FXML
    void Create_Chat(ActionEvent event) {
        try {
            // Validate input
            String chatName = Chatnamefield.getText().trim();
            LocalDate selectedDate = DateField.getValue();
            String description = descriptionField.getText().trim();

            if (chatName.isEmpty()) {
                AlertUtil.showError("Validation Error", "Chat name cannot be empty");
                return;
            }

            // Create a new chat
            Chat newChat = new Chat();

            // Set the start date if provided, otherwise use current time
            if (selectedDate != null) {
                // Convert LocalDate to Date
                Date date = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                newChat.setStartedAt(new Timestamp(date.getTime()));
            } else {
                newChat.setStartedAt(new Timestamp(new Date().getTime()));
            }

            // Set chat name and description
            newChat.setName(chatName);
            newChat.setDescription(description);

            // Set the creator (admin user) if available
            if (adminUser != null) {
                newChat.setCreator(adminUser);
            }

            // Save the chat to the database
            chatDAO.saveChat(newChat);

            // Store the last created chat ID
            lastCreatedChatId = newChat.getChatId();

            // Show success message
            AlertUtil.showInformation("Success", "Chat created successfully!");

            // Notify the parent controller that a chat was created
            if (onChatCreated != null) {
                onChatCreated.run();
            }

            // Close the form
            Stage stage = (Stage) CreateBtn.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Failed to create chat: " + e.getMessage());
        }
    }
}
