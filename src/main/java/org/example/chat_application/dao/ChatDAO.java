package org.example.chat_application.dao;

import org.example.chat_application.model.Chat;

import java.util.List;

public interface ChatDAO {
    void saveChat(Chat chat);
    Chat getChatById(int id);
    List<Chat> getAllChats();
    void updateChat(Chat chat);
    void deleteChat(int id);

}
