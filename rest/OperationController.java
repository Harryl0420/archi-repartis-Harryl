package rest;

import rest.Model.Operation;
import rest.service.OperationService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class OperationController {

    private final OperationService service = new OperationService();

    public static void main(String[] args) throws IOException {
        OperationController server = new OperationController();
        server.start(8080);
    }

    public void start(int port) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Route POST /operations
        httpServer.createContext("/operations", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    handlePost(exchange);
                } else if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    handleGetAll(exchange);
                } else {
                    sendResponse(exchange, 405, "Méthode non autorisée");
                }
            }
        });

        // Route GET /operations/{id}
        httpServer.createContext("/operations/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    handleGetById(exchange);
                } else {
                    sendResponse(exchange, 405, "Méthode non autorisée");
                }
            }
        });

        httpServer.setExecutor(null); // Utilise le thread pool par défaut
        httpServer.start();
        System.out.println("Serveur REST démarré sur http://localhost:" + port);
    }

    // --- POST /operations ---
    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            // 1. Lire le corps de la requête
            String body = readBody(exchange.getRequestBody());
            
            // 2. Parser le JSON manuellement (format attendu: {"type":"ADD", "a":5, "b":3})
            String[] parts = parseSimpleJson(body);
            String type = parts[0];
            double a = Double.parseDouble(parts[1]);
            double b = Double.parseDouble(parts[2]);

            // 3. Appel au service métier
            Operation result = service.compute(type, a, b);

            // 4. Réponse 201 Created avec en-tête Location
            exchange.getResponseHeaders().set("Location", "/operations/" + result.getId());
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            sendResponse(exchange, 201, result.toJson());

        } catch (IllegalArgumentException e) {
            // TODO énoncé : corps JSON incomplet ou opération inconnue -> 400
            sendResponse(exchange, 400, "{\"error\": \"Requête invalide : " + e.getMessage() + "\"}");
        } catch (ArithmeticException e) {
            // TODO énoncé : renvoyer 422 avec un corps d'erreur explicite, pas une 500 générique
            sendResponse(exchange, 422, "{\"error\": \"Erreur métier : " + e.getMessage() + "\"}");
        } catch (Exception e) {
            sendResponse(exchange, 400, "{\"error\": \"Format JSON invalide ou manquant\"}");
        }
    }

    // --- GET /operations ---
    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<Operation> operations = service.getAll();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < operations.size(); i++) {
            json.append(operations.get(i).toJson());
            if (i < operations.size() - 1) json.append(",");
        }
        json.append("]");
        
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        sendResponse(exchange, 200, json.toString());
    }

    // --- GET /operations/{id} ---
    private void handleGetById(HttpExchange exchange) throws IOException {
        // Extraire l'ID de l'URI (ex: "/operations/1" -> "1")
        String path = exchange.getRequestURI().getPath();
        String idStr = path.substring("/operations/".length());

        try {
            long id = Long.parseLong(idStr);
            Optional<Operation> optOp = service.getById(id);
            
            if (optOp.isPresent()) {
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                sendResponse(exchange, 200, optOp.get().toJson());
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Opération avec id " + id + " introuvable\"}");
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\": \"ID doit être un nombre entier\"}");
        }
    }

    // --- Utilitaires ---
    private String readBody(InputStream is) throws IOException {
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int length;
    while ((length = is.read(buffer)) != -1) {
        result.write(buffer, 0, length);
    }
    return result.toString(StandardCharsets.UTF_8.name());
}

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // Parseur JSON minimaliste pour le TP (évite d'ajouter Gson/Jackson)
    // Attend strictement : {"type":"...", "a":..., "b":...}
    private String[] parseSimpleJson(String json) {
        json = json.replaceAll("\\s+", ""); // Supprimer les espaces
        String type = json.split("\"type\":\"")[1].split("\"")[0];
        String aStr = json.split("\"a\":")[1].split(",")[0];
        String bStr = json.split("\"b\":")[1].split("}")[0];
        return new String[]{type, aStr, bStr};
    }
}