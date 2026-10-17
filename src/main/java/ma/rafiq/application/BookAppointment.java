package ma.rafiq.application;

import ma.rafiq.domain.Appointment;

import java.util.Objects;

public final class BookAppointment {
    private final SlotRepository slots;
    private final AppointmentRepository appointments;

    public BookAppointment(SlotRepository slots, AppointmentRepository appointments) {
        this.slots = slots;
        this.appointments = appointments;
    }

    public Appointment book(Appointment appointment) {
        Objects.requireNonNull(appointment, "appointment must not be null");
        if (!slots.reserve(appointment.slot().id())) {
            throw new IllegalStateException("Slot is already reserved");
        }
        return appointments.save(appointment);
    }
}
