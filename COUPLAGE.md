# Cartographie du couplage actuel — Séance 9

## Tableau d'analyse des composants

| Composant | Couplage temporel | Couplage technologique | Couplage de contrat |
| :--- | :--- | :--- | :--- |
| **Module RMI** | **Fort** : L'appel `stub.add(a,b)` bloque le thread client jusqu'à la réponse. Si le serveur tombe, le client gèle. | **Fort** : Exige Java des deux côtés. Impossible d'appeler le service depuis un client Node.js ou Python sans passerelle. | **Fort** : Changer la signature de `CalculatorService` (ex: ajouter un paramètre) casse immédiatement tous les clients compilés. |
| **Module REST** | **Fort** : Appel HTTP synchrone bloquant. Le client attend la réponse 200/422. | **Faible** : Utilise HTTP/JSON. Testable avec un simple `curl` depuis n'importe quel langage ou OS. | **Modéré** : Ajout de champs JSON toléré, mais renommage d'une route (`/operations` -> `/calculs`) casse les clients existants. |
| **Module Événementiel** | **Faible** : Le producteur publie sur RabbitMQ et se termine. Le consommateur traite à son rythme. | **Faible** : Tout langage possédant un client AMQP peut s'abonner aux files. | **Modéré** : Le format JSON des événements (`{"op":"add"}`) doit être respecté. L'ajout de champs est safe, la suppression casse les consommateurs. |
| **Module gRPC** | **Fort** : L'appel unaire `stub.additionner()` est bloquant (sauf streaming). | **Faible** : Tooling mature pour Java, Go, Python, C++. Indépendant du langage. | **Faible** : Protocol Buffers permet l'ajout de champs optionnels sans casser les anciens clients (compatibilité ascendante native). |