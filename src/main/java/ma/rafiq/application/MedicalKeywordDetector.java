package ma.rafiq.application;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;

public final class MedicalKeywordDetector {
    private static final Set<String> KEYWORDS = Set.of("fievre", "douleur", "saigne", "urgence", "s5ana", "skhana");

    public boolean matches(String message) {
        if (message == null || message.isBlank()) return false;
        String normalized = Normalizer.normalize(message, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
        return KEYWORDS.stream().anyMatch(normalized::contains);
    }
}
