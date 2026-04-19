package javazik.model.media;

import javazik.model.Descriptible;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Classe abstraite pour artiste solo ou groupe.
 */
public abstract class EntiteArtistique implements Serializable, Descriptible {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String nom;
    private final String pays;

    protected EntiteArtistique(String nom, String pays) {
        this.id = UUID.randomUUID().toString();
        this.nom = Objects.requireNonNull(nom).trim();
        this.pays = (pays == null || pays.isBlank()) ? "Inconnu" : pays.trim();
    }

    public String getId() { return id; }
    public String getNom() { return nom; }
    public String getPays() { return pays; }

    public abstract String getTypeLabel();

    @Override
    public String descriptionCourte() {
        return getTypeLabel() + " - " + nom + " (" + pays + ")";
    }

    @Override
    public String descriptionDetaillee() { return descriptionCourte(); }

    @Override
    public String toString() { return descriptionCourte(); }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EntiteArtistique autre)) return false;
        return id.equals(autre.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}
