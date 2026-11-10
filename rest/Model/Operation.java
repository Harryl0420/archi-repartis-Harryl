package rest.Model;

public class Operation {
    private final long id;
    private final String type;
    private final double a;
    private final double b;
    private final double result;
    private final long timestamp;

    public Operation(long id, String type, double a, double b, double result, long timestamp) {
        this.id = id;
        this.type = type;
        this.a = a;
        this.b = b;
        this.result = result;
        this.timestamp = timestamp;
    }

    public long getId() { return id; }
    public String getType() { return type; }
    public double getA() { return a; }
    public double getB() { return b; }
    public double getResult() { return result; }
    public long getTimestamp() { return timestamp; }

    // Méthode simple pour générer du JSON sans bibliothèque externe
    public String toJson() {
        return String.format(
            "{\"id\":%d,\"type\":\"%s\",\"a\":%.2f,\"b\":%.2f,\"result\":%.2f,\"timestamp\":%d}",
            id, type, a, b, result, timestamp
        );
    }
}