# Plan de migration progressive — Séance 9

## Scénario choisi
Migration du service de calcul historique (RMI) vers gRPC, sans interruption de service, en appliquant le patron du figuier étrangleur (Strangler Fig).

## Plan en étapes réversibles

| Étape | Ce qui bascule | Condition de retour en arrière |
| :--- | :--- | :--- |
| **1. Déploiement de la Façade** | Une façade (proxy léger) est déployée devant l'implémentation RMI existante. 100% du trafic transite par la façade et est routé vers RMI. | Retrait immédiat de la façade. Les clients se reconnectent directement à RMI. Aucun impact. |
| **2. Bascule ciblée via Feature Flag** | Le feature flag `use_grpc` est activé pour un sous-ensemble ciblé (ex: un nouveau client de test). Le trafic de ce client est routé vers le nouveau service gRPC. Les autres restent sur RMI. | Désactivation du flag `use_grpc`. Retour instantané à 100% RMI pour tous les clients, sans redéploiement. |
| **3. Bascule totale avec secours** | 100% du trafic est routé vers gRPC. L'ancien service RMI est conservé en mode "standby" (éteint mais déployable en 2 minutes). | Réactivation du flag `use_grpc=false` et redémarrage de RMI. Retour à la situation de l'étape 1. |
| **4. Retrait définitif** | Le code RMI, le registre RMI et les dépendances associées sont supprimés du codebase. | **Irréversible.** Condition préalable : l'étape 3 doit être stable depuis au moins 2 semaines en production, sans aucun rollback. |

## Synthèse : Pourquoi ce plan évite le scénario Netscape

En 1998, Netscape a perdu son marché en pariant tout sur une réécriture totale de 3 ans ("Big Bang"), pendant laquelle leur produit existant a stagné face à la concurrence. 

Notre plan de migration évite ce piège existentiel grâce à trois mécanismes clés :
1. **La réversibilité** : À chaque étape (sauf la dernière), nous pouvons annuler la migration en quelques secondes via un simple feature flag, sans toucher au code. Le risque est transformé en une série de petits paris contrôlés.
2. **La cohabitation** : Nous ne jetons pas l'ancien système. Il continue de servir la majorité des utilisateurs pendant que le nouveau système prouve sa stabilité en production sur un périmètre restreint.
3. **L'amélioration continue** : Contrairement à Netscape qui a livré un "big bang" après 3 ans de silence, nous livrons de la valeur incrémentale. Le nouveau service gRPC peut être amélioré et testé en conditions réelles dès l'étape 2.

En acceptant le coût temporaire de la double maintenance (RMI + gRPC), nous échangeons un risque de mort de l'entreprise contre un coût opérationnel maîtrisé et réversible. C'est l'essence même de l'ingénierie logicielle moderne : concevoir pour l'évolution, pas pour la perfection théorique.