package consommateur;

import com.rabbitmq.client.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class ConsommateurAudit {
    private final static String EXCHANGE_RESULTATS = "resultats";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // S'abonner UNIQUEMENT aux résultats
        channel.exchangeDeclare(EXCHANGE_RESULTATS, "fanout");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_RESULTATS, "");

        System.out.println(" [*] Consommateur Audit en attente de résultats pour journalisation...");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            // Journalisation avec horodatage
            System.out.println(" [AUDIT] " + LocalDateTime.now() + " -> Calcul terminé : " + message);
        };

        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }
}