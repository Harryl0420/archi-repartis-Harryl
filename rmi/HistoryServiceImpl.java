package rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import rmi.client.NotificationListener;

public class HistoryServiceImpl extends UnicastRemoteObject implements HistoryService {

    private final List<String> history;
    private final List<NotificationListener> listeners;

    private HistoryService historyService;

public void setHistoryService(HistoryService historyService) {
    this.historyService = historyService;
}

    public HistoryServiceImpl() throws RemoteException {
        super();
        // CORRECTION : Utilisation d'une liste synchronisée pour éviter les problèmes de concurrence
        history = Collections.synchronizedList(new ArrayList<>());
        listeners = new CopyOnWriteArrayList<>();
    }

    @Override
    public void subscribe(NotificationListener listener) throws RemoteException {
        listeners.add(listener);
    }

    @Override
    public void record(String entry) throws RemoteException {
        history.add(entry);
        notifyListeners(entry);
    }

    private void notifyListeners(String entry) {
        // On itère sur une copie pour éviter toute ConcurrentModificationException
        for (NotificationListener l : new ArrayList<>(listeners)) {
            try {
                l.onNewOperation(entry);
            } catch (RemoteException e) {
                listeners.remove(l); // désabonnement silencieux : le problème est CE client, pas les autres
            }
        }
    }

    @Override
    public List<String> getAll() throws RemoteException {
        return new ArrayList<>(history);
    }
}