package org.example.chat_application.app;


import org.example.chat_application.rmi.ChatService;
//import org.example.chat_application.rmi.server.ChatServerImpl;
import org.example.chat_application.rmi.server.ChatServiceImpl;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIServerLauncher {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(1099); // default RMI port
            ChatService chatService = new ChatServiceImpl();
            registry.rebind("chat", chatService);
            System.out.println("Chat RMI Server is running...");
            System.out.println("Service bound as 'chat' on localhost:1099");
            System.out.println("Ready to accept client connections");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to start RMI server: " + e.getMessage());
            e.printStackTrace();

            // Provide more helpful error messages for common issues
            if (e instanceof java.rmi.server.ExportException && e.getMessage().contains("Port already in use")) {
                System.err.println("\nThe port 1099 is already in use. This could mean:");
                System.err.println("1. Another instance of the RMI server is already running");
                System.err.println("2. Another application is using port 1099");
                System.err.println("\nTry stopping other instances or using a different port.");
            } else if (e instanceof java.net.BindException) {
                System.err.println("\nFailed to bind to the port. This could be due to:");
                System.err.println("1. Insufficient permissions");
                System.err.println("2. The port is already in use");
                System.err.println("\nTry running with administrator privileges or using a different port.");
            }
        }
    }
}
