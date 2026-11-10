package sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class CalcServer {
    public static void main(String[] args) {
        int port = 5001;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serveur Calculatrice en écoute sur le port " + port);
            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("Nouveau client connecté : " + client.getInetAddress());
                
                // B.3 : Un thread par connexion pour gérer plusieurs clients simultanément
                Thread clientThread = new Thread(new ClientHandler(client));
                clientThread.start();
            }
        } catch (IOException e) {
            System.err.println("Impossible de démarrer le serveur sur le port " + port);
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private final Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            Socket closedClient = clientSocket
        ) {
            String request = in.readLine();
            if (request == null) {
                System.out.println("Client déconnecté proprement (EOF).");
                return;
            }
            
            String response = processRequest(request);
            out.println(response);
            
        } catch (SocketException e) {
            // Capture les déconnexions brutales
            System.out.println("Déconnexion brutale du client : " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Erreur E/S avec le client : " + e.getMessage());
        }
    }

    private String processRequest(String request) {
        if (request.trim().isEmpty()) {
            return "ERREUR: Requête vide";
        }
        
        String[] parts = request.split(";");
        if (parts.length != 3) {
            return "ERREUR: Format invalide. Attendu : OPERATION;A;B";
        }
        
        String operation = parts[0].trim().toUpperCase();
        String strA = parts[1].trim();
        String strB = parts[2].trim();
        
        double a, b;
        try {
            a = Double.parseDouble(strA);
            b = Double.parseDouble(strB);
        } catch (NumberFormatException e) {
            return "ERREUR: Les opérandes A et B doivent être des nombres valides.";
        }
        
        switch (operation) {
            case "ADD": return "RÉSULTAT: " + (a + b);
            case "SUB": return "RÉSULTAT: " + (a - b);
            case "MUL": return "RÉSULTAT: " + (a * b);
            case "DIV":
                if (b == 0) {
                    return "ERREUR: Division par zéro impossible.";
                }
                return "RÉSULTAT: " + (a / b);
            default:
                return "ERREUR: Opération '" + operation + "' inconnue. Supportées : ADD, SUB, MUL, DIV.";
        }
    }
}
