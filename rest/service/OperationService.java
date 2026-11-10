package rest.service;

import rest.Model.Operation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class OperationService {
    // Thread-safe pour gérer les requêtes HTTP concurrentes
    private final List<Operation> history = new CopyOnWriteArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Operation compute(String type, double a, double b) {
        double result = 0;
        String upperType = type.toUpperCase();
        
        switch (upperType) {
            case "ADD": result = a + b; break;
            case "SUB": result = a - b; break;
            case "MUL": result = a * b; break;
            case "DIV":
                if (b == 0) {
                    throw new ArithmeticException("Division par zéro impossible");
                }
                result = a / b;
                break;
            default:
                throw new IllegalArgumentException("Opération inconnue : " + type);
        }

        Operation newOp = new Operation(
            idGenerator.getAndIncrement(),
            upperType,
            a,
            b,
            result,
            System.currentTimeMillis()
        );
        
        history.add(newOp);
        return newOp;
    }

    public List<Operation> getAll() {
        return new ArrayList<>(history); // Copie défensive
    }

    public Optional<Operation> getById(long id) {
        return history.stream().filter(op -> op.getId() == id).findFirst();
    }
}