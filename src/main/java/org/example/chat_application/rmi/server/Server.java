package org.example.chat_application.rmi.server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class Server {
    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099); // Start RMI registry
            Naming.rebind("chat", new ChatServiceImpl());
            System.out.println("Chat server started...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
