package ma.rafiq.adapters.in;

import ma.rafiq.application.HandleIncomingMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class TwilioWebhookController {
    private static final Logger log = LoggerFactory.getLogger(TwilioWebhookController.class);
    private final HandleIncomingMessage handler;
    private final Set<String> processedMessageSids = ConcurrentHashMap.newKeySet();

    public TwilioWebhookController(HandleIncomingMessage handler) {
        this.handler = handler;
    }

    @PostMapping(value = "/webhooks/twilio/whatsapp", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> receive(
            @RequestParam(name = "MessageSid") String messageSid,
            @RequestParam(name = "From") String from,
            @RequestParam(name = "Body") String body) {
        if (!processedMessageSids.add(messageSid)) {
            log.info("Ignoring duplicate Twilio message {} from {}", messageSid, maskPhone(from));
            return ResponseEntity.ok(twiml());
        }
        log.info("Received Twilio message {} from {}", messageSid, maskPhone(from));
        handler.echo(from, body);
        return ResponseEntity.ok(twiml());
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 5) return "***";
        return phone.substring(0, Math.min(7, phone.length())) + "***";
    }

    private static String twiml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response></Response>";
    }
}
