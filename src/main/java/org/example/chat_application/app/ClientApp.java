/*package org.example.chat_application.app;

import org.example.chat_application.model.User;
import org.example.chat_application.rmi.ChatService;
import org.example.chat_application.rmi.client.ClientInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class ClientApp {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            System.out.print("Enter your name: ");
            String name = scanner.nextLine();

            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            ChatService chatService = (ChatService) registry.lookup("chat");

            ClientInterface client = new ClientInterface() {
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
            };

            ClientInterface stub = (ClientInterface) UnicastRemoteObject.exportObject(client, 0);

            // Create a User object for the client
            User user = new User();
            user.setUsername(name);

            chatService.registerClient(stub, name, user);

            new Thread(() -> {
                while (true) {
                    String message = scanner.nextLine();
                    try {
                        if (message.equalsIgnoreCase("Bye")) {
                            chatService.removeClient(name);
                            System.exit(0);
                        }
                        chatService.broadcastMessage(message, name);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();

            // Provide more helpful error message for connection issues
            if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                System.err.println("\nERROR: Unable to connect to the RMI server.");
                System.err.println("Please ensure that:");
                System.err.println("1. The RMI server is running (start RMIServerLauncher)");
                System.err.println("2. The server is accessible on localhost:1099");
                System.err.println("3. No firewall is blocking the connection");
            }
        }
    }
}*/
