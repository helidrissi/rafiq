package ma.rafiq.application;

import ma.rafiq.domain.Intent;
import ma.rafiq.domain.Conversation;
import ma.rafiq.domain.LlmResult;
import ma.rafiq.domain.InvalidConversationTransitionException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HandleIncomingMessage {
    private static final double ESCALATION_THRESHOLD = 0.7;
    private final LlmClient llmClient;
    private final MessageSender messageSender;
    private final MedicalKeywordDetector medicalKeywordDetector;
    private final Map<String, Conversation> conversations = new ConcurrentHashMap<>();

    public HandleIncomingMessage(LlmClient llmClient, MessageSender messageSender) {
        this(llmClient, messageSender, new MedicalKeywordDetector());
    }

    public HandleIncomingMessage(LlmClient llmClient, MessageSender messageSender,
                                 MedicalKeywordDetector medicalKeywordDetector) {
        this.llmClient = llmClient;
        this.messageSender = messageSender;
        this.medicalKeywordDetector = medicalKeywordDetector;
    }

    public void handle(String recipient, String message) {
        if (medicalKeywordDetector.matches(message)) {
            messageSender.send(recipient, ResponseTemplates.ESCALATION);
            return;
        }
        LlmResult result = llmClient.classify(message);
        String reply;
        if (result.confidence() < ESCALATION_THRESHOLD || result.intent() == Intent.MEDICAL_QUESTION) {
            reply = ResponseTemplates.ESCALATION;
        } else {
            var conversation = conversations.computeIfAbsent(recipient, Conversation::new);
            try { reply = transition(conversation, result.intent()); }
            catch (InvalidConversationTransitionException invalid) { reply = ResponseTemplates.forIntent(Intent.OTHER); }
        }
        messageSender.send(recipient, reply);
    }

    private String transition(Conversation conversation, Intent intent) {
        return switch (intent) {
            case BOOK_APPOINTMENT -> { conversation.proposeSlots(); yield ResponseTemplates.forIntent(intent); }
            case CHOOSE_SLOT -> { conversation.requestConfirmation(); yield ResponseTemplates.forIntent(intent); }
            case CONFIRM -> { conversation.book(); yield ResponseTemplates.forIntent(intent); }
            case DENY, OTHER -> ResponseTemplates.forIntent(intent);
            case MEDICAL_QUESTION -> ResponseTemplates.ESCALATION;
        };
    }
}
