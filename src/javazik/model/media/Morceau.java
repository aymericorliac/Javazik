package javazik.model.media;

import javazik.model.Descriptible;
import javazik.model.Jouable;
import javazik.model.review.AvisMorceau;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/** Représente un morceau du catalogue. */
public class Morceau implements Serializable, Descriptible, Jouable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String titre;
    private final int dureeSecondes;
    private final Genre genre;
    private final int annee;
    private final EntiteArtistique interprete;
    private final List<Album> albums;
    private final List<AvisMorceau> avis;
    private int nbEcoutes;
    private int nbAjoutsPlaylists;

    public Morceau(String titre, int dureeSecondes, Genre genre, int annee, EntiteArtistique interprete) {
        this.id = UUID.randomUUID().toString();
        this.titre = Objects.requireNonNull(titre).trim();
        this.dureeSecondes = dureeSecondes;
        this.genre = Objects.requireNonNull(genre);
        this.annee = annee;
        this.interprete = Objects.requireNonNull(interprete);
        this.albums = new ArrayList<>();
        this.avis = new ArrayList<>();
        this.nbEcoutes = 0;
        this.nbAjoutsPlaylists = 0;
    }

    // --- getters ---
    public String getId() { return id; }
    public String getTitre() { return titre; }
    @Override public int getDureeSecondes() { return dureeSecondes; }
    public Genre getGenre() { return genre; }
    public int getAnnee() { return annee; }
    public EntiteArtistique getInterprete() { return interprete; }
    public List<Album> getAlbums() { return Collections.unmodifiableList(albums); }
    public int getNombreEcoutes() { return nbEcoutes; }
    public List<AvisMorceau> getAvis() { return Collections.unmodifiableList(avis); }
    public int getNombreAjoutsPlaylists() { return nbAjoutsPlaylists; }

    public double getNoteMoyenne() {
        if (avis.isEmpty()) return 0.0;
        int total = 0;
        for (AvisMorceau a : avis) total += a.getNote();
        return total / (double) avis.size();
    }

    public void ajouterAlbum(Album album) {
        if (album != null && !albums.contains(album)) albums.add(album);
    }

    public void retirerAlbum(Album album) { albums.remove(album); }

    public void incrementerEcoutes() { nbEcoutes++; }

    public void incrementerAjoutsPlaylists() { nbAjoutsPlaylists++; }

    public void ajouterOuMettreAJourAvis(String auteurId, int note, String commentaire) {
        for (AvisMorceau av : avis) {
            if (av.getAuteurIdentifiant().equals(auteurId)) {
                av.mettreAJour(note, commentaire);
                return;
            }
        }
        avis.add(new AvisMorceau(auteurId, note, commentaire));
    }

    public void supprimerAvis(String auteurId) {
        avis.removeIf(a -> a.getAuteurIdentifiant().equals(auteurId));
    }

    @Override
    public String getLabelLecture() {
        return titre + " — " + interprete.getNom();
    }

    @Override
    public String descriptionCourte() {
        return "Morceau - " + titre + " - " + interprete.getNom()
                + " [" + genre + ", " + annee + ", " + formaterDuree(dureeSecondes) + "]";
    }

    @Override
    public String descriptionDetaillee() {
        StringBuilder sb = new StringBuilder(descriptionCourte());
        sb.append(System.lineSeparator()).append("Interprète : ").append(interprete.getNom());
        sb.append(System.lineSeparator()).append("Durée : ").append(formaterDuree(dureeSecondes));
        sb.append(System.lineSeparator()).append("Écoutes : ").append(nbEcoutes);
        sb.append(System.lineSeparator()).append("Ajouts en playlists : ").append(nbAjoutsPlaylists);
        sb.append(System.lineSeparator()).append("Note moyenne : ")
                .append(String.format(Locale.US, "%.2f", getNoteMoyenne()));
        if (!albums.isEmpty()) {
            sb.append(System.lineSeparator()).append("Albums : ");
            for (int i = 0; i < albums.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(albums.get(i).getTitre());
            }
        }
        if (!avis.isEmpty()) {
            sb.append(System.lineSeparator()).append("Avis :");
            for (AvisMorceau av : avis) {
                sb.append(System.lineSeparator()).append(" - ").append(av);
            }
        }
        return sb.toString();
    }

    /** Formate une durée en secondes vers "mm:ss". */
    public static String formaterDuree(int secondes) {
        int min = secondes / 60;
        int sec = secondes % 60;
        return String.format("%02d:%02d", min, sec);
    }

    @Override public String toString() { return descriptionCourte(); }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Morceau m)) return false;
        return id.equals(m.id);
    }

    @Override public int hashCode() { return id.hashCode(); }
}
