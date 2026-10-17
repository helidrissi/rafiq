# CLAUDE.md — Rafiq (MVP, Mois 1)

## Contexte
**Rafiq** (« compagnon » en arabe) : le compagnon du cabinet médical.
Repo : `rafiq`. Package Java racine : `ma.rafiq`.
Assistant WhatsApp IA pour cabinets médicaux marocains : prise de RDV en français/darija/arabizi,
rappels, escalade vers la secrétaire. Projet de simulation : **données 100 % fictives**.

## Stack
- Java 21, Spring Boot 3, Maven
- PostgreSQL (Docker en local), migrations Flyway
- Tests : JUnit 5 (Testcontainers reporté au weekend 2 ; d'ici là, tests d'intégration
  sur le Postgres du docker-compose)
- Twilio WhatsApp Sandbox (webhook exposé via ngrok)
- LLM : OpenAI via une interface `LlmClient` (Spring AI) — fournisseur interchangeable
- Exécution : local sur Mac. Déploiement futur : Railway

## Scope MVP
Weekend 1 :
1. Patient écrit sur WhatsApp → le LLM classifie l'intention et extrait les entités
2. Proposer 3 créneaux libres → choix → confirmation explicite → RDV enregistré
3. Escalade vers la secrétaire (faible confiance, question médicale, message invalide)
   Weekend 2 (reportés explicitement) : rappel automatique la veille du RDV,
   validation de la signature Twilio, Testcontainers.
   OUT : annulation/report complexe, multi-médecins, dashboard, paiement, auth avancée

## Ordre de travail
Commencer par un **walking skeleton** : WhatsApp → webhook → réponse « écho » de bout en bout,
avant toute logique métier. Ensuite épaissir couche par couche.

## Architecture (hexagonale simple)
- `domain/` : Patient, Appointment, Slot, Conversation, ConversationState, Escalation
  (aucune dépendance framework)
- `application/` : 4 cas d'usage — HandleIncomingMessage, FindAvailableSlots,
  BookAppointment, EscalateConversation. Ports : `LlmClient`, `MessageSender`, repositories.
  La détection d'intention est un appel au port `LlmClient`, pas un cas d'usage.
- `adapters/in/` : TwilioWebhookController (`POST /webhooks/twilio/whatsapp`)
- `adapters/out/` : TwilioMessageSender, OpenAiLlmClient, repositories JPA
- `configuration/` : beans, propriétés

## Machine à états de la conversation
L'état est stocké en base pour chaque conversation. Chaque message est interprété selon l'état courant.
```
IDLE → SLOTS_PROPOSED → AWAITING_CONFIRMATION → BOOKED
  (depuis n'importe quel état) → ESCALATED
```
- Transitions explicites dans le domaine, une transition invalide = message de reformulation.
- Un test unitaire par transition.

## Rôle du LLM
Le LLM **comprend**, le code **décide et répond**.
- Le LLM classifie et extrait uniquement. Il ne rédige jamais le texte envoyé au patient.
- Format JSON strict :
  `{"intent": "BOOK_APPOINTMENT|CHOOSE_SLOT|CONFIRM|DENY|MEDICAL_QUESTION|OTHER",
    "confidence": 0.0-1.0,
    "entities": {"preferred_date": "...", "time_of_day": "MORNING|AFTERNOON|EVENING", "slot_choice": 1}}`
- JSON invalide ou non conforme → escalade.
- Toutes les réponses au patient viennent de **templates validés** dans le code.

## Règles non négociables
- Jamais de données réelles de patients. Seed de données fictives uniquement.
- Aucune donnée de santé ni numéro de téléphone en clair dans les logs (masquer).
- Secrets (Twilio, OpenAI) uniquement en variables d'environnement, jamais commités.
- L'IA ne donne **aucun avis médical**.
- **Escalade médicale en double sécurité** : escalade si le LLM renvoie `MEDICAL_QUESTION`
  **OU** si une liste de mots-clés déterministe matche (français, darija, arabizi :
  fièvre, s5ana, skhana, douleur, saigne, urgence…). Les faux positifs sont acceptés.
- Escalade si confiance < 0.7.
- Réservation uniquement après confirmation explicite. Toujours proposer 3 créneaux.

## Robustesse et sécurité
- **Anti-double réservation** : contrainte unique en base sur le créneau + verrou optimiste (`@Version`).
- **Signature Twilio** : valider `X-Twilio-Signature` — **weekend 2**. D'ici là, webhook exposé
  uniquement via ngrok, URL non partagée.
- **Idempotence** : dédoublonner les messages sur `MessageSid` (Twilio peut renvoyer un message).
- **Temps** : stocker en UTC, afficher en `Africa/Casablanca` (attention au changement d'heure du Ramadan).

## Jeu de test darija
`src/test/resources/fixtures/messages-darija.json` : 20 messages fictifs réalistes
(fautes, arabizi type « bghit rdv ghda », mélange français/darija) avec l'intention attendue.
Métrique : taux de bonne classification.

## Definition of Done (Weekend 1)
- Webhook reçoit un message (idempotent sur `MessageSid`)
- Intention détectée via `LlmClient`, 3 créneaux proposés, RDV confirmé et persisté
- Demandes médicales systématiquement escaladées (double sécurité testée)
- Double réservation impossible (testé)
- Aucun numéro ni secret dans les logs
- Tests unitaires (domaine, transitions d'état) + intégration (Postgres docker-compose, Twilio mocké)
- `mvn verify` passe, README et docker-compose présents

## Décisions (ADR)
- **ADR-001 Stack Java/Spring Boot/Postgres** — maîtrisée par le CTO, robuste, vitesse d'exécution au MVP.
- **ADR-002 Twilio Sandbox** — démarrage immédiat sans vérification Meta. Limite : pas utilisable
  en production, templates hors fenêtre 24 h à revoir. À réévaluer au Mois 3 (Meta Cloud API direct ?).
- **ADR-003 OpenAI derrière `LlmClient`** — qualité correcte en français/darija, fournisseur remplaçable.
- **ADR-004 Exécution locale** — données fictives, donc pas d'enjeu CNDP au MVP. **Dette identifiée** :
  hébergement conforme (loi 09-08, autorisation CNDP) obligatoire avant tout vrai patient.
- **ADR-005 Escalade par seuil de confiance** — réponse produit à la peur des erreurs en darija.
- **ADR-006 Machine à états explicite** — conversation multi-tours prévisible et testable.
- **ADR-007 LLM = classification/extraction, réponses par templates** — aucun texte libre généré
  envoyé à un patient.
- **ADR-008 Escalade médicale en double sécurité** — LLM + mots-clés déterministes (défense en profondeur).
- **ADR-009 Rappels reportés au weekend 2** — priorité au parcours de réservation.
- **ADR-010 Signature Twilio et Testcontainers reportés au weekend 2** — protection du scope.
  Risque accepté : webhook non authentifié en local (données fictives, URL ngrok non partagée).
  Non négociables du weekend 1 : machine à états et double sécurité médicale.

## Budget MVP
~20 €/mois (OpenAI + Twilio). ngrok gratuit.