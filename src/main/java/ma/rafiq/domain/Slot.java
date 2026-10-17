package ma.rafiq.domain;

import java.time.Instant;
import java.util.Objects;

public record Slot(String id, Instant startsAt) {
    public Slot {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(startsAt, "startsAt must not be null");
    }
}
