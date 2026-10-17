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
}
