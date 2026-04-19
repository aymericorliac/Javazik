package javazik.exception;

/** Pour les champs mal remplis, valeurs hors bornes, etc. */
public class SaisieInvalideException extends JavazikException {
    public SaisieInvalideException(String message) { super(message); }
}
