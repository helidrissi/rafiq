# Comment on travaille sur Rafiq

La chaîne complète, des specs au merge, est décrite dans [la Software Factory](docs/factory/README.md).

## Le cycle

1. **Une spec, puis des issues.** Un besoin passe par une spec ([modèle](docs/specs/TEMPLATE.md)) avant de devenir des issues. Pas de code sans issue : contexte, périmètre, critère de « fini ».
2. **Une branche par issue**, depuis `develop` : `feat/9-escalade-hors-heures`, `fix/…`, `chore/…`.
3. **Une PR par issue**, partie de `develop` et non d'une autre PR, vers `develop`, avec `Closes #N` dans la description et la checklist ADR remplie.
4. **Review obligatoire** avant merge, contre les ADR. Personne ne merge sa propre PR sans relecture.
5. `main` ne reçoit que des versions stables, depuis `develop`.

## Commits

[Conventional Commits](https://www.conventionalcommits.org) : `feat:`, `fix:`, `test:`, `chore:`, `docs:`, `refactor:`.
Un commit = une étape qui compile, `mvn verify` vert.

## Organisation du backlog

| Outil | Rôle |
|---|---|
| **Milestones** | Un objectif daté par mois simulé (`An 1 · M3 — Go-live pilote Dr Benali`). Une issue appartient à un seul milestone. |
| **Labels `type:*`** | Nature du travail : feature, bug, security, tech-debt, observability, compliance, hiring. |
| **Labels `squad:*`** | Équipe propriétaire : Conversation, Cabinet, Plateforme, Business. |
| **Labels `P0` à `P3`** | Priorité, décidée par le CTO. `P0` = on arrête tout. |
| **Label `an-*`** | Année de la roadmap. |
| **Projet « Rafiq Roadmap »** | Vues Kanban (par statut) et Roadmap (par dates). |

## Definition of Done

- Critères de l'issue remplis, tests à l'appui.
- `mvn verify` vert, aucun secret dans le diff.
- Aucune donnée patient ni numéro en clair dans les logs.
- Si le parcours patient change : le test E2E webhook → réponse le couvre.
- Données de test 100 % fictives.
