package ma.rafiq.application;

import ma.rafiq.domain.Conversation;

import java.util.Optional;

public interface ConversationRepository {
    Optional<Conversation> findById(String id);
    Conversation save(Conversation conversation);
}
