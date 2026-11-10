package sockets;

import java.io.*;
import java.net.*;

public class EchoClient {

    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int port = 5000;

        try (
            Socket socket = new Socket(host, port);
            BufferedReader clavier = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            System.out.print("Message à envoyer : ");
            String message = clavier.readLine();

            out.println(message);              // envoi
            String reponse = in.readLine();     // lecture de la réponse
            System.out.println("Réponse du serveur : " + reponse);
        } catch (IOException e) {
            System.err.println("Erreur de communication avec le serveur : " + e.getMessage());
        }
       
    }
}