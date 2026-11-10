package sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class CalcClient {
    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 5001;
        
        try (
            Socket socket = new Socket(hostname, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("Connecté au serveur calculatrice.");
            System.out.println("Format attendu : OPERATION;A;B (ex: ADD;4;7)");
            System.out.println("Tapez 'quit' pour quitter.");
            
            while (true) {
                System.out.print("\nEntrez une opération : ");
                String userInput = scanner.nextLine().trim();
                
                if (userInput.equalsIgnoreCase("quit")) {
                    break;
                }
                
                out.println(userInput);
                String response = in.readLine();
                System.out.println("Réponse du serveur : " + response);
            }
        } catch (IOException e) {
            System.err.println("Erreur de communication avec le serveur : " + e.getMessage());
        }
    }
}
