package javazik.model.review;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/** Avis (note + commentaire) laissé par un abonné sur un morceau. */
public class AvisMorceau implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String auteurIdentifiant;
    private int note;
    private String commentaire;
    private LocalDateTime dateMaj;

    public AvisMorceau(String auteurIdentifiant, int note, String commentaire) {
        this.auteurIdentifiant = Objects.requireNonNull(auteurIdentifiant);
        this.note = note;
        this.commentaire = commentaire == null ? "" : commentaire.trim();
        this.dateMaj = LocalDateTime.now();
    }

    public String getAuteurIdentifiant() { return auteurIdentifiant; }
    public int getNote() { return note; }
    public String getCommentaire() { return commentaire; }
    public LocalDateTime getDateMaj() { return dateMaj; }

    public void mettreAJour(int note, String commentaire) {
        this.note = note;
        this.commentaire = commentaire == null ? "" : commentaire.trim();
        this.dateMaj = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return auteurIdentifiant + " - " + note + "/5"
                + (commentaire.isBlank() ? "" : " : " + commentaire);
    }
}
