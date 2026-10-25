package ma.rafiq.adapters.in;

import ma.rafiq.application.MessageSender;
import ma.rafiq.application.LlmClient;
import ma.rafiq.domain.Intent;
import ma.rafiq.domain.LlmResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
@AutoConfigureMockMvc
class TwilioWebhookControllerIT {
    @Autowired MockMvc mvc;
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
}
