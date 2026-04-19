package javazik.exception;

/** Levée quand on essaie d'ajouter un élément déjà existant. */
public class DoublonException extends JavazikException {
    public DoublonException(String msg) { super(msg); }
}
