package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import rmi.client.NotificationListener;

public interface HistoryService extends Remote {
    void record(String entry) throws RemoteException;
    List<String> getAll() throws RemoteException;

    // Nouvelle méthode pour le pattern Observer/Callback
    void subscribe(NotificationListener listener) throws RemoteException;
}
