
#### `NOTES.md` (Réponses aux questions de réflexion)
```markdown
# Notes d'observation et Réponses aux questions de réflexion

## Question 1 : Que se passe-t-il si un client reste connecté sans jamais rien envoyer ?
**Observation :** Le thread serveur dédié à ce client reste bloqué indéfiniment sur l'appel à la méthode `in.readLine()`. 
- Aucune exception n'est levée.
- Le thread passe dans un état d'attente d'E/S native (visible dans un thread dump comme `RUNNABLE` mais bloqué au niveau du système d'exploitation).
- **Conséquence :** Dans la version mono-thread (B.1), cela gèle complètement le serveur, empêchant tout autre client d'être servi. Dans la version multi-thread (B.3), seul ce thread spécifique est consommé, mais le serveur principal continue d'accepter de nouvelles connexions (jusqu'à épuisement des threads ou de la mémoire).

## Question 2 : Que se passe-t-il si le client se déconnecte brutalement (Ctrl+C) pendant que le serveur attend une réponse ?
**Observation :** Le système d'exploitation du client termine le processus et envoie un paquet TCP `RST` (Reset) ou `FIN` au serveur.
- Si le serveur tente de lire sur le socket (`in.readLine()`), il obtient l'exception exacte suivante :
  **`java.net.SocketException: Connection reset`**
- Si le serveur tentait d'écrire (`out.println()`), il obtiendrait :
  **`java.net.SocketException: Broken pipe`** (ou `Connection reset by peer` selon l'OS).
- **Gestion :** Grâce au bloc `catch (SocketException e)` dans `ClientHandler`, cette exception est interceptée, loggée proprement, et le thread se termine sans faire planter le serveur global.

## Question 3 (Lien B.1 vs B.3) : Le serveur mono-thread bloquait-il vraiment le deuxième client, ou se contentait-il de le mettre en attente ?
**Nuance importante :** Le serveur mono-thread ne "bloquait" pas activement le deuxième client au niveau applicatif. Il le mettait en **attente** dans la file d'écoute (*backlog queue*) gérée par le système d'exploitation. 
- Le deuxième client établit bien la connexion TCP (le `accept()` du côté OS est fait), mais l'application serveur ne peut pas appeler `accept()` pour récupérer ce socket tant qu'elle est bloquée sur `in.readLine()` pour le premier client.
- La modification B.3 (multi-thread) ne change pas le comportement réseau de base, mais elle permet au thread principal de reboucler immédiatement sur `serverSocket.accept()`, vidant ainsi la file d'attente de l'OS et déléguant le traitement à un nouveau thread, ce qui donne une impression de simultanéité réelle.