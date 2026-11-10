package sockets;

import java.io.*;
import java.net.*;

public class EchoServer {

    public static void main(String[] args) throws IOException {
        int port = 5000;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serveur en écoute sur le port " + port);

            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("Client connecté : " + client.getInetAddress());

                try (
                    BufferedReader in = new BufferedReader(
                        new InputStreamReader(client.getInputStream())
                    );
                    PrintWriter out = new PrintWriter(client.getOutputStream(), true)
                ) {
                    String ligne = in.readLine();
                    System.out.println("Reçu : " + ligne);
                    out.println(ligne);
                } finally {
                    client.close();
                    System.out.println("Client déconnecté");
                }
            }
        }
    }
}