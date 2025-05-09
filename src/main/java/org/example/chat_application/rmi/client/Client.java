package org.example.chat_application.rmi.client;

import org.example.chat_application.model.User;
import org.example.chat_application.rmi.ChatService;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class Client extends UnicastRemoteObject implements ClientInterface {
    private final String name;

    protected Client(String name) throws Exception {
        super();
        this.name = name;
    }

    @Override
    public void receiveMessage(String message) {
        System.out.println(message);
    }

    @Override
    public void notifyChatStarted(int chatId, String time) throws RemoteException {
        System.out.println("Chat started at: " + time);
    }

    @Override
    public void notifyUserJoined(String nickname, String time) throws RemoteException {
        System.out.println("\"" + nickname + "\" has joined: " + time);
    }

    @Override
    public void notifyUserLeft(String nickname, String time) throws RemoteException {
        System.out.println("\"" + nickname + "\" left: " + time);
    }

    @Override
    public void notifyChatEnded(String time) throws RemoteException {
        System.out.println("Chat stopped at: " + time);
    }

    @Override
    public void notifyAdminStartedChat(int chatId, String adminName) throws RemoteException {
        System.out.println("Admin " + adminName + " started a chat with ID: " + chatId);
    }

    public static void main(String[] args) {
        try {
            Scanner sc = new Scanner(System.in);
            System.out.print("Enter your username: ");
            String name = sc.nextLine();

            Client client = new Client(name);
            ChatService service = (ChatService) Naming.lookup("rmi://localhost:1099/chat");

            // Create a User object for the client
            User user = new User();
            user.setUsername(name);

            service.registerClient(client, name, user);

            while (true) {
                String message = sc.nextLine();
                service.broadcastMessage(message, name);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
