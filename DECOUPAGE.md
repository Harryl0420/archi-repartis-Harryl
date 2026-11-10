# Découpage Microservices et Justifications — Séance 6

## A.1 — Inventaire des capacités métier
- **Calcul** : Exécuter les opérations arithmétiques (add, sub, mul, div). Service sans état (stateless).
- **Historique** : Conserver, lister et retrouver les opérations passées. Service avec état (stateful), possède les données.

## A.2 — Proposition de découpage

| Service proposé | Responsabilité | Données possédées | Dépend de |
| :--- | :--- | :--- | :--- |
| `calc-service` | Exécuter les opérations de calcul et valider les règles métier (ex: division par zéro). | Aucune donnée persistante propre (service sans état). | `history-service` (pour enregistrer chaque opération effectuée). |
| `history-service` | Conserver, lister et exposer l'historique des opérations. | Table des opérations horodatées (ID, type, opérandes, résultat, timestamp). | Aucun (service racine pour les données). |

## A.3 — Alternatives de découpage écartées

### Alternative 1 : Le Monolithe (Fusionner calc et history)
**Description** : Ne faire qu'un seul service `calcul-history-service` qui fait tout.
**Pourquoi écarté** : Cela recrée un couplage fort au déploiement. Si la base de données de l'historique est lente ou en maintenance, le service de calcul (qui pourrait autrement être très rapide et scalable) devient indisponible. Nous voulons pouvoir scaler le `calc-service` indépendamment (ex: ajouter des instances lors d'un pic de charge) sans toucher à l'historique.

### Alternative 2 : Découpage par opération technique (4 services de calcul)
**Description** : Créer `add-service`, `sub-service`, `mul-service`, `div-service`.
**Pourquoi écarté** : C'est un découpage purement technique, pas métier. Ces quatre opérations partagent le même cycle de vie, les mêmes règles de validation et le même domaine. Les séparer multiplierait la complexité opérationnelle (4 déploiements, 4 bases de code) pour aucun gain d'autonomie métier. C'est le symptôme d'un "monolithe distribué".

### Alternative 3 : Découpage par couche technique (Anti-pattern)
**Description** : Créer un `api-service` (contrôleurs), un `logic-service` (règles) et un `data-service` (accès BDD).
**Pourquoi écarté** : C'est exactement le piège du "monolithe distribué" vu en cours. La moindre évolution (ex: ajouter un champ "commentaire" à une opération) nécessiterait de modifier et redéployer les 3 services en même temps. On perd l'autonomie des équipes sans gagner en scalabilité.