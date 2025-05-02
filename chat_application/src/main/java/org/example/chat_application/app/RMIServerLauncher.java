package org.example.chat_application.app;


import org.example.chat_application.rmi.ChatService;
import org.example.chat_application.rmi.server.ChatServiceImpl;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIServerLauncher {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(1099); // default RMI port
            ChatService chatService = new ChatServiceImpl();
            registry.rebind("ChatService", chatService);
            System.out.println("Chat RMI Server is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

