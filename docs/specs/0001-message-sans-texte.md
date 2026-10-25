# Spec 0001 · Répondre aux messages sans texte

| | |
|---|---|
| **Statut** | En relecture |
| **Auteurs** | CTO, co-CEO |
| **Relecture terrain** | Pr Tazi, le 25/10/2026 |
| **Milestone visé** | À proposer par le PM |
| **ADR concernés** | ADR-006, ADR-007, ADR-008 |

> Spec volontairement petite : c'est le premier passage de la Software Factory. Elle sert autant à livrer cette fonctionnalité qu'à mesurer la qualité de l'usine.

## 1. Le problème

Les patients marocains envoient beaucoup de **messages vocaux** sur WhatsApp, surtout le soir, et parfois une **photo** (ordonnance, résultat d'analyse) sans aucun texte. Aujourd'hui, Rafiq traite ces messages comme un texte vide : la réponse est imprévisible, et une maman qui envoie un vocal inquiet à 23 h peut ne recevoir aucune réponse utile.

Fait terrain (Pr Tazi) : « Les mamans envoient des vocaux en darija, surtout la nuit. Si on ne répond pas, elles rappellent le lendemain en colère, ou elles ne rappellent pas du tout. »

## 2. Les utilisateurs

| Utilisateur | Ce qu'il veut | Ce qu'il craint |
|---|---|---|
| Patient / maman | Être entendu tout de suite, savoir quoi faire | Que son message se perde, surtout si c'est urgent |
| Secrétaire | Retrouver les demandes qu'elle doit traiter | Rater un patient qui a écrit pendant la nuit |
| Médecin | Qu'aucune urgence ne passe inaperçue | Que l'assistant donne un avis médical |

## 3. Les parcours

### Parcours A · Message vocal sans texte

- **Persona** : une maman, 23 h, envoie un message vocal de 40 secondes en darija, sans texte.
- **Déroulé** :
  1. Elle envoie le vocal au numéro WhatsApp du cabinet.
  2. Rafiq répond **immédiatement** avec le message « Message sans texte » (section 4).
  3. La demande est transmise à la secrétaire (la conversation passe à l'état escaladé).
- **Résultat attendu** : la maman reçoit une seule réponse, claire, qui lui dit quoi faire en cas d'urgence et comment être servie. La conversation de ce numéro est à l'état `ESCALATED`.

### Parcours B · Photo sans texte

- **Persona** : un patient envoie la photo de son ordonnance, sans texte.
- **Déroulé et résultat** : identiques au parcours A.

### Parcours C · Photo ou vocal accompagné d'un texte

- **Persona** : un patient envoie une photo avec la légende « bghit rdv ghda ».
- **Déroulé** : Rafiq ignore le fichier et traite **le texte** exactement comme un message texte normal (mots-clés médicaux d'abord, puis le LLM).
- **Résultat attendu** : le patient reçoit la réponse habituelle à une demande de rendez-vous.

### Cas limites

- **Média + texte médical** (« s5ana » en légende d'un vocal) : l'escalade médicale habituelle s'applique (ADR-008), pas le message « Message sans texte ».
- **Message vide**, sans texte et sans fichier : même traitement que le parcours A.
- **Doublon** du même vocal (même `MessageSid`) : une seule réponse, jamais deux.
- **Texte composé uniquement d'espaces** : traité comme un message sans texte.

## 4. Les règles non négociables

- **ADR-007** : la réponse est un template validé. Texte validé par le Pr Tazi :

  > Nous ne pouvons pas écouter les messages vocaux ni lire les images. Si c'est urgent, appelez le 141 (SAMU). Sinon, écrivez votre demande en quelques mots, la secrétaire vous répondra.
  > Ma kan9drouch nsem3o l-vocal wla nchoufo tswira. Ila kan chi haja mosta3jla, 3iyto l 141. Wla ktbo lina chno bghitou, w l-secrétaire ghadi tjawbkom.

- **ADR-008** : s'il y a un texte, les mots-clés médicaux restent évalués en premier.
- **ADR-006** : l'escalade passe par la machine à états (`Conversation.escalate()`).
- **Le LLM n'est pas appelé** pour un message sans texte : il n'y a rien à classifier.
- Aucun contenu de message, aucune URL de fichier et aucun numéro complet dans les logs. Le fichier n'est ni téléchargé ni stocké.
- Même réponse de jour comme de nuit.

## 5. Hors scope

- Transcrire ou comprendre les vocaux, lire les images : plus tard, avec la stratégie IA darija.
- Enregistrer l'escalade en base et la boîte des escalades de la secrétaire : milestone « Parcours fiable » et specs suivantes.
- Réponse différente selon les horaires du cabinet.
- Validation de la signature Twilio (issue #8).

## 6. Notes techniques

- Point d'entrée : `POST /webhooks/twilio/whatsapp` (`TwilioWebhookController`). Aujourd'hui il ne lit que `MessageSid`, `From` et `Body`.
- Twilio envoie aussi `NumMedia` (nombre de fichiers) et `MediaContentType0`, `MediaUrl0`… Pour un vocal ou une photo sans légende, `Body` est vide.
- Le traitement passe par `HandleIncomingMessage.handle()`. L'état des conversations y est aujourd'hui gardé en mémoire.
- Le dédoublonnage `MessageSid` existant doit continuer à s'appliquer.

## 7. Critères de succès

- [ ] Parcours A rejoué par le webhook (`Body` vide, `NumMedia=1`, `MediaContentType0=audio/ogg`) : exactement un message envoyé, égal au template « Message sans texte », et conversation à l'état `ESCALATED`.
- [ ] Parcours B (`MediaContentType0=image/jpeg`) : même résultat.
- [ ] Parcours C (photo + « bghit rdv ghda ») : réponse habituelle de demande de rendez-vous.
- [ ] Média + « s5ana » : template d'escalade médicale, pas le template « Message sans texte ».
- [ ] Message vide et message d'espaces : même résultat que le parcours A.
- [ ] Même `MessageSid` envoyé deux fois : une seule réponse.
- [ ] Le LLM n'est jamais appelé pour un message sans texte (vérifié avec `FakeLlmClient`).
- [ ] Aucun log ne contient le contenu, l'URL du média ou le numéro complet.
- [ ] `mvn verify` vert, verdict ✅ de Yassine.

## 8. Questions ouvertes

| Question | Qui tranche | Réponse |
|---|---|---|
| Le message bilingue est-il trop long pour WhatsApp ? | Pr Tazi | Non : deux phrases courtes, les patients lisent les deux langues |
| Faut-il répondre une seule fois par nuit à un patient qui envoie dix vocaux ? | CTO | Hors scope V0 : une réponse par message, à revoir avec la boîte des escalades |

## Porte 1 · Checklist de validation

- [x] Les parcours sont écrits du point de vue des utilisateurs.
- [x] Le Pr Tazi a relu les parcours.
- [x] Le hors scope est explicite.
- [x] Chaque critère de succès est vérifiable.
- [ ] Les questions ouvertes bloquantes sont tranchées. *(validation du CTO)*
