package ma.rafiq.testing;

import ma.rafiq.domain.Intent;
import ma.rafiq.domain.LlmResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FakeLlmClientTest {
    @Test
    void returnsConfiguredResultWithoutCallingAnExternalService() {
        var expected = new LlmResult(Intent.BOOK_APPOINTMENT, 0.95, List.of("date"), "reply");
        var client = new FakeLlmClient(expected);

        assertEquals(expected, client.classify("bghit rdv"));
        assertEquals(1, client.callCount());
    }
}
