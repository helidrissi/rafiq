package ma.rafiq.application;

import ma.rafiq.domain.Intent;
import ma.rafiq.domain.LlmResult;

public class HandleIncomingMessage {
    private static final double ESCALATION_THRESHOLD = 0.7;
    private final LlmClient llmClient;
    private final MessageSender messageSender;

    public HandleIncomingMessage(LlmClient llmClient, MessageSender messageSender) {
        this.llmClient = llmClient; this.messageSender = messageSender;
    }

    public void echo(String recipient, String message) {
        messageSender.send(recipient, message);
    }

    public void handle(String recipient, String message) {
        LlmResult result = llmClient.classify(message);
        String reply = result.confidence() < ESCALATION_THRESHOLD || result.intent() == Intent.MEDICAL_QUESTION
                ? "Je transmets votre demande à la secrétaire."
                : result.reply();
        messageSender.send(recipient, reply);
    }
}
