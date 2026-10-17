package ma.rafiq.application;

import ma.rafiq.domain.Slot;

import java.util.List;

public final class FindAvailableSlots {
    private final SlotRepository slots;

    public FindAvailableSlots(SlotRepository slots) { this.slots = slots; }

    public List<Slot> findThree() {
        var result = slots.findAvailable(3);
        if (result.size() != 3) throw new IllegalStateException("Exactly three slots are required");
        return List.copyOf(result);
    }
}
