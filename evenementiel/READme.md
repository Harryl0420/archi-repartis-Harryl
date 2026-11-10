Voici le fichier **`README.md` complet et prêt à l'emploi** pour votre dossier `evenementiel`. 

Il contient à la fois les **instructions techniques** pour lancer le projet (comme demandé dans le livrable) et les **réponses rédigées aux questions de réflexion** des Parties C, D et E, ce qui en fait un document de rendu autonome et complet pour la Séance 7.

Copiez-collez ce contenu dans un fichier nommé `README.md` à la racine de votre dossier `evenementiel`.

---

```markdown
# Module Événementiel - Fil Rouge (Séance 7)

Ce module implémente le service de calcul de manière asynchrone via un courtier de messages (RabbitMQ), rompant avec le couplage temporel des architectures RMI et REST.

## Prérequis
- **Docker** et **Docker Compose** installés et en cours d'exécution.
- **Java 17** ou supérieur.
- **Maven** installé.

---

## 1. Démarrage du courtier de messages (RabbitMQ)

À la racine du dossier `evenementiel`, lancez le conteneur en arrière-plan :

```bash
docker compose up -d
```

**Vérification** : L'interface d'administration est accessible sur [http://localhost:15672](http://localhost:15672)  
*(Identifiants par défaut : `guest` / `guest`)*

Pour arrêter le courtier à la fin de la séance :
```bash
docker compose down
```

---

## 2. Lancement des applications

Ouvrez **trois terminaux séparés** dans VS Code (assurez-vous d'être dans le dossier `evenementiel` dans chacun d'eux).

### Terminal 1 : Consommateur de calcul
Ce processus écoute les demandes, effectue le calcul, et publie le résultat.
```bash
mvn compile exec:java "-Dexec.mainClass=consommateur.ConsommateurCalcul"
```

### Terminal 2 : Consommateur d'audit (Second consommateur)
Ce processus écoute uniquement les résultats pour tenir un journal horodaté, sans modifier la logique de calcul.
```bash
mvn compile exec:java "-Dexec.mainClass=consommateur.ConsommateurAudit"
```

### Terminal 3 : Producteur
Ce processus publie une demande de calcul et se termine immédiatement, sans attendre de réponse.
```bash
mvn compile exec:java "-Dexec.mainClass=producteur.Producteur"
```

---

## 3. Tests d'intégration asynchrones

 **Important** : Pour que le test passe, le **Consommateur de calcul (Terminal 1) doit être en cours d'exécution** en arrière-plan. Sinon, le message sera publié mais personne ne répondra, et le test échouera par timeout.

Lancez la commande suivante dans un quatrième terminal :
```bash
mvn clean test
```
*Le test utilise la bibliothèque **Awaitility** pour attendre de manière bornée (5 secondes maximum) que la réponse asynchrone arrive, évitant ainsi les tests "flaky" (aléatoires).*

---

## 4. Réponses aux questions de réflexion (Livrables théoriques)

### 4.1. Extensibilité (Réponse à la Partie C)
**Question** : *Combien de lignes de code, et dans quel(s) fichier(s), avez-vous dû modifier pour ajouter ce second consommateur (Audit) ?*

**Réponse** : **Zéro ligne modifiée** dans les fichiers existants (`Producteur.java` et `ConsommateurCalcul.java`). Nous avons uniquement **créé** un nouveau fichier (`ConsommateurAudit.java`) et déclaré un nouvel échange (`resultats`). Cela démontre concrètement le découplage structurel du modèle Publish/Subscribe : le producteur ignore totalement l'existence, le nombre ou la nature de ses consommateurs. Ajouter une nouvelle fonctionnalité (comme l'audit) ne nécessite aucune régression sur le code existant.

### 4.2. Nature des tests asynchrones (Réponse à la Partie D)
**Question** : *Pourquoi un test asynchrone "flaky" (qui échoue parfois, sans modification du code) est-il un symptôme architectural et non un simple bug de test ?*

**Réponse** : Un test "flaky" révèle le passage fondamental d'une **cohérence immédiate** (synchrone, comme en RMI/REST) à une **cohérence à terme** (*eventual consistency*). En synchrone, l'ordre d'exécution et la causalité sont garantis par la pile d'appels bloquante. En événementiel, le producteur et le consommateur sont des processus totalement indépendants. 

Un test qui échoue aléatoirement en affirmant une valeur "trop tôt" ne contient pas un bug de logique ; il met en lumière l'absence de garantie temporelle inhérente à l'architecture. Utiliser une attente bornée explicite (comme avec Awaitility) n'est pas un "hack" pour faire passer le test, mais l'acceptation de la nature asynchrone du système : on teste la propriété finale (le résultat arrive *in fine*), et non l'instantanéité.

### 4.3. Tableau comparatif polyglotte (Réponse à la Partie E)
*Rempli à partir des observations concrètes de ce TP, et non de généralités de cours.*

| Critère | RMI (Séance 2-3) | REST (Séance 4) | Événementiel (Séance 7 - Notre implémentation) |
| :--- | :--- | :--- | :--- |
| **Couplage temporel** | **Fort** : Le client bloque en attendant la réponse du serveur. | **Fort** : Le client HTTP attend la réponse (risque de timeout). | **Nul** : Le producteur publie et se termine immédiatement. Le consommateur traite à son rythme. |
| **Effort pour ajouter un traitement supplémentaire** | **Élevé** : Il faut modifier l'interface RMI, l'implémentation et potentiellement les clients. | **Moyen** : Il faut ajouter un nouvel endpoint ou modifier le contrôleur existant. | **Nul sur l'existant** : Ajout de `ConsommateurAudit.java` sans toucher à une seule ligne du producteur ou du premier consommateur. |
| **Complexité du test d'intégration** | **Faible** : Appel de méthode direct, `assertEquals` immédiat sur la valeur de retour. | **Faible/Moyenne** : Requête HTTP, vérification du code statut et du corps JSON. | **Élevée** : Nécessite une attente bornée explicite (Awaitility) et de faire tourner le consommateur en arrière-plan pour éviter les échecs aléatoires. |
| **Visibilité d'un résultat immédiat pour l'appelant** | **Immédiate** : La méthode retourne la valeur (ex: `double`). | **Immédiate** : La réponse HTTP contient le résultat (ex: 201 Created + JSON). | **Aucune** : Le producteur ne sait pas si, quoi, ni quand le calcul sera fait. La corrélation se fait via l'`idOperation` dans un second message. |
```

