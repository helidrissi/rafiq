package ma.rafiq.adapters.out;

import ma.rafiq.application.AppointmentRepository;
import ma.rafiq.domain.Appointment;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

public final class JdbcAppointmentRepository implements AppointmentRepository {
    private final JdbcTemplate jdbc;

    public JdbcAppointmentRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public Appointment save(Appointment appointment) {
        jdbc.update("INSERT INTO appointments(id, patient_id, slot_id) VALUES (?, ?, ?)",
                UUID.fromString(appointment.id()), UUID.fromString(appointment.patient().id()), UUID.fromString(appointment.slot().id()));
        return appointment;
    }
}
