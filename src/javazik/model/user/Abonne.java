package javazik.model.user;

import javazik.model.media.Genre;
import javazik.model.media.Morceau;
import javazik.model.playlist.Playlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utilisateur abonné : peut créer des playlists,
 * consulter son historique, laisser des avis, etc.
 */
public class Abonne extends Utilisateur {
    private static final long serialVersionUID = 1L;

    private final List<Playlist> playlists;
    private final List<Morceau> historiqueEcoutes;

    public Abonne(String identifiant, String motDePasse, String nomAffichage) {
        super(identifiant, motDePasse, nomAffichage);
        this.playlists = new ArrayList<>();
        this.historiqueEcoutes = new ArrayList<>();
    }

    @Override
    public String getRole() { return "Abonné"; }

    public List<Playlist> getPlaylists() { return Collections.unmodifiableList(playlists); }

    public List<Morceau> getHistoriqueEcoutes() {
        return Collections.unmodifiableList(historiqueEcoutes);
    }

    public Playlist creerPlaylist(String nom) {
        Playlist p = new Playlist(nom, getIdentifiant());
        playlists.add(p);
        return p;
    }

    public boolean supprimerPlaylist(Playlist p) {
        return playlists.remove(p);
    }

    public void enregistrerEcoute(Morceau m) {
        if (m != null) historiqueEcoutes.add(m);
    }

    /** Trouve le genre le plus écouté par cet abonné. */
    public Genre getGenrePrefere() {
        if (historiqueEcoutes.isEmpty()) return null;
        Map<Genre, Integer> compteur = new EnumMap<>(Genre.class);
        for (Morceau m : historiqueEcoutes) {
            compteur.merge(m.getGenre(), 1, Integer::sum);
        }
        return compteur.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /** Renvoie le nom de l'interprète le plus souvent écouté. */
    public String getInterpretePrefere() {
        return historiqueEcoutes.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getInterprete().getNom(),
                        LinkedHashMap::new,
                        Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /** Supprime toute trace d'un morceau dans l'historique et les playlists. */
    public void retirerMorceauPartout(Morceau m) {
        historiqueEcoutes.removeIf(x -> x.equals(m));
        for (Playlist p : playlists) {
            p.retirerMorceau(m);
        }
    }

    public Playlist getPlaylistParId(String id) {
        return playlists.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst().orElse(null);
    }
}
