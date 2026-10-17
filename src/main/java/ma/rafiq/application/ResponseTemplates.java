package ma.rafiq.application;

import ma.rafiq.domain.Intent;

public final class ResponseTemplates {
    public static final String ESCALATION = "Je transmets votre demande à la secrétaire.";

    private ResponseTemplates() { }

    public static String forIntent(Intent intent) {
        return switch (intent) {
            case BOOK_APPOINTMENT -> "Je vais vous proposer trois créneaux disponibles.";
            case CHOOSE_SLOT -> "Merci. Souhaitez-vous confirmer ce créneau ?";
            case CONFIRM -> "Votre rendez-vous est confirmé.";
            case DENY -> "D'accord. Aucun rendez-vous n'a été enregistré.";
            case OTHER -> "Pouvez-vous préciser votre demande ?";
            case MEDICAL_QUESTION -> ESCALATION;
        };
    }
}
