package org.example.chat_application.app;

import org.example.chat_application.rmi.ChatService;
import org.example.chat_application.rmi.client.ClientCallback;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class ClientApp {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            System.out.print("Enter your Nick Name: ");
            String nickname = scanner.nextLine();

            System.out.print("Enter your Profile (ex: Admin/User): ");
            String profile = scanner.nextLine();

            // Connect to the RMI Registry and get ChatService
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            ChatService chatService = (ChatService) registry.lookup("ChatService");

            // Create client callback and export it as a stub
            ClientCallback callback = new ClientCallback() {
                @Override
                public void receiveMessage(String message) {
                    System.out.println(message);
                }
            };
            ClientCallback stub = (ClientCallback) UnicastRemoteObject.exportObject(callback, 0);

            // Register this client
            chatService.registerClient(nickname, profile, stub);

            // Chat input loop
            while (true) {
                String message = scanner.nextLine();

                if (message.equalsIgnoreCase("Bye")) {
                    chatService.removeClient(nickname);
                    System.exit(0);
                }

                chatService.broadcastMessage(message, nickname);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
