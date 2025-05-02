package org.example.chat_application.rmi;
import java.rmi.Remote;
import java.rmi.RemoteException;
import org.example.chat_application.rmi.client.ClientCallback;

public interface ChatService extends Remote {
    void registerClient(String nickname, String profile, ClientCallback client) throws RemoteException;
    void broadcastMessage(String message, String sender) throws RemoteException;
    void removeClient(String nickname) throws RemoteException;
}



