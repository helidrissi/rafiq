package ma.rafiq.e2e;

import ma.rafiq.application.LlmClient;
import ma.rafiq.application.MessageSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
@AutoConfigureMockMvc
class TwilioMedicalEscalationE2ETest {
    @Autowired MockMvc mvc;
    @MockBean MessageSender messageSender;
    @MockBean LlmClient llmClient;

    @Test
    void escalatesMedicalMessageReceivedThroughTwilioWebhook() throws Exception {
        mvc.perform(post("/webhooks/twilio/whatsapp")
                        .param("MessageSid", "SM-E2E-FIEVRE")
                        .param("From", "whatsapp:+212612345678")
                        .param("Body", "bébé fièvre 40"))
                .andExpect(status().isOk());

        verify(messageSender).send(
                "whatsapp:+212612345678",
                "Je transmets votre demande à la secrétaire.");
    }
}
