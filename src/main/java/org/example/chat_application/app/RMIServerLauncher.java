package org.example.chat_application.app;

import org.example.chat_application.rmi.ChatService;
import org.example.chat_application.rmi.server.ChatServiceImpl;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMIServerLauncher {

    public static void launchRMIRegistry() throws Exception {
        // Create registry on port 1099
        Registry registry = LocateRegistry.createRegistry(1099);
        System.out.println("RMI Registry started on port 1099...");

        // Create ChatService implementation instance
        ChatServiceImpl chatService = new ChatServiceImpl();

        // Bind to registry - ChatServiceImpl already extends UnicastRemoteObject, so no need to export it again
        registry.rebind("chat", chatService);
        System.out.println("ChatService bound to RMI registry.");
    }

    public static void main(String[] args) {
        try {
            launchRMIRegistry();
        } catch (Exception e) {
            System.err.println("Failed to start RMI server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
