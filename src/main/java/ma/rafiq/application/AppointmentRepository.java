package ma.rafiq.application;

import ma.rafiq.domain.Appointment;

public interface AppointmentRepository {
    Appointment save(Appointment appointment);
}
