package javazik.model.core;

import javazik.model.media.Album;
import javazik.model.media.Artiste;
import javazik.model.media.EntiteArtistique;
import javazik.model.media.Genre;
import javazik.model.media.Groupe;
import javazik.model.media.Morceau;
import javazik.model.media.ModeTri;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Catalogue central : stocke morceaux, albums, artistes/groupes. */
public class Catalogue implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, EntiteArtistique> entites;
    private final Map<String, Album> albums;
    private final Map<String, Morceau> morceaux;

    public Catalogue() {
        this.entites = new LinkedHashMap<>();
        this.albums = new LinkedHashMap<>();
        this.morceaux = new LinkedHashMap<>();
    }

    // --- accesseurs ---
    public List<EntiteArtistique> getEntitesArtistiques() { return new ArrayList<>(entites.values()); }
    public List<Artiste> getArtistes() {
        return entites.values().stream().filter(Artiste.class::isInstance).map(Artiste.class::cast).toList();
    }
    public List<Groupe> getGroupes() {
        return entites.values().stream().filter(Groupe.class::isInstance).map(Groupe.class::cast).toList();
    }
    public List<Album> getAlbums() { return new ArrayList<>(albums.values()); }
    public List<Morceau> getMorceaux() { return new ArrayList<>(morceaux.values()); }

    // --- ajout / suppression ---
    public void ajouterEntite(EntiteArtistique e) { entites.put(e.getId(), e); }
    public void ajouterAlbum(Album a) { albums.put(a.getId(), a); }
    public void ajouterMorceau(Morceau m) { morceaux.put(m.getId(), m); }

    public EntiteArtistique getEntiteParId(String id) { return entites.get(id); }
    public Album getAlbumParId(String id) { return albums.get(id); }
    public Morceau getMorceauParId(String id) { return morceaux.get(id); }

    public EntiteArtistique supprimerEntite(String id) { return entites.remove(id); }
    public Album supprimerAlbum(String id) { return albums.remove(id); }
    public Morceau supprimerMorceau(String id) { return morceaux.remove(id); }

    // --- recherches ---
    public List<Morceau> rechercherMorceaux(String requete) {
        String q = normaliser(requete);
        return morceaux.values().stream()
                .filter(m -> q.isBlank() || normaliser(m.getTitre()).contains(q)
                        || normaliser(m.getInterprete().getNom()).contains(q))
                .sorted(Comparator.comparing(Morceau::getTitre))
                .toList();
    }

    public List<Album> rechercherAlbums(String requete) {
        String q = normaliser(requete);
        return albums.values().stream()
                .filter(a -> q.isBlank() || normaliser(a.getTitre()).contains(q)
                        || normaliser(a.getArtiste().getNom()).contains(q))
                .sorted(Comparator.comparing(Album::getTitre))
                .toList();
    }

    public List<EntiteArtistique> rechercherEntites(String requete) {
        String q = normaliser(requete);
        return entites.values().stream()
                .filter(e -> q.isBlank() || normaliser(e.getNom()).contains(q))
                .sorted(Comparator.comparing(EntiteArtistique::getNom))
                .toList();
    }

    public List<Morceau> rechercheAvancee(String requete, Genre genre,
                                          Integer anneeMin, Integer anneeMax, ModeTri tri) {
        String q = normaliser(requete);
        Comparator<Morceau> comp = switch (tri == null ? ModeTri.TITRE : tri) {
            case TITRE -> Comparator.comparing(Morceau::getTitre);
            case ANNEE -> Comparator.comparingInt(Morceau::getAnnee).thenComparing(Morceau::getTitre);
            case DUREE -> Comparator.comparingInt(Morceau::getDureeSecondes).thenComparing(Morceau::getTitre);
            case ECOUTES -> Comparator.comparingInt(Morceau::getNombreEcoutes).reversed().thenComparing(Morceau::getTitre);
            case NOTE_MOYENNE -> Comparator.comparingDouble(Morceau::getNoteMoyenne).reversed().thenComparing(Morceau::getTitre);
        };
        return morceaux.values().stream()
                .filter(m -> q.isBlank() || normaliser(m.getTitre()).contains(q)
                        || normaliser(m.getInterprete().getNom()).contains(q))
                .filter(m -> genre == null || m.getGenre() == genre)
                .filter(m -> anneeMin == null || m.getAnnee() >= anneeMin)
                .filter(m -> anneeMax == null || m.getAnnee() <= anneeMax)
                .sorted(comp)
                .toList();
    }

    // --- navigation ---
    public List<Morceau> getMorceauxParEntite(EntiteArtistique e) {
        return morceaux.values().stream()
                .filter(m -> m.getInterprete().equals(e))
                .sorted(Comparator.comparing(Morceau::getTitre))
                .toList();
    }

    public List<Album> getAlbumsParEntite(EntiteArtistique e) {
        return albums.values().stream()
                .filter(a -> a.getArtiste().equals(e))
                .sorted(Comparator.comparing(Album::getAnnee).thenComparing(Album::getTitre))
                .toList();
    }

    public List<Morceau> getMorceauxParAlbum(Album album) {
        return album.getMorceaux().stream()
                .sorted(Comparator.comparing(Morceau::getTitre)).toList();
    }

    // --- stats ---
    public List<Morceau> getTopEcoutes(int limite) {
        return morceaux.values().stream()
                .sorted(Comparator.comparingInt(Morceau::getNombreEcoutes).reversed()
                        .thenComparing(Morceau::getTitre))
                .limit(limite).toList();
    }

    public List<Morceau> getTopNotes(int limite) {
        return morceaux.values().stream()
                .filter(m -> !m.getAvis().isEmpty())
                .sorted(Comparator.comparingDouble(Morceau::getNoteMoyenne).reversed()
                        .thenComparing(Morceau::getTitre))
                .limit(limite).toList();
    }

    public List<Morceau> getTopAjoutsPlaylists(int limite) {
        return morceaux.values().stream()
                .filter(m -> m.getNombreAjoutsPlaylists() > 0)
                .sorted(Comparator.comparingInt(Morceau::getNombreAjoutsPlaylists).reversed()
                        .thenComparing(Morceau::getTitre))
                .limit(limite).toList();
    }

    public Map<Genre, Long> getEcoutesParGenre() {
        Map<Genre, Long> stats = new EnumMap<>(Genre.class);
        for (Morceau m : morceaux.values()) {
            stats.merge(m.getGenre(), (long) m.getNombreEcoutes(), Long::sum);
        }
        return stats;
    }

    /** Texte de navigation depuis un morceau : infos, autres morceaux, albums. */
    public String naviguerDepuisMorceau(Morceau morceau) {
        StringBuilder sb = new StringBuilder(morceau.descriptionDetaillee());
        EntiteArtistique interprete = morceau.getInterprete();
        sb.append(System.lineSeparator()).append(System.lineSeparator())
                .append("Explorer ").append(interprete.getNom()).append(" :");
        List<Morceau> autres = getMorceauxParEntite(interprete).stream()
                .filter(m -> !m.equals(morceau)).limit(5).toList();
        if (autres.isEmpty()) {
            sb.append(System.lineSeparator()).append(" - Aucun autre morceau");
        } else {
            for (Morceau a : autres)
                sb.append(System.lineSeparator()).append(" - ").append(a.getTitre());
        }
        sb.append(System.lineSeparator()).append(System.lineSeparator()).append("Albums associés :");
        if (morceau.getAlbums().isEmpty()) {
            sb.append(System.lineSeparator()).append(" - Aucun album");
        } else {
            for (Album alb : morceau.getAlbums())
                sb.append(System.lineSeparator()).append(" - ").append(alb.getTitre())
                        .append(" (").append(alb.getAnnee()).append(")");
        }
        return sb.toString();
    }

    private String normaliser(String texte) {
        return texte == null ? "" : texte.toLowerCase(Locale.ROOT).trim();
    }
}
