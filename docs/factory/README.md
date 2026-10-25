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
| **PM senior** | agent `pm` | La spec validée | Un milestone et ses issues, critères *Étant donné / Quand / Alors*, en brouillon puis créés après accord | Prioriser, inventer un besoin, modifier le repo |
| **Fullstack senior** | agent `backend` (Java 21, Spring Boot, Spring AI, Next.js) | Une issue | Une branche, une PR, du code simple et lisible, ses tests unitaires | Toucher aux tests du QA, déborder de l'issue, merger |
| **QA senior** | agent `qa` | L'issue et la spec, jamais le code d'abord | Des scénarios joués comme un utilisateur, un rapport, une issue par bug | Modifier `src/main`, affaiblir un test |
| **Reviewer** | agent `reviewer` (Yassine) | La PR | Un verdict contre les ADR, la sécurité et le branchement réel | Modifier le code, merger |

Les contrats détaillés de chaque agent sont dans [`.claude/agents/`](../../.claude/agents/). Codex les charge via [`.codex/agents/`](../../.codex/agents/).

## Les portes

| Porte | Moment | Le CTO vérifie | Si non |
|---|---|---|---|
| **1 · Spec prête** | Avant le PM | Les parcours sont écrits du point de vue des utilisateurs ; le hors scope est explicite ; chaque critère de succès est vérifiable ; le Pr Tazi a relu les parcours | La spec retourne en rédaction |
| **2 · Issues prêtes** | Avant le Fullstack | Chaque issue a des critères testables, un périmètre, un milestone ; le CTO fixe les priorités `P0`–`P3` | Le PM corrige ses brouillons |
| **3 · Merge** | Après Yassine | Verdict ✅ de Yassine, rapport QA sans échec bloquant, `mvn verify` vert | La PR retourne au Fullstack |

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

## Les métriques

Relevées à chaque passage, dans la description de la PR ou le rapport de fin de milestone.

| Métrique | Ce qu'elle dit | Cible V0 |
|---|---|---|
| **Délai spec → merge** | La vitesse de l'usine | Mesurer d'abord, pas de cible |
| **Bugs trouvés par le QA avant merge / bugs trouvés après** | La qualité du filet | Plus de 80 % avant merge |
| **Allers-retours avec Yassine par PR** | La qualité du code en entrée de review | 1 au plus en moyenne |
| **Issues terminées sans question au PM** | La qualité des issues | Plus de 80 % |

## Comment lancer un passage

1. Rédiger la spec à partir de [`docs/specs/TEMPLATE.md`](../specs/TEMPLATE.md), la relire avec le Pr Tazi, la valider (porte 1).
2. Codex : « Lance l'agent pm sur `docs/specs/NNNN-titre.md` et montre-moi les brouillons d'issues. » Valider et prioriser (porte 2).
3. Pour chaque issue, dans l'ordre des priorités :
   - « Lance l'agent backend sur l'issue #N. »
   - « Lance l'agent qa sur l'issue #N et la PR associée. »
   - « Lance l'agent reviewer sur la PR. »
4. Merger (porte 3), puis relever les métriques.

Le premier passage porte sur **une seule petite spec**, pour roder l'usine avant de lui confier un milestone entier.

## Évolutions prévues

- **V1** : CI GitHub Actions qui lance `mvn verify` et les tests QA sur chaque PR ; métriques collectées automatiquement.
- **V2** : plusieurs Fullstack en parallèle sur des issues indépendantes (worktrees), un agent sécurité et conformité.
