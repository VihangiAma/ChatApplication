package org.example.chat_application.dao;

import org.example.chat_application.model.Message;

import java.util.List;

public interface MessageDAO {
    void saveMessage(Message message);
    Message getMessageById(int id);
    List<Message> getAllMessages();
    List<Message> getMessagesByChatId(int chatId);
    void updateMessage(Message message);
    void deleteMessage(int id);
}
