package ma.rafiq.application;

import ma.rafiq.domain.Appointment;
import ma.rafiq.domain.Patient;
import ma.rafiq.domain.Slot;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookingUseCasesTest {
    @Test void proposesExactlyThreeAvailableSlots() {
        var slotRepository = new InMemorySlots(List.of(
                new Slot("1", Instant.parse("2026-10-20T09:00:00Z")),
                new Slot("2", Instant.parse("2026-10-20T10:00:00Z")),
                new Slot("3", Instant.parse("2026-10-20T11:00:00Z"))));

        assertEquals(3, new FindAvailableSlots(slotRepository).findThree().size());
    }

    @Test void preventsDoubleBooking() {
        var slots = new InMemorySlots(List.of(new Slot("1", Instant.now())));
        var appointments = new InMemoryAppointments();
        var useCase = new BookAppointment(slots, appointments);
        var appointment = new Appointment("a1", new Patient("p1", "Patient fictif"), slots.all().getFirst());
        useCase.book(appointment);

        assertThrows(IllegalStateException.class, () -> useCase.book(
                new Appointment("a2", new Patient("p2", "Autre patient fictif"), slots.all().getFirst())));
        assertEquals(1, appointments.saved.size());
    }

    private static final class InMemorySlots implements SlotRepository {
        private final List<Slot> all;
        private final List<String> reserved = new ArrayList<>();
        private InMemorySlots(List<Slot> all) { this.all = new ArrayList<>(all); }
        private List<Slot> all() { return all; }
        @Override public synchronized List<Slot> findAvailable(int limit) {
            return all.stream().filter(s -> !reserved.contains(s.id())).limit(limit).toList();
        }
        @Override public synchronized boolean reserve(String slotId) {
            if (reserved.contains(slotId)) return false;
            if (all.stream().noneMatch(s -> s.id().equals(slotId))) return false;
            reserved.add(slotId); return true;
        }
    }

    private static final class InMemoryAppointments implements AppointmentRepository {
        private final List<Appointment> saved = new ArrayList<>();
        @Override public Appointment save(Appointment appointment) { saved.add(appointment); return appointment; }
    }
}
