package ma.rafiq.domain;

import java.util.List;

public record LlmResult(Intent intent, double confidence, List<String> slotsRequested, String reply) {
    public LlmResult {
        if (confidence < 0 || confidence > 1) throw new IllegalArgumentException("confidence must be between 0 and 1");
        slotsRequested = slotsRequested == null ? List.of() : List.copyOf(slotsRequested);
    }
}
