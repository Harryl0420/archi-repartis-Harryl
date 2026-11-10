# Module gRPC — Séance 8

Ce module implémente le service de calcul en utilisant gRPC et Protocol Buffers, offrant une alternative performante et à contrat strict aux paradigmes RMI, REST et Événementiel.

## Prérequis
- Java 17+
- Maven

## 1. Générer le code à partir du contrat `.proto`
Le code client et serveur (stubs) est généré automatiquement à chaque compilation Maven grâce au plugin `protobuf-maven-plugin`.
```bash
mvn clean compile

2. Lancer le serveur gRPC

mvn exec:java "-Dexec.mainClass=grpc.ServeurGrpc"

3. Lancer le client de test (dans un autre terminal)

mvn exec:java "-Dexec.mainClass=grpc.ClientGrpc"

