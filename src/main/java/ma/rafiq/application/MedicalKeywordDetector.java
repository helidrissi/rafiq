package ma.rafiq.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

public final class MedicalKeywordDetector {
    private final Pattern keywordPattern;

    public MedicalKeywordDetector() {
        try (var input = MedicalKeywordDetector.class.getResourceAsStream("/medical-keywords.json")) {
            if (input == null) throw new IllegalStateException("Missing medical keywords resource");
            var keywords = new ObjectMapper().readValue(input, String[].class);
            var alternatives = Arrays.stream(keywords).map(this::normalize).map(Pattern::quote).toList();
            keywordPattern = Pattern.compile("(?iu)(?<![\\p{L}\\p{N}])(?:"
                    + String.join("|", alternatives) + ")(?![\\p{L}\\p{N}])");
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load medical keywords", exception);
        }
    }

    public boolean matches(String message) {
        return message != null && keywordPattern.matcher(normalize(message)).find();
    }

    private String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
