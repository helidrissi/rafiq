## Pourquoi

<!-- Le problème ou l'objectif, en 2 lignes. -->

Closes #

## Quoi

<!-- Ce qui change, en quelques puces. Une PR = un sujet. -->

## Checklist ADR (à cocher avant review)

- [ ] **ADR-006** : la conversation passe par la machine à états, aucune transition implicite
- [ ] **ADR-007** : aucun texte libre généré par le LLM n'est envoyé au patient (templates uniquement)
- [ ] **ADR-008** : les mots-clés médicaux sont évalués **avant** le LLM sur ce chemin
- [ ] Aucune donnée patient ni numéro en clair dans les logs, les erreurs ou Sentry
- [ ] Aucun secret dans le diff (variables d'environnement uniquement)
- [ ] Dates stockées en UTC, affichées en `Africa/Casablanca`

## Preuve

- [ ] `mvn verify` vert
- [ ] Test qui prouve le comportement (et qui échouait avant, pour un bug)
- [ ] Si le parcours patient change : le test E2E webhook → réponse le couvre

## Pour le reviewer

<!-- Ce qu'il faut regarder en priorité, les choix discutables, ce qui est hors scope. -->
