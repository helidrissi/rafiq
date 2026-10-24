package ma.rafiq.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.rafiq.domain.Intent;
import ma.rafiq.domain.LlmResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DarijaFixtureTest {
    record Fixture(String message, String intent) { }

    static Stream<Arguments> messages() throws IOException {
        try (var input = DarijaFixtureTest.class.getResourceAsStream("/fixtures/messages-darija.json")) {
            if (input == null) throw new IllegalStateException("Missing darija fixture");
            return Stream.of(new ObjectMapper().readValue(input, Fixture[].class))
                    .map(fixture -> Arguments.of(fixture.message(), Intent.valueOf(fixture.intent())));
        }
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("messages")
    void fixtureContainsAValidExpectedIntent(String message, Intent expected) {
        var client = new ma.rafiq.testing.FakeLlmClient(new LlmResult(expected, 0.99, List.of(), "ignored"));
        assertEquals(expected, client.classify(message).intent());
    }
}
