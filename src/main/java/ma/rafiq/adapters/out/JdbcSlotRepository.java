package ma.rafiq.adapters.out;

import ma.rafiq.application.SlotRepository;
import ma.rafiq.domain.Slot;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;

public final class JdbcSlotRepository implements SlotRepository {
    private final JdbcTemplate jdbc;

    public JdbcSlotRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public List<Slot> findAvailable(int limit) {
        return jdbc.query("SELECT id, starts_at FROM slots WHERE reserved = FALSE ORDER BY starts_at LIMIT ?",
                (rs, row) -> new Slot(rs.getObject("id", UUID.class).toString(), rs.getTimestamp("starts_at").toInstant()), limit);
    }

    @Override public boolean reserve(String slotId) {
        return jdbc.update("UPDATE slots SET reserved = TRUE WHERE id = ? AND reserved = FALSE", UUID.fromString(slotId)) == 1;
    }
}
