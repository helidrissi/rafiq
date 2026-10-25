package ma.rafiq.application;

import ma.rafiq.domain.Intent;
import ma.rafiq.domain.Conversation;
import ma.rafiq.domain.LlmResult;
import ma.rafiq.domain.InvalidConversationTransitionException;
import ma.rafiq.domain.ConversationState;

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
        var conversation = conversations.computeIfAbsent(recipient, Conversation::new);
        if (message == null || message.isBlank()) {
            conversation.escalate();
            messageSender.send(recipient, ResponseTemplates.MESSAGE_SANS_TEXTE);
            return;
        }
        if (medicalKeywordDetector.matches(message)) {
            conversation.escalate();
            messageSender.send(recipient, ResponseTemplates.ESCALATION);
            return;
        }
        LlmResult result = llmClient.classify(message);
        String reply;
        if (result.confidence() < ESCALATION_THRESHOLD || result.intent() == Intent.MEDICAL_QUESTION) {
            conversation.escalate();
            reply = ResponseTemplates.ESCALATION;
        } else {
            try { reply = transition(conversation, result.intent()); }
            catch (InvalidConversationTransitionException invalid) { reply = ResponseTemplates.forIntent(Intent.OTHER); }
        }
        messageSender.send(recipient, reply);
    }

    ConversationState conversationState(String recipient) {
        return conversations.get(recipient) == null ? null : conversations.get(recipient).state();
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
