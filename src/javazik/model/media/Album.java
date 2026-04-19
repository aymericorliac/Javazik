package javazik.model.media;

import javazik.model.Descriptible;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Album implements Serializable, Descriptible {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String titre;
    private final int annee;
    private final EntiteArtistique artiste;
    private final List<Morceau> morceaux;

    public Album(String titre, int annee, EntiteArtistique artiste) {
        this.id = UUID.randomUUID().toString();
        this.titre = Objects.requireNonNull(titre).trim();
        this.annee = annee;
        this.artiste = Objects.requireNonNull(artiste);
        this.morceaux = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getTitre() { return titre; }
    public int getAnnee() { return annee; }
    public EntiteArtistique getArtiste() { return artiste; }
    public List<Morceau> getMorceaux() { return Collections.unmodifiableList(morceaux); }

    public void ajouterMorceau(Morceau m) {
        if (m != null && !morceaux.contains(m)) {
            morceaux.add(m);
            m.ajouterAlbum(this);
        }
    }

    public void retirerMorceau(Morceau m) {
        if (m != null && morceaux.remove(m)) {
            m.retirerAlbum(this);
        }
    }

    @Override
    public String descriptionCourte() {
        return "Album - " + titre + " (" + annee + ") - " + artiste.getNom();
    }

    @Override
    public String descriptionDetaillee() {
        StringBuilder sb = new StringBuilder(descriptionCourte());
        sb.append(System.lineSeparator()).append("Nombre de morceaux : ").append(morceaux.size());
        for (Morceau m : morceaux) {
            sb.append(System.lineSeparator()).append(" - ").append(m.getTitre());
        }
        return sb.toString();
    }

    @Override public String toString() { return descriptionCourte(); }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Album a)) return false;
        return id.equals(a.id);
    }

    @Override public int hashCode() { return id.hashCode(); }
}
