package ma.rafiq.domain;

public class InvalidConversationTransitionException extends RuntimeException {
    public InvalidConversationTransitionException(ConversationState from, ConversationState to) {
        super("Invalid conversation transition: " + from + " -> " + to);
    }
}
