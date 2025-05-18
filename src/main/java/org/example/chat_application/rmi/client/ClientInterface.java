package org.example.chat_application.rmi.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInterface extends Remote {
    void receiveMessage(String message) throws RemoteException;

    // Chat event notifications
    void notifyChatStarted(int chatId, String time) throws RemoteException;
    void notifyUserJoined(String nickname, String time) throws RemoteException;
    void notifyUserLeft(String nickname, String time) throws RemoteException;
    void notifyChatEnded(String time) throws RemoteException;

    // Admin notifications
    void notifyAdminStartedChat(int chatId, String adminName) throws RemoteException;

    // Online status notifications
    void updateUserOnlineStatus(String username, boolean isOnline) throws RemoteException;

    // ✅ Dashboard notification
    void receiveDashboardNotification(String message) throws RemoteException;
}
