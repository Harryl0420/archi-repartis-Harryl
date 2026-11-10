# ADR-0002: Stratégie de versionnage de l'API REST

## Statut
Accepté

## Contexte
L'API REST actuelle (`/operations`) est utilisée par plusieurs clients externes. Nous devons ajouter un champ obligatoire `timestamp` dans la réponse, ce qui constitue un changement cassant (breaking change) pour les clients qui parsent strictement le JSON.

## Décision
Adopter le versionnage explicite dans l'URI (`/v1/operations`, `/v2/operations`) pour gérer les changements cassants, avec une période de dépréciation de 6 mois pour l'ancienne version.

## Alternatives écartées
- **Versionnage par en-tête HTTP (`Accept: application/vnd.api.v2+json`)** : Écartée. Bien que plus propre sémantiquement (l'URI de la ressource ne change pas), cette approche est moins visible, plus difficile à déboguer via un navigateur ou `curl`, et souvent mal implémentée par les clients.
- **Compatibilité ascendante forcée (rendre `timestamp` optionnel)** : Écartée. Le champ `timestamp` est métier critique pour l'audit. Le rendre optionnel créerait de la dette technique et des incohérences de données.

## Conséquences
- Nécessite de maintenir deux routes (`/v1/` et `/v2/`) en parallèle pendant 6 mois.
- Obligation de communiquer clairement la date de fin de support de `/v1/` aux consommateurs externes.