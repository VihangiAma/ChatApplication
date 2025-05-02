package org.example.chat_application.rmi.server;

import org.example.chat_application.rmi.ChatService;
import org.example.chat_application.rmi.client.ClientCallback;

import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChatServiceImpl extends UnicastRemoteObject implements ChatService {

    // Store subscribed clients
    private final Map<String, ClientCallback> subscribedClients = new HashMap<>();
    private final Map<String, String> clientProfiles = new HashMap<>();
    private final SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
    private final List<String> chatHistory = new ArrayList<>();
    private boolean chatStarted = false;

    public ChatServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public synchronized void registerClient(String nickname, String profile, ClientCallback client) throws RemoteException {
        if (!chatStarted && profile.equalsIgnoreCase("admin")) {
            chatStarted = true;
            String time = formatter.format(new Date());
            String startMessage = "Chat started : " + time;
            chatHistory.add(startMessage);
            broadcastMessage(startMessage);
        }

        if (!subscribedClients.containsKey(nickname)) {
            subscribedClients.put(nickname, client);
            clientProfiles.put(nickname, profile);
            String time = formatter.format(new Date());
            String joinMsg = nickname + " has joined : " + time;
            chatHistory.add(joinMsg);
            broadcastMessage(joinMsg);
        }
    }

    @Override
    public synchronized void broadcastMessage(String message, String sender) throws RemoteException {
        String formattedMessage = "\n" + sender + "\n" + message;
        chatHistory.add(formattedMessage);
        for (ClientCallback client : subscribedClients.values()) {
            client.receiveMessage(formattedMessage);
        }
    }

    @Override
    public synchronized void removeClient(String nickname) throws RemoteException {
        if (subscribedClients.containsKey(nickname)) {
            subscribedClients.remove(nickname);
            clientProfiles.remove(nickname);

            String time = formatter.format(new Date());
            String leaveMsg = nickname + " left : " + time;
            chatHistory.add(leaveMsg);
            broadcastMessage(leaveMsg);

            if (subscribedClients.isEmpty() && chatStarted) {
                chatStarted = false;
                String stopTime = formatter.format(new Date());
                String stopMsg = "Chat stopped at : " + stopTime;
                chatHistory.add(stopMsg);
                broadcastMessage(stopMsg);
                saveChatToFile();
            }
        }
    }

    public void broadcastMessage(String message) throws RemoteException {
        chatHistory.add(message); // Keep history
        for (ClientCallback client : subscribedClients.values()) {
            client.receiveMessage(message);
        }
    }
    /*private void broadcastMessage(String message) {
        for (ClientCallback client : subscribedClients.values()) {
            try {
                client.receiveMessage(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }*/

    private void saveChatToFile() {
        try (FileWriter writer = new FileWriter("chat_history.txt")) {
            for (String line : chatHistory) {
                writer.write(line + System.lineSeparator());
            }
            System.out.println("Chat history saved to chat_history.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
