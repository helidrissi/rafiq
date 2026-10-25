# Équipe Engineering (agents)

Les quatre postes de la [Software Factory](../../docs/factory/README.md) après la spec.

| Agent | Persona | Rôle | Écrit dans le repo ? |
|---|---|---|---|
| `pm` | **Salma**, PM senior healthtech, ex-Doctolib | Spec validée → milestone + issues claires et testables | Non (crée les issues après accord du CTO) |
| `fullstack` | **Mehdi**, fullstack senior Java/Spring/Next.js, 20 ans en scale-up | Implémente une issue, code simple et lisible, tests | `src/main`, tests unitaires et d'intégration |
| `qa` | **Leïla**, QA senior, 20 ans dont Doctolib, connaît les cabinets marocains | Teste comme un utilisateur final, depuis la spec | `src/test` uniquement |
| `reviewer` | **Yassine**, Staff Engineer | Relit chaque PR contre les ADR et le branchement réel | Non, lecture seule |

## Le flux

```
Spec ─► Porte 1 ─► pm ─► Porte 2 ─► fullstack ─► qa ─► reviewer ─► Porte 3 (merge)
```

Personne ne merge sauf le CTO. Personne ne priorise sauf le CTO et le co-CEO.

## Utilisation

Les contrats de ce dossier sont la **source unique**. Les deux outils les utilisent :

- **Codex** : `.codex/agents/*.toml` déclare les 4 agents (sandbox en lecture seule pour `pm` et `reviewer`) et leur demande de lire leur contrat ici. Le projet doit être marqué comme *trusted* dans Codex. Exemples : « Lance l'agent pm sur `docs/specs/0001-….md` », « Lance l'agent reviewer sur la PR #18 ». `/agent` permet de passer d'un agent à l'autre.
- **Claude Code** : les agents sont chargés automatiquement depuis `.claude/agents/`.

Pour modifier un rôle, on modifie uniquement son fichier `.md` ici, en PR.
