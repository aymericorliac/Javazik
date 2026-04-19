package javazik.controller;

import javazik.exception.AuthentificationException;
import javazik.exception.DroitInsuffisantException;
import javazik.exception.JavazikException;
import javazik.model.core.Catalogue;
import javazik.model.core.PlateformeMusicale;
import javazik.model.core.StatistiquesSnapshot;
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
import javazik.persistence.GestionnaireSauvegarde;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/** Contrôleur principal, fait le lien entre le modèle et les vues. */
public class AppController {
    private final PlateformeMusicale modele;
    private final ContexteSession session;
    private final String cheminSauvegarde;

    public AppController(PlateformeMusicale modele, String cheminSauvegarde) {
        this.modele = modele;
        this.cheminSauvegarde = cheminSauvegarde;
        this.session = new ContexteSession();
        this.session.demarrerVisiteur();
    }

    public PlateformeMusicale getModele() { return modele; }
    public ContexteSession getSession() { return session; }
    public Catalogue getCatalogue() { return modele.getCatalogue(); }

    // --- connexion ---
    public void continuerCommeVisiteur() { session.demarrerVisiteur(); }

    public void connecterAbonne(String id, String mdp) throws AuthentificationException {
        session.connecter(modele.authentifierAbonne(id, mdp));
    }

    public void connecterAdmin(String id, String mdp) throws AuthentificationException {
        session.connecter(modele.authentifierAdmin(id, mdp));
    }

    public Abonne creerCompte(String id, String mdp, String nom) throws JavazikException {
        return modele.creerCompteAbonne(id, mdp, nom);
    }

    public void deconnecter() { session.fermer(); }

    // --- recherche ---
    public List<Morceau> rechercherMorceaux(String q) { return modele.rechercherMorceaux(q); }
    public List<Album> rechercherAlbums(String q) { return modele.rechercherAlbums(q); }
    public List<EntiteArtistique> rechercherEntites(String q) { return modele.rechercherEntites(q); }

    public List<Morceau> rechercheAvancee(String q, Genre genre,
            Integer anneeMin, Integer anneeMax, ModeTri tri) {
        return modele.rechercheAvancee(q, genre, anneeMin, anneeMax, tri);
    }

    public List<Morceau> getMorceauxParEntite(EntiteArtistique e) {
        return modele.getCatalogue().getMorceauxParEntite(e);
    }
    public List<Album> getAlbumsParEntite(EntiteArtistique e) {
        return modele.getCatalogue().getAlbumsParEntite(e);
    }
    public List<Morceau> getMorceauxParAlbum(Album a) {
        return modele.getCatalogue().getMorceauxParAlbum(a);
    }

    // --- écoute ---
    public void ecouterMorceau(Morceau m) throws JavazikException {
        modele.ecouterMorceau(session, m);
    }

    public String detailsNavigation(Morceau m) { return modele.getDetailsNavigation(m); }

    // --- session courante ---
    public Abonne getAbonneCourant() { return session.getAbonne(); }
    public Administrateur getAdminCourant() { return session.getAdmin(); }

    public List<Playlist> getPlaylistsCourantes() {
        return getAbonneCourant() == null ? Collections.emptyList() : getAbonneCourant().getPlaylists();
    }

    public List<Morceau> getHistoriqueCourant() {
        return getAbonneCourant() == null ? Collections.emptyList() : getAbonneCourant().getHistoriqueEcoutes();
    }

    public List<Morceau> getRecommandationsCourantes() {
        return getAbonneCourant() == null ? Collections.emptyList()
                : modele.recommanderPour(getAbonneCourant(), 8);
    }

    // --- avis ---
    public void noterMorceau(Morceau m, int note, String comm) throws JavazikException {
        Abonne ab = getAbonneCourant();
        if (ab == null) throw new DroitInsuffisantException("Réservé aux abonnés.");
        modele.noterMorceau(ab, m, note, comm);
    }

    public void supprimerAvis(Morceau m) throws JavazikException {
        Abonne ab = getAbonneCourant();
        if (ab == null) throw new DroitInsuffisantException("Réservé aux abonnés.");
        modele.supprimerAvis(ab, m);
    }

    // --- playlists ---
    public Playlist creerPlaylist(String nom) throws JavazikException {
        Abonne ab = getAbonneCourant();
        if (ab == null) throw new DroitInsuffisantException("Réservé aux abonnés.");
        return modele.creerPlaylist(ab, nom);
    }

    public void renommerPlaylist(String playlistId, String nouveauNom) throws JavazikException {
        Abonne ab = getAbonneCourant();
        if (ab == null) throw new DroitInsuffisantException("Réservé aux abonnés.");
        modele.renommerPlaylist(ab, playlistId, nouveauNom);
    }

    public void supprimerPlaylist(String playlistId) throws JavazikException {
        Abonne ab = getAbonneCourant();
        if (ab == null) throw new DroitInsuffisantException("Réservé aux abonnés.");
        modele.supprimerPlaylist(ab, playlistId);
    }

    public void ajouterMorceauAPlaylist(String playlistId, Morceau m) throws JavazikException {
        Abonne ab = getAbonneCourant();
        if (ab == null) throw new DroitInsuffisantException("Réservé aux abonnés.");
        modele.ajouterMorceauAPlaylist(ab, playlistId, m);
    }

    public int ajouterDepuisAutrePlaylist(String cibleId, String sourceId) throws JavazikException {
        Abonne ab = getAbonneCourant();
        if (ab == null) throw new DroitInsuffisantException("Réservé aux abonnés.");
        return modele.ajouterDepuisAutrePlaylist(ab, cibleId, sourceId);
    }

    public void retirerMorceauDePlaylist(String playlistId, Morceau m) throws JavazikException {
        Abonne ab = getAbonneCourant();
        if (ab == null) throw new DroitInsuffisantException("Réservé aux abonnés.");
        modele.retirerMorceauDePlaylist(ab, playlistId, m);
    }

    // --- admin catalogue ---
    public Artiste ajouterArtiste(String nom, String pays) throws JavazikException {
        verifierAdmin(); return modele.ajouterArtiste(nom, pays);
    }
    public Groupe ajouterGroupe(String nom, String pays) throws JavazikException {
        verifierAdmin(); return modele.ajouterGroupe(nom, pays);
    }
    public void lierArtisteAuGroupe(Groupe g, Artiste a) throws JavazikException {
        verifierAdmin(); modele.lierArtisteAuGroupe(g, a);
    }
    public Album ajouterAlbum(String titre, int annee, EntiteArtistique e) throws JavazikException {
        verifierAdmin(); return modele.ajouterAlbum(titre, annee, e);
    }
    public Morceau ajouterMorceau(String titre, int duree, Genre genre, int annee,
                                  EntiteArtistique artiste, List<Album> albums) throws JavazikException {
        verifierAdmin(); return modele.ajouterMorceau(titre, duree, genre, annee, artiste, albums);
    }
    public void supprimerMorceau(String id) throws JavazikException { verifierAdmin(); modele.supprimerMorceau(id); }
    public void supprimerAlbum(String id) throws JavazikException { verifierAdmin(); modele.supprimerAlbum(id); }
    public void supprimerEntite(String id) throws JavazikException { verifierAdmin(); modele.supprimerEntite(id); }

    // --- admin comptes ---
    public void suspendreAbonne(String id) throws JavazikException { verifierAdmin(); modele.suspendreCompte(id); }
    public void reactiverAbonne(String id) throws JavazikException { verifierAdmin(); modele.reactiverCompte(id); }
    public void supprimerAbonne(String id) throws JavazikException { verifierAdmin(); modele.supprimerCompte(id); }

    public StatistiquesSnapshot getStatistiques() throws DroitInsuffisantException {
        verifierAdmin(); return modele.getStatistiques();
    }

    public List<Utilisateur> getUtilisateurs() throws DroitInsuffisantException {
        verifierAdmin(); return modele.getUtilisateurs();
    }

    private void verifierAdmin() throws DroitInsuffisantException {
        if (getAdminCourant() == null) throw new DroitInsuffisantException("Action réservée aux administrateurs.");
    }

    // --- sauvegarde ---
    public void sauvegarder() throws IOException {
        GestionnaireSauvegarde.sauvegarder(modele, cheminSauvegarde);
    }

    // --- lookups ---
    public Playlist getPlaylistById(String id) {
        return getAbonneCourant() == null ? null : getAbonneCourant().getPlaylistParId(id);
    }
    public Morceau getMorceauById(String id) { return modele.getCatalogue().getMorceauParId(id); }
    public Album getAlbumById(String id) { return modele.getCatalogue().getAlbumParId(id); }
    public EntiteArtistique getEntiteById(String id) { return modele.getCatalogue().getEntiteParId(id); }
}
