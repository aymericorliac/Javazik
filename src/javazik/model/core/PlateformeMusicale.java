package javazik.model.core;

import javazik.exception.AuthentificationException;
import javazik.exception.DoublonException;
import javazik.exception.DroitInsuffisantException;
import javazik.exception.ElementIntrouvableException;
import javazik.exception.LimiteEcoutesException;
import javazik.exception.SaisieInvalideException;
import javazik.model.media.Album;
import javazik.model.media.Artiste;
import javazik.model.media.EntiteArtistique;
import javazik.model.media.Genre;
import javazik.model.media.Groupe;
import javazik.model.media.Morceau;
import javazik.model.media.ModeTri;
import javazik.model.playlist.Playlist;
import javazik.model.session.ContexteSession;
import javazik.model.user.Abonne;
import javazik.model.user.Administrateur;
import javazik.model.user.Utilisateur;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service métier principal. Gère catalogue, comptes,
 * playlists, avis et stats. Indépendant de toute vue.
 */
public class PlateformeMusicale implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Catalogue catalogue;
    private final Map<String, Utilisateur> utilisateurs;
    private long ecoutesTotales;

    public PlateformeMusicale() {
        this.catalogue = new Catalogue();
        this.utilisateurs = new LinkedHashMap<>();
        this.ecoutesTotales = 0;
    }

    public Catalogue getCatalogue() { return catalogue; }

    public List<Utilisateur> getUtilisateurs() {
        return new ArrayList<>(utilisateurs.values());
    }

    public long getEcoutesTotales() {
        return catalogue.getMorceaux().stream().mapToLong(Morceau::getNombreEcoutes).sum();
    }

    // ===== Gestion des comptes =====

    public void ajouterUtilisateur(Utilisateur u) throws DoublonException {
        if (utilisateurs.containsKey(u.getIdentifiant()))
            throw new DoublonException("Identifiant déjà pris : " + u.getIdentifiant());
        utilisateurs.put(u.getIdentifiant(), u);
    }

    public Abonne creerCompteAbonne(String identifiant, String mdp, String nomAffichage)
            throws SaisieInvalideException, DoublonException {
        if (identifiant == null || identifiant.isBlank() || mdp == null || mdp.isBlank()
                || nomAffichage == null || nomAffichage.isBlank()) {
            throw new SaisieInvalideException("Tous les champs sont obligatoires.");
        }
        if (mdp.trim().length() < 4)
            throw new SaisieInvalideException("Le mot de passe doit faire au moins 4 caractères.");
        Abonne ab = new Abonne(identifiant, mdp, nomAffichage);
        ajouterUtilisateur(ab);
        return ab;
    }

    public Utilisateur authentifier(String identifiant, String mdp) throws AuthentificationException {
        Utilisateur u = utilisateurs.get(identifiant);
        if (u == null || !u.verifieMotDePasse(mdp))
            throw new AuthentificationException("Identifiant ou mot de passe incorrect.");
        if (!u.estActif())
            throw new AuthentificationException("Ce compte est suspendu.");
        return u;
    }

    public Administrateur authentifierAdmin(String id, String mdp) throws AuthentificationException {
        Utilisateur u = authentifier(id, mdp);
        if (!(u instanceof Administrateur admin))
            throw new AuthentificationException("Ce compte n'est pas administrateur.");
        return admin;
    }

    public Abonne authentifierAbonne(String id, String mdp) throws AuthentificationException {
        Utilisateur u = authentifier(id, mdp);
        if (!(u instanceof Abonne ab))
            throw new AuthentificationException("Ce compte n'est pas abonné.");
        return ab;
    }

    // ===== Recherche =====

    public List<Morceau> rechercherMorceaux(String q) { return catalogue.rechercherMorceaux(q); }
    public List<Album> rechercherAlbums(String q) { return catalogue.rechercherAlbums(q); }
    public List<EntiteArtistique> rechercherEntites(String q) { return catalogue.rechercherEntites(q); }

    public List<Morceau> rechercheAvancee(String q, Genre genre,
                                          Integer anneeMin, Integer anneeMax, ModeTri tri) {
        return catalogue.rechercheAvancee(q, genre, anneeMin, anneeMax, tri);
    }

    // ===== Écoute =====

    public void ecouterMorceau(ContexteSession session, Morceau morceau)
            throws LimiteEcoutesException, DroitInsuffisantException {
        if (session == null || session.getType() == null)
            throw new DroitInsuffisantException("Aucune session active.");
        if (morceau == null)
            throw new DroitInsuffisantException("Morceau introuvable.");

        if (session.estVisiteur()) {
            if (!session.consommerEcouteVisiteur())
                throw new LimiteEcoutesException("Limite de 5 écoutes visiteur atteinte.");
        } else if (session.estAbonne()) {
            session.getAbonne().enregistrerEcoute(morceau);
        } else if (!session.estAdmin()) {
            throw new DroitInsuffisantException("Aucune session active.");
        }
        morceau.incrementerEcoutes();
        ecoutesTotales++;
    }

    // ===== Avis =====

    public void noterMorceau(Abonne ab, Morceau m, int note, String comm) throws SaisieInvalideException {
        if (m == null) throw new SaisieInvalideException("Morceau introuvable.");
        if (note < 1 || note > 5) throw new SaisieInvalideException("La note doit être entre 1 et 5.");
        m.ajouterOuMettreAJourAvis(ab.getIdentifiant(), note, comm);
    }

    public void supprimerAvis(Abonne ab, Morceau m) throws SaisieInvalideException {
        if (m == null) throw new SaisieInvalideException("Morceau introuvable.");
        m.supprimerAvis(ab.getIdentifiant());
    }

    // ===== Recommandations =====

    public List<Morceau> recommanderPour(Abonne ab, int limite) {
        Genre genrePref = ab.getGenrePrefere();
        String artistePref = ab.getInterpretePrefere();
        return catalogue.getMorceaux().stream()
                .filter(m -> ab.getHistoriqueEcoutes().stream().noneMatch(h -> h.equals(m)))
                .sorted(Comparator
                        .comparing((Morceau m) -> genrePref != null && m.getGenre() == genrePref ? 1 : 0).reversed()
                        .thenComparing(m -> artistePref != null && m.getInterprete().getNom().equals(artistePref) ? 1 : 0,
                                Comparator.reverseOrder())
                        .thenComparing(Morceau::getNoteMoyenne, Comparator.reverseOrder())
                        .thenComparing(Morceau::getNombreEcoutes, Comparator.reverseOrder())
                        .thenComparing(Morceau::getTitre))
                .limit(limite).toList();
    }

    // ===== Playlists =====

    public Playlist creerPlaylist(Abonne ab, String nom) throws SaisieInvalideException {
        if (nom == null || nom.isBlank())
            throw new SaisieInvalideException("Le nom de playlist ne peut pas être vide.");
        return ab.creerPlaylist(nom);
    }

    public void renommerPlaylist(Abonne ab, String playlistId, String nouveauNom)
            throws ElementIntrouvableException, SaisieInvalideException {
        if (nouveauNom == null || nouveauNom.isBlank())
            throw new SaisieInvalideException("Le nom ne peut pas être vide.");
        trouverPlaylist(ab, playlistId).renommer(nouveauNom);
    }

    public void supprimerPlaylist(Abonne ab, String playlistId) throws ElementIntrouvableException {
        ab.supprimerPlaylist(trouverPlaylist(ab, playlistId));
    }

    public void ajouterMorceauAPlaylist(Abonne ab, String playlistId, Morceau m)
            throws ElementIntrouvableException, DoublonException, SaisieInvalideException {
        if (m == null) throw new SaisieInvalideException("Morceau introuvable.");
        Playlist p = trouverPlaylist(ab, playlistId);
        if (!p.ajouterMorceau(m))
            throw new DoublonException("Ce morceau est déjà dans la playlist.");
    }

    public int ajouterDepuisAutrePlaylist(Abonne ab, String cibleId, String sourceId)
            throws ElementIntrouvableException {
        Playlist cible = trouverPlaylist(ab, cibleId);
        Playlist source = trouverPlaylist(ab, sourceId);
        return cible.ajouterDepuis(source);
    }

    public void retirerMorceauDePlaylist(Abonne ab, String playlistId, Morceau m)
            throws ElementIntrouvableException, SaisieInvalideException {
        if (!trouverPlaylist(ab, playlistId).retirerMorceau(m))
            throw new SaisieInvalideException("Ce morceau n'est pas dans la playlist.");
    }

    private Playlist trouverPlaylist(Abonne ab, String id) throws ElementIntrouvableException {
        Playlist p = ab.getPlaylistParId(id);
        if (p == null) throw new ElementIntrouvableException("Playlist introuvable.");
        return p;
    }

    // ===== Gestion du catalogue (admin) =====

    public Artiste ajouterArtiste(String nom, String pays) throws SaisieInvalideException, DoublonException {
        if (nom == null || nom.isBlank()) throw new SaisieInvalideException("Nom d'artiste obligatoire.");
        if (catalogue.getArtistes().stream().anyMatch(a -> a.getNom().equalsIgnoreCase(nom.trim())))
            throw new DoublonException("Cet artiste existe déjà.");
        Artiste a = new Artiste(nom, pays);
        catalogue.ajouterEntite(a);
        return a;
    }

    public Groupe ajouterGroupe(String nom, String pays) throws SaisieInvalideException, DoublonException {
        if (nom == null || nom.isBlank()) throw new SaisieInvalideException("Nom de groupe obligatoire.");
        if (catalogue.getGroupes().stream().anyMatch(g -> g.getNom().equalsIgnoreCase(nom.trim())))
            throw new DoublonException("Ce groupe existe déjà.");
        Groupe g = new Groupe(nom, pays);
        catalogue.ajouterEntite(g);
        return g;
    }

    public void lierArtisteAuGroupe(Groupe g, Artiste a) throws SaisieInvalideException {
        if (g == null || a == null) throw new SaisieInvalideException("Artiste ou groupe introuvable.");
        g.ajouterMembre(a);
    }

    public Album ajouterAlbum(String titre, int annee, EntiteArtistique artiste)
            throws SaisieInvalideException, DoublonException {
        if (titre == null || titre.isBlank()) throw new SaisieInvalideException("Titre d'album obligatoire.");
        if (annee < 1900 || annee > 2026) throw new SaisieInvalideException("Année invalide.");
        if (artiste == null) throw new SaisieInvalideException("Interprète obligatoire.");
        boolean existe = catalogue.getAlbums().stream()
                .anyMatch(a -> a.getTitre().equalsIgnoreCase(titre.trim()) && a.getArtiste().equals(artiste));
        if (existe) throw new DoublonException("Cet album existe déjà pour cet interprète.");
        Album alb = new Album(titre, annee, artiste);
        catalogue.ajouterAlbum(alb);
        return alb;
    }

    public Morceau ajouterMorceau(String titre, int duree, Genre genre, int annee,
                                  EntiteArtistique artiste, List<Album> albumsAssocies)
            throws SaisieInvalideException, DoublonException {
        if (titre == null || titre.isBlank()) throw new SaisieInvalideException("Titre obligatoire.");
        if (duree <= 0) throw new SaisieInvalideException("Durée invalide.");
        if (annee < 1900 || annee > 2026) throw new SaisieInvalideException("Année invalide.");
        if (artiste == null) throw new SaisieInvalideException("Interprète obligatoire.");
        boolean existe = catalogue.getMorceaux().stream()
                .anyMatch(m -> m.getTitre().equalsIgnoreCase(titre.trim()) && m.getInterprete().equals(artiste));
        if (existe) throw new DoublonException("Ce morceau existe déjà pour cet interprète.");
        Morceau m = new Morceau(titre, duree, genre, annee, artiste);
        catalogue.ajouterMorceau(m);
        if (albumsAssocies != null) {
            for (Album alb : albumsAssocies) alb.ajouterMorceau(m);
        }
        return m;
    }

    public void supprimerMorceau(String morceauId) throws ElementIntrouvableException {
        Morceau m = catalogue.supprimerMorceau(morceauId);
        if (m == null) throw new ElementIntrouvableException("Morceau introuvable.");
        for (Album alb : new ArrayList<>(m.getAlbums())) alb.retirerMorceau(m);
        for (Utilisateur u : utilisateurs.values()) {
            if (u instanceof Abonne ab) ab.retirerMorceauPartout(m);
        }
    }

    public void supprimerAlbum(String albumId) throws ElementIntrouvableException {
        Album alb = catalogue.supprimerAlbum(albumId);
        if (alb == null) throw new ElementIntrouvableException("Album introuvable.");
        for (Morceau m : new ArrayList<>(alb.getMorceaux())) alb.retirerMorceau(m);
    }

    public void supprimerEntite(String entiteId) throws ElementIntrouvableException {
        EntiteArtistique e = catalogue.supprimerEntite(entiteId);
        if (e == null) throw new ElementIntrouvableException("Artiste / groupe introuvable.");
        // on supprime aussi ses albums et morceaux
        for (Album alb : catalogue.getAlbums().stream().filter(a -> a.getArtiste().equals(e)).toList())
            supprimerAlbum(alb.getId());
        for (Morceau m : catalogue.getMorceaux().stream().filter(m -> m.getInterprete().equals(e)).toList())
            supprimerMorceau(m.getId());
    }

    // ===== Gestion des comptes (admin) =====

    public void suspendreCompte(String identifiant) throws ElementIntrouvableException, SaisieInvalideException {
        Utilisateur u = utilisateurs.get(identifiant);
        if (u == null) throw new ElementIntrouvableException("Utilisateur introuvable.");
        if (!(u instanceof Abonne)) throw new SaisieInvalideException("Seuls les abonnés peuvent être suspendus.");
        u.suspendre();
    }

    public void reactiverCompte(String identifiant) throws ElementIntrouvableException, SaisieInvalideException {
        Utilisateur u = utilisateurs.get(identifiant);
        if (u == null) throw new ElementIntrouvableException("Utilisateur introuvable.");
        if (!(u instanceof Abonne)) throw new SaisieInvalideException("Seuls les abonnés peuvent être réactivés.");
        u.reactiver();
    }

    public void supprimerCompte(String identifiant) throws ElementIntrouvableException, SaisieInvalideException {
        Utilisateur u = utilisateurs.get(identifiant);
        if (u == null) throw new ElementIntrouvableException("Utilisateur introuvable.");
        if (!(u instanceof Abonne)) throw new SaisieInvalideException("Seuls les abonnés peuvent être supprimés.");
        utilisateurs.remove(identifiant);
    }

    // ===== Stats =====

    public StatistiquesSnapshot getStatistiques() {
        int admins = (int) utilisateurs.values().stream().filter(Administrateur.class::isInstance).count();
        int abonnes = (int) utilisateurs.values().stream().filter(Abonne.class::isInstance).count();
        return new StatistiquesSnapshot(
                utilisateurs.size(), abonnes, admins,
                catalogue.getMorceaux().size(), catalogue.getAlbums().size(),
                catalogue.getEntitesArtistiques().size(), getEcoutesTotales(),
                catalogue.getTopEcoutes(5), catalogue.getTopNotes(5),
                catalogue.getTopAjoutsPlaylists(5), catalogue.getEcoutesParGenre());
    }

    public String getDetailsNavigation(Morceau m) {
        return catalogue.naviguerDepuisMorceau(m);
    }
}
