package org.example.chat_application.rmi.client;
import java.rmi.Remote;
import java.rmi.RemoteException;
import org.example.chat_application.rmi.client.ClientCallback;

public interface ClientCallback extends Remote {


    void receiveMessage(String message) throws RemoteException;
}

