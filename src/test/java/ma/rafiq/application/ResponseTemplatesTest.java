package ma.rafiq.application;

import ma.rafiq.domain.Intent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResponseTemplatesTest {
    @Test void providesValidatedReplyForAppointmentIntent() {
        assertEquals("Je vais vous proposer trois créneaux disponibles.",
                ResponseTemplates.forIntent(Intent.BOOK_APPOINTMENT));
    }

    @Test void neverUsesMedicalLlmTextAsPatientReply() {
        assertEquals(ResponseTemplates.ESCALATION,
                ResponseTemplates.forIntent(Intent.MEDICAL_QUESTION));
    }
}
