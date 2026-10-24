# Équipe Engineering (agents)

| Agent | Rôle | Écrit dans le repo ? |
|---|---|---|
| `pm` | Objectif → issues avec critères d'acceptation testables | Non (crée des issues après accord du CTO) |
| `backend` | Implémente une issue, avec ses tests unitaires | `src/main`, tests unitaires |
| `qa` | Tests d'acceptation et E2E depuis la spec | `src/test` uniquement |
| `reviewer` | Yassine : relit plans et PR contre les ADR | Non, lecture seule |

## Le flux

```
CTO (objectif) → pm (issue + critères) → backend (code + tests unitaires)
   → qa (tests E2E depuis la spec) → reviewer (verdict ADR) → CTO (merge)
```

Personne ne merge sauf le CTO. Personne ne priorise sauf le CTO et le co-CEO.

## Utilisation

Les contrats de ce dossier sont la **source unique**. Les deux outils les utilisent :

- **Codex** : `.codex/agents/*.toml` déclare les 4 agents (sandbox en lecture seule pour `pm` et `reviewer`) et leur demande de lire leur contrat ici. Le projet doit être marqué comme *trusted* dans Codex pour qu'ils soient chargés. Exemple : « Lance l'agent reviewer sur la PR #2 ». `/agent` permet de passer d'un agent à l'autre.
- **Claude Code** : les agents sont chargés automatiquement depuis `.claude/agents/`. Exemple : « Utilise l'agent qa pour écrire les tests d'acceptation de l'issue #9 ».

Pour modifier un rôle, on modifie uniquement son fichier `.md` ici, en PR.
