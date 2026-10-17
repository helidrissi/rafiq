package ma.rafiq.domain;

public enum ConversationState {
    IDLE,
    SLOTS_PROPOSED,
    AWAITING_CONFIRMATION,
    BOOKED,
    ESCALATED
}
