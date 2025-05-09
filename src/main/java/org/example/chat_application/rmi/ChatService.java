package org.example.chat_application.rmi;

import org.example.chat_application.model.User;
import org.example.chat_application.rmi.client.ClientInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ChatService extends Remote {
    void registerClient(ClientInterface client, String name, User user) throws RemoteException;
    void removeClient(String name) throws RemoteException;
    void broadcastMessage(String message, String senderName) throws RemoteException;

    // Admin chat methods
    void startChat(int chatId, String adminName) throws RemoteException;
    void subscribeToChat(int chatId, String userName) throws RemoteException;
    void unsubscribeFromChat(int chatId, String userName) throws RemoteException;
    List<String> getSubscribedUsers(int chatId) throws RemoteException;
    List<String> getActiveUsers(int chatId) throws RemoteException;
    void notifySubscribedUsers(int chatId, String message) throws RemoteException;
    void endChat(int chatId) throws RemoteException;

    // Notify all users about a new chat created by an admin
    void notifyAllUsersAboutNewChat(int chatId, String adminName) throws RemoteException;
}
