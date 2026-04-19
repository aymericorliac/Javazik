package javazik.model.playlist;

import javazik.model.Descriptible;
import javazik.model.media.Morceau;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Playlist perso d'un abonné. */
public class Playlist implements Serializable, Descriptible {
    private static final long serialVersionUID = 1L;

    private final String id;
    private String nom;
    private final String proprietaireId;
    private final List<Morceau> morceaux;

    public Playlist(String nom, String proprietaireId) {
        this.id = UUID.randomUUID().toString();
        this.nom = Objects.requireNonNull(nom).trim();
        this.proprietaireId = Objects.requireNonNull(proprietaireId);
        this.morceaux = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getNom() { return nom; }
    public String getProprietaireId() { return proprietaireId; }
    public List<Morceau> getMorceaux() { return Collections.unmodifiableList(morceaux); }

    public int getDureeTotale() {
        return morceaux.stream().mapToInt(Morceau::getDureeSecondes).sum();
    }

    public void renommer(String nouveauNom) {
        this.nom = Objects.requireNonNull(nouveauNom).trim();
    }

    public boolean ajouterMorceau(Morceau m) {
        if (m != null && !morceaux.contains(m)) {
            morceaux.add(m);
            m.incrementerAjoutsPlaylists();
            return true;
        }
        return false;
    }

    /** Copie les morceaux d'une autre playlist (ignore les doublons). */
    public int ajouterDepuis(Playlist autre) {
        int compteur = 0;
        if (autre != null) {
            for (Morceau m : autre.getMorceaux()) {
                if (ajouterMorceau(m)) compteur++;
            }
        }
        return compteur;
    }

    public boolean retirerMorceau(Morceau m) {
        return morceaux.remove(m);
    }

    @Override
    public String descriptionCourte() {
        return "Playlist - " + nom + " (" + morceaux.size() + " morceau(x), "
                + Morceau.formaterDuree(getDureeTotale()) + ")";
    }

    @Override
    public String descriptionDetaillee() {
        StringBuilder sb = new StringBuilder(descriptionCourte());
        for (Morceau m : morceaux) {
            sb.append(System.lineSeparator()).append(" - ").append(m.getLabelLecture());
        }
        return sb.toString();
    }

    @Override
    public String toString() { return descriptionCourte(); }
}
