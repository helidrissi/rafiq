package ma.rafiq.application;

import ma.rafiq.domain.Intent;
import ma.rafiq.domain.LlmResult;

public class HandleIncomingMessage {
    private static final double ESCALATION_THRESHOLD = 0.7;
    private final LlmClient llmClient;
    private final MessageSender messageSender;
    private final MedicalKeywordDetector medicalKeywordDetector;

    public HandleIncomingMessage(LlmClient llmClient, MessageSender messageSender) {
        this(llmClient, messageSender, new MedicalKeywordDetector());
    }

    public HandleIncomingMessage(LlmClient llmClient, MessageSender messageSender,
                                 MedicalKeywordDetector medicalKeywordDetector) {
        this.llmClient = llmClient;
        this.messageSender = messageSender;
        this.medicalKeywordDetector = medicalKeywordDetector;
    }

    public void echo(String recipient, String message) {
        messageSender.send(recipient, message);
    }

    public void handle(String recipient, String message) {
        if (medicalKeywordDetector.matches(message)) {
            messageSender.send(recipient, ResponseTemplates.ESCALATION);
            return;
        }
        LlmResult result = llmClient.classify(message);
        String reply = result.confidence() < ESCALATION_THRESHOLD || result.intent() == Intent.MEDICAL_QUESTION
                ? ResponseTemplates.ESCALATION
                : ResponseTemplates.forIntent(result.intent());
        messageSender.send(recipient, reply);
    }
}
