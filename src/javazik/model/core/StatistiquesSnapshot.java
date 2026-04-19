package javazik.model.core;

import javazik.model.media.Genre;
import javazik.model.media.Morceau;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Photo instantanée des stats, affichée dans l'espace admin.
 */
public class StatistiquesSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int nbUtilisateurs;
    private final int nbAbonnes;
    private final int nbAdmins;
    private final int nbMorceaux;
    private final int nbAlbums;
    private final int nbEntites;
    private final long totalEcoutes;
    private final List<Morceau> topEcoutes;
    private final List<Morceau> topNotes;
    private final List<Morceau> topPlaylists;
    private final Map<Genre, Long> ecoutesParGenre;

    public StatistiquesSnapshot(int nbUtilisateurs, int nbAbonnes, int nbAdmins,
                                int nbMorceaux, int nbAlbums, int nbEntites,
                                long totalEcoutes,
                                List<Morceau> topEcoutes, List<Morceau> topNotes,
                                List<Morceau> topPlaylists, Map<Genre, Long> ecoutesParGenre) {
        this.nbUtilisateurs = nbUtilisateurs;
        this.nbAbonnes = nbAbonnes;
        this.nbAdmins = nbAdmins;
        this.nbMorceaux = nbMorceaux;
        this.nbAlbums = nbAlbums;
        this.nbEntites = nbEntites;
        this.totalEcoutes = totalEcoutes;
        this.topEcoutes = List.copyOf(topEcoutes);
        this.topNotes = List.copyOf(topNotes);
        this.topPlaylists = List.copyOf(topPlaylists);
        this.ecoutesParGenre = Map.copyOf(ecoutesParGenre);
    }

    public int getNbUtilisateurs() { return nbUtilisateurs; }
    public int getNbAbonnes() { return nbAbonnes; }
    public int getNbAdmins() { return nbAdmins; }
    public int getNbMorceaux() { return nbMorceaux; }
    public int getNbAlbums() { return nbAlbums; }
    public int getNbEntites() { return nbEntites; }
    public long getTotalEcoutes() { return totalEcoutes; }
    public List<Morceau> getTopEcoutes() { return Collections.unmodifiableList(topEcoutes); }
    public List<Morceau> getTopNotes() { return Collections.unmodifiableList(topNotes); }
    public List<Morceau> getTopPlaylists() { return Collections.unmodifiableList(topPlaylists); }
    public Map<Genre, Long> getEcoutesParGenre() { return Collections.unmodifiableMap(ecoutesParGenre); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Utilisateurs : ").append(nbUtilisateurs).append(System.lineSeparator());
        sb.append("Abonnés : ").append(nbAbonnes).append(System.lineSeparator());
        sb.append("Administrateurs : ").append(nbAdmins).append(System.lineSeparator());
        sb.append("Morceaux : ").append(nbMorceaux).append(System.lineSeparator());
        sb.append("Albums : ").append(nbAlbums).append(System.lineSeparator());
        sb.append("Artistes / groupes : ").append(nbEntites).append(System.lineSeparator());
        sb.append("Écoutes totales : ").append(totalEcoutes).append(System.lineSeparator());
        afficherListe(sb, "Top écoutes", topEcoutes, m -> m.getTitre() + " (" + m.getNombreEcoutes() + " écoute(s))");
        afficherListe(sb, "Top notes", topNotes, m -> m.getTitre() + " (" + String.format(java.util.Locale.US, "%.2f", m.getNoteMoyenne()) + "/5)");
        afficherListe(sb, "Top ajouts playlists", topPlaylists, m -> m.getTitre() + " (" + m.getNombreAjoutsPlaylists() + " ajout(s))");
        if (!ecoutesParGenre.isEmpty()) {
            sb.append("Écoutes par genre :").append(System.lineSeparator());
            ecoutesParGenre.forEach((g, v) ->
                    sb.append(" - ").append(g).append(" : ").append(v).append(System.lineSeparator()));
        }
        return sb.toString().trim();
    }

    private static void afficherListe(StringBuilder sb, String titre, List<Morceau> liste, Function<Morceau, String> fmt) {
        if (!liste.isEmpty()) {
            sb.append(titre).append(" :").append(System.lineSeparator());
            for (Morceau m : liste)
                sb.append(" - ").append(fmt.apply(m)).append(System.lineSeparator());
        }
    }
}
