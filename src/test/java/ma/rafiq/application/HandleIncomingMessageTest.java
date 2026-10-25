package ma.rafiq.application;

import ma.rafiq.domain.Intent;
import ma.rafiq.domain.LlmResult;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HandleIncomingMessageTest {
    @Test void escalatesBlankMessageWithoutCallingLlm() {
        var client = new ma.rafiq.testing.FakeLlmClient(
                new LlmResult(Intent.OTHER, 0.99, List.of(), "ignored"));
        var sent = new String[1];
        var handler = new HandleIncomingMessage(client, (recipient, message) -> sent[0] = message);

        handler.handle("patient-blank", "   ");

        assertEquals(ResponseTemplates.MESSAGE_SANS_TEXTE, sent[0]);
        assertEquals(0, client.callCount());
        assertEquals(ma.rafiq.domain.ConversationState.ESCALATED,
                handler.conversationState("patient-blank"));
    }

    @Test void escalatesMedicalQuestions() {
        var sent = new String[1];
        var handler = new HandleIncomingMessage(
                ignored -> new LlmResult(Intent.MEDICAL_QUESTION, 0.99, List.of(), "avis"),
                (recipient, message) -> sent[0] = message);
        handler.handle("masked-recipient", "j'ai mal");
        assertEquals("Je transmets votre demande à la secrétaire.", sent[0]);
    }

    @Test void escalatesByKeywordBeforeCallingLlm() {
        var client = new ma.rafiq.testing.FakeLlmClient(
                new LlmResult(Intent.BOOK_APPOINTMENT, 0.99, List.of(), "réponse"));
        var sent = new String[1];
        var handler = new HandleIncomingMessage(client, (recipient, message) -> sent[0] = message);

        handler.handle("masked-recipient", "j'ai de la fièvre");

        assertEquals("Je transmets votre demande à la secrétaire.", sent[0]);
        assertEquals(0, client.callCount());
    }

    @Test void sendsCodeTemplateInsteadOfLlmReply() {
        var handler = new HandleIncomingMessage(
                ignored -> new LlmResult(Intent.BOOK_APPOINTMENT, 0.99, List.of(), "texte libre du LLM"),
                (recipient, message) -> assertEquals("Je vais vous proposer trois créneaux disponibles.", message));
        handler.handle("masked-recipient", "je veux un rendez-vous");
    }

    @Test void drivesConversationThroughBookingStates() {
        var results = new java.util.ArrayDeque<>(List.of(
                new LlmResult(Intent.BOOK_APPOINTMENT, 0.99, List.of(), "ignored"),
                new LlmResult(Intent.CHOOSE_SLOT, 0.99, List.of("1"), "ignored"),
                new LlmResult(Intent.CONFIRM, 0.99, List.of(), "ignored")));
        var replies = new java.util.ArrayList<String>();
        var handler = new HandleIncomingMessage(ignored -> results.remove(),
                (recipient, message) -> replies.add(message));

        handler.handle("patient-1", "rdv");
        handler.handle("patient-1", "1");
        handler.handle("patient-1", "oui");

        assertEquals(List.of(
                "Je vais vous proposer trois créneaux disponibles.",
                "Merci. Souhaitez-vous confirmer ce créneau ?",
                "Votre rendez-vous est confirmé."), replies);
    }
}
