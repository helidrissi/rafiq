package ma.rafiq.adapters.in;

import ma.rafiq.application.MessageSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TwilioWebhookControllerIT {
    @Autowired MockMvc mvc;
    @MockBean MessageSender messageSender;

    @Test
    void receivesTwilioMessageAndEchoesThroughHandler() throws Exception {
        mvc.perform(post("/webhooks/twilio/whatsapp")
                        .param("MessageSid", "SM-123")
                        .param("From", "whatsapp:+212612345678")
                        .param("Body", "Bonjour"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/xml"));

        verify(messageSender).send("whatsapp:+212612345678", "Bonjour");
    }

    @Test
    void ignoresDuplicateMessageSid() throws Exception {
        var request = post("/webhooks/twilio/whatsapp")
                .param("MessageSid", "SM-DUP")
                .param("From", "whatsapp:+212612345678")
                .param("Body", "Bonjour");

        mvc.perform(request).andExpect(status().isOk());
        mvc.perform(request).andExpect(status().isOk());

        verify(messageSender, times(1)).send("whatsapp:+212612345678", "Bonjour");
    }
}
