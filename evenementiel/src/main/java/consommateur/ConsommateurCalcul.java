package consommateur;

import com.rabbitmq.client.*;
import java.nio.charset.StandardCharsets;

public class ConsommateurCalcul {
    private final static String EXCHANGE_DEMANDES = "calculs";
    private final static String EXCHANGE_RESULTATS = "resultats";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // 1. S'abonner aux demandes
        channel.exchangeDeclare(EXCHANGE_DEMANDES, "fanout");
        String queueName = channel.queueDeclare().getQueue(); // File temporaire unique
        channel.queueBind(queueName, EXCHANGE_DEMANDES, "");

        System.out.println(" [*] Consommateur Calcul en attente de demandes...");

        // 2. Traiter les messages
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Reçu demande : " + message);

            // Parsing simplifié du JSON
            String id = extraireValeur(message, "id");
            String op = extraireValeur(message, "op");
            double a = Double.parseDouble(extraireValeur(message, "a"));
            double b = Double.parseDouble(extraireValeur(message, "b"));

            double resultat = 0;
            switch (op) {
                case "add": resultat = a + b; break;
                case "sub": resultat = a - b; break;
                case "mul": resultat = a * b; break;
                case "div": resultat = (b != 0) ? a / b : 0; break;
                default: System.out.println(" [!] Opération inconnue"); return;
            }

            // 3. Publier le résultat avec le MÊME ID (corrélation)
            String reponse = "{\"id\":\"" + id + "\", \"resultat\":" + resultat + "}";
            
            channel.exchangeDeclare(EXCHANGE_RESULTATS, "fanout");
            channel.basicPublish(EXCHANGE_RESULTATS, "", null, reponse.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [->] Résultat publié : " + reponse);
        };

        // Démarrer la consommation (true = accusé de réception automatique)
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }

    // Utilitaire pour extraire une valeur du JSON sans bibliothèque externe
    private static String extraireValeur(String json, String cle) {
        String search = "\"" + cle + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) {
            search = "\"" + cle + "\":"; // Pour les nombres
            start = json.indexOf(search);
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            return json.substring(start + search.length(), end).replace("\"", "").trim();
        }
        int end = json.indexOf("\"", start + search.length());
        return json.substring(start + search.length(), end);
    }
}