package ma.rafiq.adapters.in;

import ma.rafiq.application.HandleIncomingMessage;
import ma.rafiq.application.MessageSender;
import ma.rafiq.application.LlmClient;
import ma.rafiq.domain.Conversation;
import ma.rafiq.domain.ConversationState;
import ma.rafiq.domain.Intent;
import ma.rafiq.domain.LlmResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class TwilioWebhookControllerTest {
    @Autowired MockMvc mvc;
    @Autowired HandleIncomingMessage handler;
    @MockBean MessageSender messageSender;
    @MockBean LlmClient llmClient;

    @BeforeEach
    void configureLlm() {
        org.mockito.Mockito.when(llmClient.classify(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(new LlmResult(Intent.OTHER, 0.9, java.util.List.of(), "ignored"));
    }

    @Test
    void receivesTwilioMessageAndEchoesThroughHandler() throws Exception {
        mvc.perform(post("/webhooks/twilio/whatsapp")
                        .param("MessageSid", "SM-123")
                        .param("From", "whatsapp:+212612345678")
                        .param("Body", "Bonjour"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/xml"));

        verify(messageSender).send("whatsapp:+212612345678", "Pouvez-vous préciser votre demande ?");
    }

    @Test
    void ignoresDuplicateMessageSid() throws Exception {
        var request = post("/webhooks/twilio/whatsapp")
                .param("MessageSid", "SM-DUP")
                .param("From", "whatsapp:+212612345678")
                .param("Body", "Bonjour");

        mvc.perform(request).andExpect(status().isOk());
        mvc.perform(request).andExpect(status().isOk());

        verify(messageSender, times(1)).send("whatsapp:+212612345678", "Pouvez-vous préciser votre demande ?");
    }

    @Test
    void receivesAudioWithoutBodyAndSendsValidatedNoTextTemplate() throws Exception {
        mvc.perform(post("/webhooks/twilio/whatsapp")
                        .param("MessageSid", "SM-AUDIO-NO-TEXT")
                        .param("From", "whatsapp:+212612345678")
                        .param("NumMedia", "1")
                        .param("MediaContentType0", "audio/ogg")
                        .param("MediaUrl0", "https://api.twilio.com/secret-media-url"))
                .andExpect(status().isOk());

        verify(messageSender).send("whatsapp:+212612345678",
                ma.rafiq.application.ResponseTemplates.MESSAGE_SANS_TEXTE);
        verify(llmClient, never()).classify(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void receivesImageWithoutBodyAndSendsValidatedNoTextTemplate() throws Exception {
        var from = "whatsapp:+212600000025";

        sendWebhookWithMedia("SM-IMAGE-NO-TEXT", from, null, "image/jpeg",
                "https://api.twilio.com/private-image-url");

        verify(messageSender).send(from, ma.rafiq.application.ResponseTemplates.MESSAGE_SANS_TEXTE);
        verify(llmClient, never()).classify(anyString());
        assertEquals(ConversationState.ESCALATED, stateOf(from));
    }

    @Test
    void resumesBookingAfterAudioWithoutTextEscalation() throws Exception {
        var from = "whatsapp:+212600000023";

        mvc.perform(post("/webhooks/twilio/whatsapp")
                        .param("MessageSid", "SM-AUDIO-THEN-BOOKING")
                        .param("From", from)
                        .param("NumMedia", "1")
                        .param("MediaContentType0", "audio/ogg"))
                .andExpect(status().isOk());

        org.mockito.Mockito.when(llmClient.classify("bghit rdv ghda"))
                .thenReturn(new LlmResult(Intent.BOOK_APPOINTMENT, 0.99, java.util.List.of(), "ignored"));

        mvc.perform(post("/webhooks/twilio/whatsapp")
                        .param("MessageSid", "SM-TEXT-AFTER-AUDIO")
                        .param("From", from)
                        .param("Body", "bghit rdv ghda"))
                .andExpect(status().isOk());

        verify(messageSender).send(from, ma.rafiq.application.ResponseTemplates.MESSAGE_SANS_TEXTE);
        verify(messageSender).send(from,
                "Je vais vous proposer trois créneaux disponibles.");
    }

    @Test
    void receivesImageWithMedicalCaptionAndEscalatesMedicalMessageFirst() throws Exception {
        mvc.perform(post("/webhooks/twilio/whatsapp")
                        .param("MessageSid", "SM-IMAGE-MEDICAL")
                        .param("From", "whatsapp:+212612345678")
                        .param("Body", "s5ana")
                        .param("NumMedia", "1")
                        .param("MediaContentType0", "image/jpeg")
                        .param("MediaUrl0", "https://api.twilio.com/secret-media-url"))
                .andExpect(status().isOk());

        verify(messageSender).send("whatsapp:+212612345678",
                ma.rafiq.application.ResponseTemplates.ESCALATION);
        verify(llmClient, never()).classify(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void receivesImageWithBookingCaptionAndTreatsCaptionAsNormalText() throws Exception {
        var from = "whatsapp:+212600000024";
        org.mockito.Mockito.when(llmClient.classify("bghit rdv ghda"))
                .thenReturn(new LlmResult(Intent.BOOK_APPOINTMENT, 0.99, java.util.List.of(), "ignored"));

        mvc.perform(post("/webhooks/twilio/whatsapp")
                        .param("MessageSid", "SM-IMAGE-BOOKING")
                        .param("From", from)
                        .param("Body", "bghit rdv ghda")
                        .param("NumMedia", "1")
                        .param("MediaContentType0", "image/jpeg")
                        .param("MediaUrl0", "https://api.twilio.com/secret-media-url"))
                .andExpect(status().isOk());

        verify(messageSender).send(from,
                "Je vais vous proposer trois créneaux disponibles.");
        verify(llmClient).classify("bghit rdv ghda");
    }

    @Test
    void playsCompleteJourneyAfterAudioEscalationOneUserMessagePerReply() throws Exception {
        var from = "whatsapp:+212600000026";
        when(llmClient.classify("bghit rdv ghda")).thenReturn(result(Intent.BOOK_APPOINTMENT));
        when(llmClient.classify("2")).thenReturn(result(Intent.CHOOSE_SLOT));
        when(llmClient.classify("oui")).thenReturn(result(Intent.CONFIRM));

        sendWebhookWithMedia("SM-D-AUDIO", from, null, "audio/ogg", "https://twilio.test/audio");
        assertEquals(ConversationState.ESCALATED, stateOf(from));

        sendWebhook("SM-D-BOOK", from, "bghit rdv ghda");
        assertEquals(ConversationState.SLOTS_PROPOSED, stateOf(from));

        sendWebhook("SM-D-CHOICE", from, "2");
        assertEquals(ConversationState.AWAITING_CONFIRMATION, stateOf(from));

        sendWebhook("SM-D-CONFIRM", from, "oui");
        assertEquals(ConversationState.BOOKED, stateOf(from));

        var replies = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(messageSender, times(4)).send(eq(from), replies.capture());
        assertEquals(List.of(
                ma.rafiq.application.ResponseTemplates.MESSAGE_SANS_TEXTE,
                "Je vais vous proposer trois créneaux disponibles.",
                "Merci. Souhaitez-vous confirmer ce créneau ?",
                "Votre rendez-vous est confirmé."), replies.getAllValues());
        verify(llmClient).classify("bghit rdv ghda");
        verify(llmClient).classify("2");
        verify(llmClient).classify("oui");
    }

    @Test
    void playsCompleteFrancophoneBookingJourneyThroughWebhook() throws Exception {
        var from = "whatsapp:+212600000035";
        when(llmClient.classify("Bonjour, je voudrais un rendez-vous demain"))
                .thenReturn(result(Intent.BOOK_APPOINTMENT));
        when(llmClient.classify("1")).thenReturn(result(Intent.CHOOSE_SLOT));
        when(llmClient.classify("oui")).thenReturn(result(Intent.CONFIRM));

        sendWebhook("SM-FR-BOOK", from, "Bonjour, je voudrais un rendez-vous demain");
        sendWebhook("SM-FR-CHOICE", from, "1");
        sendWebhook("SM-FR-CONFIRM", from, "oui");

        var replies = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(messageSender, times(3)).send(eq(from), replies.capture());
        assertEquals(List.of(
                "Je vais vous proposer trois créneaux disponibles.",
                "Merci. Souhaitez-vous confirmer ce créneau ?",
                "Votre rendez-vous est confirmé."), replies.getAllValues());
        assertEquals(ConversationState.BOOKED, stateOf(from));
    }

    @Test
    void resumesBookingAfterMedicalDarijaEscalation() throws Exception {
        var from = "whatsapp:+212600000027";
        when(llmClient.classify("bghit rdv ghda")).thenReturn(result(Intent.BOOK_APPOINTMENT));

        sendWebhook("SM-MEDICAL", from, "s5ana");
        assertEquals(ConversationState.ESCALATED, stateOf(from));

        sendWebhook("SM-MEDICAL-BOOK", from, "bghit rdv ghda");
        assertEquals(ConversationState.SLOTS_PROPOSED, stateOf(from));

        var replies = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(messageSender, times(2)).send(eq(from), replies.capture());
        assertEquals(List.of(
                ma.rafiq.application.ResponseTemplates.ESCALATION,
                "Je vais vous proposer trois créneaux disponibles."), replies.getAllValues());
        verify(llmClient).classify("bghit rdv ghda");
        verify(llmClient, never()).classify("s5ana");
    }

    @Test
    void treatsEmptyAndWhitespaceBodyAsNoTextWithoutCallingLlm() throws Exception {
        var emptyFrom = "whatsapp:+212600000028";
        var spacesFrom = "whatsapp:+212600000029";

        sendWebhook("SM-EMPTY", emptyFrom, null);
        sendWebhook("SM-SPACES", spacesFrom, "   ");

        verify(messageSender).send(emptyFrom, ma.rafiq.application.ResponseTemplates.MESSAGE_SANS_TEXTE);
        verify(messageSender).send(spacesFrom, ma.rafiq.application.ResponseTemplates.MESSAGE_SANS_TEXTE);
        verify(llmClient, never()).classify(anyString());
        assertEquals(ConversationState.ESCALATED, stateOf(emptyFrom));
        assertEquals(ConversationState.ESCALATED, stateOf(spacesFrom));
    }

    @Test
    void deduplicatesTheSameSidForMediaWithoutText() throws Exception {
        var from = "whatsapp:+212600000030";
        var request = post("/webhooks/twilio/whatsapp")
                .param("MessageSid", "SM-DUP-MEDIA")
                .param("From", from)
                .param("NumMedia", "1")
                .param("MediaContentType0", "audio/ogg")
                .param("MediaUrl0", "https://twilio.test/duplicate-audio");

        mvc.perform(request).andExpect(status().isOk());
        mvc.perform(request).andExpect(status().isOk());

        verify(messageSender, times(1)).send(from, ma.rafiq.application.ResponseTemplates.MESSAGE_SANS_TEXTE);
        verify(llmClient, never()).classify(anyString());
    }

    @Test
    void keepsMedicalCaptionOnMedicalEscalationPath() throws Exception {
        var from = "whatsapp:+212600000031";

        sendWebhookWithMedia("SM-MEDICAL-CAPTION", from, "s5ana", "audio/ogg",
                "https://twilio.test/medical-audio");

        verify(messageSender).send(from, ma.rafiq.application.ResponseTemplates.ESCALATION);
        verify(messageSender, never()).send(from, ma.rafiq.application.ResponseTemplates.MESSAGE_SANS_TEXTE);
        verify(llmClient, never()).classify(anyString());
        assertEquals(ConversationState.ESCALATED, stateOf(from));
    }

    @Test
    void invalidChoiceInIdleGetsReformulationAndLeavesStateIdle() throws Exception {
        var from = "whatsapp:+212600000032";
        when(llmClient.classify("2")).thenReturn(result(Intent.CHOOSE_SLOT));

        sendWebhook("SM-INVALID-CHOICE", from, "2");

        verify(messageSender).send(from, "Pouvez-vous préciser votre demande ?");
        assertEquals(ConversationState.IDLE, stateOf(from));
    }

    @Test
    void lowConfidenceEscalatesAndDoesNotAdvanceConversation() throws Exception {
        var from = "whatsapp:+212600000033";
        when(llmClient.classify("peut-être un rendez-vous"))
                .thenReturn(new LlmResult(Intent.OTHER, 0.69, List.of(), "ignored"));

        sendWebhook("SM-LOW-CONFIDENCE", from, "peut-être un rendez-vous");

        verify(messageSender).send(from, ma.rafiq.application.ResponseTemplates.ESCALATION);
        assertEquals(ConversationState.ESCALATED, stateOf(from));
    }

    @Test
    void doesNotLogMessageMediaUrlOrCompletePhone(CapturedOutput output) throws Exception {
        var body = "s5ana";
        var fullPhone = "whatsapp:+212600000034";
        var mediaUrl = "https://twilio.test/private-medical-audio";

        sendWebhookWithMedia("SM-LOG-SAFETY", fullPhone, body, "audio/ogg", mediaUrl);

        assertFalse(output.getOut().contains(body));
        assertFalse(output.getOut().contains(mediaUrl));
        assertFalse(output.getOut().contains(fullPhone));
    }

    private void sendWebhook(String messageSid, String from, String body) throws Exception {
        var request = post("/webhooks/twilio/whatsapp")
                .param("MessageSid", messageSid)
                .param("From", from);
        if (body != null) request.param("Body", body);
        mvc.perform(request).andExpect(status().isOk());
    }

    private void sendWebhookWithMedia(String messageSid, String from, String body,
                                      String contentType, String mediaUrl) throws Exception {
        var request = post("/webhooks/twilio/whatsapp")
                .param("MessageSid", messageSid)
                .param("From", from)
                .param("NumMedia", "1")
                .param("MediaContentType0", contentType)
                .param("MediaUrl0", mediaUrl);
        if (body != null) request.param("Body", body);
        mvc.perform(request).andExpect(status().isOk());
    }

    @SuppressWarnings("unchecked")
    private ConversationState stateOf(String from) {
        var conversations = (Map<String, Conversation>) ReflectionTestUtils.getField(handler, "conversations");
        return conversations.get(from).state();
    }

    private static LlmResult result(Intent intent) {
        return new LlmResult(intent, 0.99, List.of(), "ignored");
    }
}
