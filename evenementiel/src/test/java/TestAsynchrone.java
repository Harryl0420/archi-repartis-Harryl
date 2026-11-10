import com.rabbitmq.client.*;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestAsynchrone {

    private Connection connection;
    private Channel channel;
    private final String EXCHANGE_DEMANDES = "calculs";
    private final String EXCHANGE_RESULTATS = "resultats";
    
    // Variable thread-safe pour stocker le résultat reçu de manière asynchrone
    private AtomicReference<String> resultatRecu = new AtomicReference<>(null);

    @BeforeEach
    public void setUp() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
        channel = connection.createChannel();

        // S'assurer que les échanges existent
        channel.exchangeDeclare(EXCHANGE_DEMANDES, "fanout");
        channel.exchangeDeclare(EXCHANGE_RESULTATS, "fanout");

        // Le test s'abonne temporairement aux résultats pour pouvoir les vérifier
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_RESULTATS, "");

        channel.basicConsume(queueName, true, (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [TEST] Réponse interceptée : " + message);
            resultatRecu.set(message); // On stocke le résultat dès qu'il arrive
        }, consumerTag -> {});
    }

    @Test
    public void testCalculAsynchrone() throws Exception {
        // 1. Publier la demande (comme le ferait le Producteur)
        String idOperation = "test-123";
        String message = "{\"id\":\"" + idOperation + "\", \"op\":\"add\", \"a\":5, \"b\":3}";
        channel.basicPublish(EXCHANGE_DEMANDES, "", null, message.getBytes(StandardCharsets.UTF_8));
        System.out.println(" [TEST] Demande publiée : " + message);

        // 2. ATTENTE BORNÉE (Le cœur du sujet)
        // On attend AU PLUS 5 secondes que resultatRecu ne soit plus null ET contienne le bon résultat
        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .pollInterval(500, TimeUnit.MILLISECONDS) // Vérifie toutes les 500ms
            .until(() -> resultatRecu.get() != null && resultatRecu.get().contains("\"resultat\":8.0"));

        // 3. Assertion finale (si on arrive ici, c'est que la condition ci-dessus est vraie)
        assertTrue(resultatRecu.get().contains(idOperation), "L'ID de corrélation doit correspondre");
        System.out.println(" [TEST] Succès : Le calcul asynchrone a été vérifié !");
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (channel != null && channel.isOpen()) channel.close();
        if (connection != null && connection.isOpen()) connection.close();
    }
}