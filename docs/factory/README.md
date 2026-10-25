# Software Factory · V0

Comment un besoin devient du code mergé, testé et relu chez Rafiq.

La factory transforme une **spec validée** en **code mergé dans `develop`**. Quatre agents font le travail ; le CTO contrôle à trois portes. Personne d'autre que le CTO ne merge.

## La chaîne

```
Besoin ─► SPEC ─► PM ─► FULLSTACK ─► QA ─► YASSINE ─► MERGE
          ▲       ▲                                   ▲
          │    Porte 1          Porte 2            Porte 3
          │  spec validée   issues et priorités   CTO seul
   CTO + co-CEO
   + Pr Tazi
```

| Poste | Qui | Reçoit | Produit | Interdits |
|---|---|---|---|---|
| **Spec** | CTO, co-CEO, Pr Tazi (terrain) | Un besoin | `docs/specs/NNNN-titre.md` selon [le modèle](../specs/TEMPLATE.md) | Parler de technique avant d'avoir écrit les parcours |
| **PM senior** | agent `pm` (Salma) | La spec validée | Un milestone et ses issues, critères *Étant donné / Quand / Alors*, en brouillon puis créés après accord | Prioriser, inventer un besoin, modifier le repo |
| **Fullstack senior** | agent `fullstack` (Mehdi : Java 21, Spring Boot, Spring AI, Next.js) | Une issue | Une branche, une PR, du code simple et lisible, ses tests unitaires | Toucher aux tests du QA, déborder de l'issue, merger |
| **QA senior** | agent `qa` (Leïla) | L'issue et la spec, jamais le code d'abord | Des scénarios joués comme un utilisateur, un rapport, une issue par bug | Modifier `src/main`, affaiblir un test |
| **Reviewer** | agent `reviewer` (Yassine) | La PR | Un verdict contre les ADR, la sécurité et le branchement réel | Modifier le code, merger |

Les contrats détaillés de chaque agent sont dans [`.claude/agents/`](../../.claude/agents/). Codex les charge via [`.codex/agents/`](../../.codex/agents/).

## Les portes

| Porte | Moment | Le CTO vérifie | Si non |
|---|---|---|---|
| **1 · Spec prête** | Avant le PM | Les parcours sont écrits du point de vue des utilisateurs ; le hors scope est explicite ; chaque critère de succès est vérifiable ; le Pr Tazi a relu les parcours. **Le merge de la PR de spec vaut validation**, à condition que la PR contienne déjà le statut « Validée » et la checklist cochée | La spec retourne en rédaction |
| **2 · Issues prêtes** | Avant le Fullstack | Chaque issue a des critères testables, un périmètre, un milestone ; le CTO fixe les priorités `P0`–`P3` | Le PM corrige ses brouillons |
| **3 · Merge** | Après Yassine | Verdict ✅ de Yassine, rapport QA sans échec bloquant, `mvn verify` vert. Merge **uniquement en « Squash and merge »** : un commit propre par issue dans `develop` | La PR retourne au Fullstack |

## Le QA « comme un utilisateur »

Le QA ne vérifie pas que le code fait ce que le développeur a écrit. Il vérifie ce que vivent la maman, le patient, la secrétaire et le médecin :

- **Conversations jouées de bout en bout** par `POST /webhooks/twilio/whatsapp`, avec le message réellement envoyé et l'état en base.
- **Personas** : la maman qui écrit à 23 h, le patient qui écrit en arabizi, celui qui change d'avis trois fois, celui qui envoie un message vocal.
- **Playwright** dès qu'une spec contient un écran.
- **Sessions exploratoires** qui cherchent à casser le parcours.
- Un rapport scénario par scénario : passé, échoué ou non couvert, et une issue `type:bug` par problème trouvé.

## Les règles

1. **Une issue = une branche = une PR, partie de `develop`.** Pas de PR empilées.
2. **Un code non appelé depuis le point d'entrée réel n'est pas fini.** Une classe testée mais jamais branchée n'est pas une fonctionnalité.
3. **Un seul contrat par rôle**, dans `.claude/agents/`, modifié uniquement par PR.
4. **Le test qui prouve un bug s'écrit avant le correctif.**
5. **Données 100 % fictives**, aucun secret dans le repo, aucune donnée patient en clair dans les logs.
6. **Les agents ne mergent jamais et ne priorisent jamais.**
7. **Un rapport produit par un agent de remplacement ne compte pas.** Si l'agent demandé n'est pas chargé, Codex s'arrête et le dit ; il n'improvise pas avec un agent générique.
8. **Ce qui est décidé à une porte est appliqué à la lettre.** Après la porte 2, on vérifie que l'issue créée porte bien les labels, la priorité et le milestone validés.
9. **Le diff ne contient que l'issue.** Avant chaque push, `git diff --stat origin/develop...HEAD` ne doit lister que des fichiers de l'issue. Pour mettre une branche à jour, on rebase sur `develop`, ou on fait un commit de merge **sans aucun changement de code** ; jamais de correctif glissé dans un commit de merge.
10. **Un test qui ne tourne pas n'existe pas.** Maven n'exécute dans `mvn verify` que les classes `*Test` (Surefire ; Failsafe n'est pas configuré). Toute classe de test se termine donc par `Test`, jamais par `IT`. Quand on ajoute des tests, on vérifie que le nombre de tests exécutés augmente.

## Changer une spec en cours de route

La spec est la source de vérité : les issues en découlent, le code découle des issues. Une spec modifiée repasse donc par toute la chaîne, sans raccourci :

```
Spec révisée ─► Porte 1 (merge) ─► PM met à jour l'issue ─► Porte 2 ─► Fullstack ─► QA ─► Yassine ─► Porte 3
```

- On modifie **le même fichier** de spec, on incrémente sa révision et on complète sa section **Historique** (date, changement, origine).
- Si la PR de code n'est pas encore mergée et que le changement est dans le périmètre de l'issue, le PM **met à jour l'issue existante** au lieu d'en créer une nouvelle, et le Fullstack corrige dans la même PR.

## Les métriques

Relevées à chaque passage, dans la description de la PR ou le rapport de fin de milestone.

| Métrique | Ce qu'elle dit | Cible V0 |
|---|---|---|
| **Délai spec → merge** | La vitesse de l'usine | Mesurer d'abord, pas de cible |
| **Bugs trouvés par le QA avant merge / bugs trouvés après** | La qualité du filet | Plus de 80 % avant merge |
| **Allers-retours avec Yassine par PR** | La qualité du code en entrée de review | 1 au plus en moyenne |
| **Issues terminées sans question au PM** | La qualité des issues | Plus de 80 % |

## Comment lancer un passage

0. **Étape zéro** : `git pull` sur `develop`, puis demander à Codex « Liste les agents personnalisés chargés, sans en lancer ». Les quatre agents (`pm`, `fullstack`, `qa`, `reviewer`) doivent apparaître. Sinon, on corrige avant d'aller plus loin.
1. Rédiger la spec à partir de [`docs/specs/TEMPLATE.md`](../specs/TEMPLATE.md), la relire avec le Pr Tazi, la valider (porte 1).
2. Codex : « Lance l'agent pm sur `docs/specs/NNNN-titre.md` et montre-moi les brouillons d'issues. » Valider et prioriser (porte 2).
3. Pour chaque issue, dans l'ordre des priorités :
   - « Lance l'agent fullstack sur l'issue #N. »
   - « Lance l'agent qa sur l'issue #N et la PR associée. »
   - « Lance l'agent reviewer sur la PR. »
4. Merger en **Squash and merge** (porte 3), puis relever les métriques.

Le premier passage porte sur **une seule petite spec**, pour roder l'usine avant de lui confier un milestone entier.

## Évolutions prévues

- **V1** : CI GitHub Actions qui lance `mvn verify` et les tests QA sur chaque PR ; protection de `develop` (CI verte obligatoire, squash seulement) ; métriques collectées automatiquement.
- **V2** : plusieurs Fullstack en parallèle sur des issues indépendantes (worktrees), un agent sécurité et conformité.
