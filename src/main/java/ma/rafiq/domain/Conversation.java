package ma.rafiq.domain;

import java.util.Objects;

public final class Conversation {
    private final String id;
    private ConversationState state;

    public Conversation(String id) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.state = ConversationState.IDLE;
    }

    public String id() { return id; }
    public ConversationState state() { return state; }

    public void restoreState(ConversationState state) {
        this.state = Objects.requireNonNull(state, "state must not be null");
    }

    public void proposeSlots() { transitionTo(ConversationState.SLOTS_PROPOSED); }

    public void resumeBooking() {
        if (state != ConversationState.ESCALATED) {
            throw new InvalidConversationTransitionException(state, ConversationState.SLOTS_PROPOSED);
        }
        transitionTo(ConversationState.SLOTS_PROPOSED);
    }
    public void requestConfirmation() { transitionTo(ConversationState.AWAITING_CONFIRMATION); }
    public void book() { transitionTo(ConversationState.BOOKED); }

    public void escalate() {
        if (state != ConversationState.ESCALATED) state = ConversationState.ESCALATED;
    }

    private void transitionTo(ConversationState target) {
        if (!isAllowed(state, target)) throw new InvalidConversationTransitionException(state, target);
        state = target;
    }

    private static boolean isAllowed(ConversationState from, ConversationState to) {
        return (from == ConversationState.IDLE && to == ConversationState.SLOTS_PROPOSED)
                || (from == ConversationState.ESCALATED && to == ConversationState.SLOTS_PROPOSED)
                || (from == ConversationState.SLOTS_PROPOSED && to == ConversationState.AWAITING_CONFIRMATION)
                || (from == ConversationState.AWAITING_CONFIRMATION && to == ConversationState.BOOKED);
    }
}
