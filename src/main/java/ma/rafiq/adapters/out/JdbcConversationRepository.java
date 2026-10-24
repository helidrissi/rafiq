package ma.rafiq.adapters.out;

import ma.rafiq.application.ConversationRepository;
import ma.rafiq.domain.Conversation;
import ma.rafiq.domain.ConversationState;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;
import java.util.UUID;

public final class JdbcConversationRepository implements ConversationRepository {
    private final JdbcTemplate jdbc;

    public JdbcConversationRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public Optional<Conversation> findById(String id) {
        return jdbc.query("SELECT id, state FROM conversations WHERE id = ?", (rs, row) -> {
            var conversation = new Conversation(rs.getObject("id", UUID.class).toString());
            conversation.restoreState(ConversationState.valueOf(rs.getString("state")));
            return conversation;
        }, UUID.fromString(id)).stream().findFirst();
    }

    @Override public Conversation save(Conversation conversation) {
        jdbc.update("INSERT INTO conversations(id, state) VALUES (?, ?) ON CONFLICT (id) DO UPDATE SET state = EXCLUDED.state, updated_at = CURRENT_TIMESTAMP",
                UUID.fromString(conversation.id()), conversation.state().name());
        return conversation;
    }
}
