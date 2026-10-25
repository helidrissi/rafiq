package ma.rafiq.adapters.out;

import ma.rafiq.application.ProcessedMessageRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

public final class JdbcProcessedMessageRepository implements ProcessedMessageRepository {
    private final JdbcTemplate jdbc;

    public JdbcProcessedMessageRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public boolean markIfNew(String messageSid) {
        try {
            jdbc.update("INSERT INTO processed_messages(message_sid) VALUES (?)", messageSid);
            return true;
        } catch (DuplicateKeyException duplicate) {
            return false;
        }
    }
}
