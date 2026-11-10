package producteur;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.nio.charset.StandardCharsets;

public class Producteur {
    // Nom de l'échange (exchange) tel que défini dans la fiche de TP
    private final static String EXCHANGE_NAME = "calculs";

    public static void main(String[] argv) throws Exception {
        // 1. Configuration de la connexion au courtier local
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("guest");
        factory.setPassword("guest");

        // 2. Création de la connexion et du canal (try-with-resources pour fermer proprement)
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            
            // 3. Déclaration de l'échange de type "fanout" (broadcast)
            // Si l'échange n'existe pas, RabbitMQ le crée. S'il existe déjà, rien ne se passe.
            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

            // 4. Préparation du message (format JSON simple)
            String idOperation = "op-" + System.currentTimeMillis();
            String message = "{\"id\":\"" + idOperation + "\", \"op\":\"add\", \"a\":5, \"b\":3}";

            // 5. Publication du message sur l'échange
            // Le deuxième paramètre (routingKey) est vide car c'est un fanout (on diffuse à tout le monde)
            channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes(StandardCharsets.UTF_8));
            
            System.out.println(" [x] Demande publiée : " + message);
            System.out.println(" [i] En attente de consommateurs... (le producteur ne reçoit pas de réponse immédiate)");
        }
    }
}