package ma.rafiq.application;

import ma.rafiq.domain.Intent;
import ma.rafiq.domain.LlmResult;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HandleIncomingMessageTest {
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
}
