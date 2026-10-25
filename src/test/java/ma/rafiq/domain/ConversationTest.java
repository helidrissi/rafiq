package ma.rafiq.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConversationTest {
    @Test void startsIdle() {
        assertEquals(ConversationState.IDLE, new Conversation("conversation-1").state());
    }

    @Test void transitionsFromIdleToSlotsProposed() {
        var conversation = new Conversation("conversation-1");
        conversation.proposeSlots();
        assertEquals(ConversationState.SLOTS_PROPOSED, conversation.state());
    }

    @Test void transitionsFromSlotsProposedToAwaitingConfirmation() {
        var conversation = new Conversation("conversation-1");
        conversation.proposeSlots();
        conversation.requestConfirmation();
        assertEquals(ConversationState.AWAITING_CONFIRMATION, conversation.state());
    }

    @Test void transitionsFromAwaitingConfirmationToBooked() {
        var conversation = new Conversation("conversation-1");
        conversation.proposeSlots();
        conversation.requestConfirmation();
        conversation.book();
        assertEquals(ConversationState.BOOKED, conversation.state());
    }

    @Test void escalatesFromAnyState() {
        var conversation = new Conversation("conversation-1");
        conversation.escalate();
        assertEquals(ConversationState.ESCALATED, conversation.state());
    }

    @Test void resumesBookingFromEscalated() {
        var conversation = new Conversation("conversation-1");
        conversation.escalate();

        conversation.resumeBooking();

        assertEquals(ConversationState.SLOTS_PROPOSED, conversation.state());
    }

    @Test void rejectsResumingBookingBeforeEscalation() {
        var conversation = new Conversation("conversation-1");
        assertThrows(InvalidConversationTransitionException.class, conversation::resumeBooking);
    }

    @Test void rejectsBookingDirectlyFromEscalated() {
        var conversation = new Conversation("conversation-1");
        conversation.escalate();

        assertThrows(InvalidConversationTransitionException.class, conversation::book);
    }

    @Test void rejectsInvalidTransition() {
        var conversation = new Conversation("conversation-1");
        assertThrows(InvalidConversationTransitionException.class, conversation::book);
    }
}
