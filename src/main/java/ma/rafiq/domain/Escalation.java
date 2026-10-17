package ma.rafiq.domain;

import java.util.Objects;

public record Escalation(String id, String reason) {
    public Escalation {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(reason, "reason must not be null");
    }
}
