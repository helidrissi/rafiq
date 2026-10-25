# Spec NNNN · Titre court

| | |
|---|---|
| **Statut** | Brouillon · En relecture · Validée (porte 1) |
| **Auteurs** | CTO, co-CEO |
| **Relecture terrain** | Pr Tazi, le JJ/MM/AAAA |
| **Milestone visé** | `An X · MY — …` |
| **ADR concernés** | ADR-00X, … |

> Copier ce fichier en `docs/specs/NNNN-titre.md`. Remplir les sections dans l'ordre : on ne parle de technique qu'à la section 6.

## 1. Le problème

Quel problème du cabinet on résout, pour qui, et pourquoi maintenant. Deux à cinq lignes, avec un fait du terrain.

## 2. Les utilisateurs

| Utilisateur | Ce qu'il veut | Ce qu'il craint |
|---|---|---|
| Patient / maman | | |
| Secrétaire | | |
| Médecin | | |

## 3. Les parcours

Un parcours par situation, écrit du point de vue de l'utilisateur. Le QA les rejouera tels quels.

### Parcours A · Nom du parcours

- **Persona** : qui, quand, dans quelle langue (français, darija, arabizi).
- **Déroulé** :
  1. L'utilisateur fait…
  2. Rafiq répond…
  3. …
- **Résultat attendu** : ce que l'utilisateur voit, et ce qui existe en base à la fin.

### Parcours B · …

### Cas limites

- Message ambigu, faible confiance du LLM.
- Message médical (y compris de nuit).
- Message vocal, image ou message vide.
- Doublon du même message.
- Créneau pris entre la proposition et la confirmation.

## 4. Les règles non négociables

- ADR-006 : la conversation passe par la machine à états.
- ADR-007 : seuls des templates validés sont envoyés au patient.
- ADR-008 : mots-clés médicaux évalués avant le LLM.
- Dates stockées en UTC, affichées en `Africa/Casablanca`.
- Aucune donnée patient ni numéro en clair dans les logs. Données de test fictives.
- Règles propres à cette spec : …

## 5. Hors scope

Ce que cette spec ne fait pas, explicitement, et où ce sera traité.

- …

## 6. Notes techniques

Uniquement ce qui contraint la réalisation : point d'entrée, tables touchées, dépendance externe, état actuel du code. Pas de solution détaillée : c'est le rôle du PM et du Fullstack.

## 7. Critères de succès

Chaque critère est vérifiable par un test ou une mesure.

- [ ] Le parcours A, rejoué de bout en bout par le webhook, produit … et l'état en base est …
- [ ] …
- [ ] `mvn verify` vert, Yassine ✅.

## 8. Questions ouvertes

| Question | Qui tranche | Réponse |
|---|---|---|
| | | |

## Porte 1 · Checklist de validation

- [ ] Les parcours sont écrits du point de vue des utilisateurs.
- [ ] Le Pr Tazi a relu les parcours.
- [ ] Le hors scope est explicite.
- [ ] Chaque critère de succès est vérifiable.
- [ ] Les questions ouvertes bloquantes sont tranchées.
