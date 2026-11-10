## Partie B — Architecture réelle du projet fil rouge

### B.1 — Inventaire des couches réellement présentes

| Module | Couches réellement identifiées | État réel du code |
| :--- | :--- | :--- |
| **`/socket`** | Monolithe procédural | Aucune séparation. La boucle `while(true)` de `EchoServer`/`CalcServer` gère à la fois l'acceptation de la connexion (Présentation), la lecture des flux et le traitement (Métier). |
| **`/rmi`** | Présentation + Métier couplés | La logique métier (`add`, `div`) est directement implémentée dans `CalculatorServiceImpl`, qui est une classe d'infrastructure RMI. |
| **`/rest`** | Présentation + Métier (partiellement séparés) | `OperationController` délègue le calcul à `OperationService` (bon point). Cependant, le contrôleur effectue lui-même le parsing JSON manuel via des `split()` (surcharge). |

### B.2 — Chasse aux violations de couche

1. **Violation "Couche basse connaît la couche haute" (Module RMI) :** 
   La classe `CalculatorServiceImpl` contient la logique métier pure mais étend `UnicastRemoteObject` et lève des `RemoteException`. Le métier est prisonnier de l'infrastructure réseau.
2. **Violation "Fusion totale des couches" (Module Socket) :** 
   Dans `CalcServer.java`, il n'existe pas de classe de service. Le thread qui gère le socket prend directement les décisions métier.
3. **Violation "Surcharge de la couche Présentation" (Module REST) :** 
   `OperationController` contient la méthode `parseSimpleJson` qui utilise des regex et des `split`. La désérialisation des données est une responsabilité d'infrastructure, pas du contrôleur HTTP.

### Question de réflexion — Décision sur la dette technique

**Violation choisie :** Le couplage de `CalculatorServiceImpl` à l'infrastructure RMI.
**Décision :** Nous choisissons d'**assumer consciemment cette dette technique** pour l'instant.
**Justification (Coût/Bénéfice) :** Corriger cette violation exigerait d'extraire une classe `Calculator` pure (sans dépendance `java.rmi`), puis de refactorer les 3 modules pour qu'ils partagent ce noyau. Le coût de ce refactoring transversal est disproportionné par rapport au bénéfice immédiat dans le cadre de ce TP, dont l'objectif est de comparer les paradigmes de communication. Nous documentons cette violation pour la corriger proprement si le projet devait évoluer vers une application multi-canaux en production.