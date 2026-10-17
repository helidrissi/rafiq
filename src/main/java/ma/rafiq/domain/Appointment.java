package ma.rafiq.domain;

import java.util.Objects;

public record Appointment(String id, Patient patient, Slot slot) {
    public Appointment {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(patient, "patient must not be null");
        Objects.requireNonNull(slot, "slot must not be null");
    }
}
