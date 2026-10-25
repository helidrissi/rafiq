package ma.rafiq.application;

import ma.rafiq.domain.Intent;

public final class ResponseTemplates {
    public static final String ESCALATION = "Je transmets votre demande à la secrétaire.";
    public static final String MESSAGE_SANS_TEXTE = "Nous ne pouvons pas écouter les messages vocaux ni lire les images. "
            + "Si c'est urgent, appelez le 141 (SAMU). Sinon, écrivez votre demande en quelques mots, "
            + "la secrétaire vous répondra.\n"
            + "Ma kan9drouch nsem3o l-vocal wla nchoufo tswira. Ila kan chi haja mosta3jla, "
            + "3iyto l 141. Wla ktbo lina chno bghitou, w l-secrétaire ghadi tjawbkom.";

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
