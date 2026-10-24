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

- **Claude Code** : les agents sont chargés automatiquement depuis `.claude/agents/`. Exemple : « Utilise l'agent qa pour écrire les tests d'acceptation de l'issue #9 ».
- **Codex** : référencer ces fichiers depuis `AGENTS.md` et demander explicitement le rôle, par exemple « Agis selon `.claude/agents/reviewer.md` et relis la PR #2 ».
