# ADR-0001: Migration du service de calcul de RMI vers gRPC

## Statut
Accepté

## Contexte
Le service RMI actuel impose Java des deux côtés et casse tout client existant au moindre changement de signature de méthode. Le comparatif polyglotte de la séance 8 montre que gRPC offre une latence similaire (~1-3ms) mais avec une interopérabilité multi-langage et une compatibilité ascendante native grâce aux numéros de champ Protocol Buffers.

## Décision
Migrer progressivement le service de calcul de RMI vers gRPC, via une façade de routage et des feature flags, sans interruption de service pour les clients Java existants.

## Alternatives écartées
- **Réécriture totale en une fois (Big Bang)** : Écartée. Risque analogue au cas Netscape vu en cours. Aucune réversibilité en cas de bug en production sur le nouveau service.
- **Migration vers REST plutôt que gRPC** : Écartée. Bien que REST soit plus interopérable, nos mesures de la séance 8 montrent que gRPC réduit la taille des messages de 70% et la latence de 50% par rapport à REST/JSON, ce qui est critique pour notre volume d'appels internes.

## Conséquences
- Double maintenance temporaire du service RMI et gRPC pendant la transition.
- Nécessite la mise en place d'un mécanisme de bascule réversible (feature flag).
- Les clients Java devront mettre à jour leurs dépendances de build pour inclure le client gRPC.