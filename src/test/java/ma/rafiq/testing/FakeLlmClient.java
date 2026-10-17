package ma.rafiq.testing;

import ma.rafiq.application.LlmClient;
import ma.rafiq.domain.LlmResult;

import java.util.Objects;

/** Test double for deterministic use-case tests; never calls an external LLM. */
public final class FakeLlmClient implements LlmClient {
    private final LlmResult result;
    private int callCount;

    public FakeLlmClient(LlmResult result) {
        this.result = Objects.requireNonNull(result, "result must not be null");
    }

    @Override
    public LlmResult classify(String message) {
        callCount++;
        return result;
    }

    public int callCount() { return callCount; }
}
