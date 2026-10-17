package ma.rafiq.domain;

import java.util.Objects;

public record Patient(String id, String displayName) {
    public Patient {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(displayName, "displayName must not be null");
    }
}
