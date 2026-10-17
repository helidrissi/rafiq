package ma.rafiq.application;

import ma.rafiq.domain.Slot;

import java.util.List;

public interface SlotRepository {
    List<Slot> findAvailable(int limit);
}
