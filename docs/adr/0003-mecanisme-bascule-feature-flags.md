# ADR-0003: Mécanisme de bascule pour la migration progressive

## Statut
Accepté

## Contexte
Pour appliquer le patron du figuier étrangleur (Strangler Fig) lors de la migration RMI -> gRPC (ADR-0001), nous avons besoin d'un moyen de router dynamiquement le trafic entre l'ancien et le nouveau service sans redéploiement.

## Décision
Utiliser des Feature Flags (indicateurs de fonctionnalité) stockés dans une configuration externalisée (fichier de propriétés rechargé à chaud ou variable d'environnement) pour basculer le routage au niveau de la façade.

## Alternatives écartées
- **Routage par API Gateway (ex: Nginx, Kong)** : Écartée pour l'instant. Bien que robuste, cela introduit une nouvelle infrastructure à maintenir. Pour un projet de cette taille, un feature flag applicatif est suffisant et moins coûteux.
- **Bascule par pourcentage d'utilisateurs (Canary Release)** : Écartée à ce stade. Nous migrons d'abord par "sous-ensemble ciblé" (ex: un seul client testeur) avant d'envisager un routage aléatoire à 10%.

## Conséquences
- Ajout d'une dépendance légère de gestion de configuration dans la façade.
- La façade devient un point critique : si elle tombe, plus aucun service n'est accessible. Elle doit être hautement disponible.