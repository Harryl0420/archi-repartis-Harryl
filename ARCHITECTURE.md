# Architecture Logicielle — Séance 5

## Partie A — Cas fourni : Plateforme de réservation de salles

### A.1 — Identification des couches

| Couche | Rôle en une phrase | Ce qu'elle ne doit PAS faire |
| :--- | :--- | :--- |
| **Présentation** | Exposer l'interface web, recevoir les requêtes HTTP et traduire les entrées/sorties. | Contenir des règles de gestion (ex: vérifier un chevauchement) ou écrire du SQL. |
| **Métier / Application** | Appliquer les règles de gestion (disponibilité, capacité), orchestrer les actions et déclencher les processus (ex: envoi d'email). | Savoir comment les données sont persistées (structure de la base) ni comment l'UI est rendue. |
| **Accès aux données** | Persister et récupérer les entités (Salles, Réservations) depuis le stockage. | Prendre des décisions métier (ex: décider si une réservation est valide ou non). |

### A.2 — Le piège du cahier des charges

**Style architectural introduit silencieusement :** Architecture centrée sur les données, plus précisément un **Dépôt partagé** (*Shared Repository* / *Integration Database*).

**Risque concret :** Couplage fort et implicite au schéma physique de la base. Si l'équipe qui gère le "système de gestion des emplois du temps" décide un jour de renommer la colonne `capacite_salle` en `nb_places`, ou de changer son type de `INT` à `VARCHAR`, la plateforme de réservation plantera en production. Et ce, **sans qu'aucune ligne de code de la plateforme de réservation n'ait été modifiée**.

### A.3 — Violation de couche plausible (Email)

**Pourquoi c'est une violation :** L'envoi d'un courriel de confirmation est une règle de processus métier, pas une responsabilité de la couche Présentation. Le contrôleur web ne doit savoir que traduire le HTTP.

**Difficulté future :** Si plus tard on doit déclencher un envoi d'email depuis un traitement automatique nocturne (batch de rappels), ce batch ne passera jamais par le contrôleur web. Il faudra alors **dupliquer** le code d'envoi d'email, créant de la redondance et un risque fort d'incohérence (ex: modifier le template d'email dans le web mais oublier de le faire dans le batch).

---

## Partie B — Projet fil rouge (archi-repartis-Harryl)

### B.1 — Inventaire des couches réellement présentes

| Module | Couches réellement identifiées | État réel |
| :--- | :--- | :--- |
| `/socket` | Monolithe procédural | Présentation et Métier fusionnés dans la boucle `while(true)` de `EchoServer`/`CalcServer`. |
| `/rmi` | Présentation + Métier couplés | La logique métier (`add`, `div`) est directement dans `CalculatorServiceImpl`, qui étend `UnicastRemoteObject`. |
| `/rest` | Présentation + Métier séparés | `OperationController` (HTTP) délègue à `OperationService`. Cependant, le parsing JSON manuel est dans le contrôleur. |

### B.2 — Chasse aux violations de couche

1. **Violation "Couche basse connaît la couche haute" (RMI) :** `CalculatorServiceImpl` (Métier) étend `UnicastRemoteObject` et lève `RemoteException`. Le métier est prisonnier de l'infrastructure RMI.
2. **Violation "Saut de couche / Surcharge" (REST) :** `OperationController` (Présentation) effectue un parsing JSON manuel via des `split()` et regex, au lieu de déléguer la désérialisation à une couche d'infrastructure.

### Question de réflexion — Décision sur la dette technique

**Violation choisie :** Le couplage de `CalculatorServiceImpl` à RMI.
**Décision :** Nous choisissons d'**assumer consciemment cette dette technique** pour l'instant.
**Justification (Coût/Bénéfice) :** Corriger cette violation exigerait d'extraire une classe `Calculator` pure (sans dépendance `java.rmi`), puis de refactorer les 3 modules (Socket, RMI, REST) pour qu'ils partagent ce noyau métier. Le coût de ce refactoring transversal est disproportionné par rapport au bénéfice immédiat dans le cadre de ce TP pédagogique, dont l'objectif est de comparer les paradigmes de communication. Nous documentons cette violation pour la corriger proprement si le projet devait évoluer vers une vraie application multi-canaux en production.


