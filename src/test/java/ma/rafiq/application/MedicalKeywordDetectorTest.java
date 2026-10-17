package ma.rafiq.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MedicalKeywordDetectorTest {
    private final MedicalKeywordDetector detector = new MedicalKeywordDetector();

    @Test void detectsFrenchKeywordWithAccent() { assertTrue(detector.matches("J'ai de la fièvre")); }
    @Test void detectsDarijaAndArabiziKeywords() {
        assertTrue(detector.matches("3andi s5ana"));
        assertTrue(detector.matches("kayna skhana"));
    }
    @Test void ignoresNonMedicalMessage() { assertFalse(detector.matches("bghit rdv ghda")); }
}
