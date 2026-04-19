package javazik.view.console;

import javazik.controller.AppController;
import javazik.exception.AuthentificationException;
import javazik.exception.JavazikException;
import javazik.model.core.StatistiquesSnapshot;
import javazik.model.media.Album;
import javazik.model.media.Artiste;
import javazik.model.media.EntiteArtistique;
import javazik.model.media.Genre;
import javazik.model.media.Groupe;
import javazik.model.media.Morceau;
import javazik.model.media.ModeTri;
import javazik.model.playlist.Playlist;
import javazik.model.user.Utilisateur;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/** Vue console complète de Javazik. */
public class VueConsole {
    private final AppController ctrl;
    private final Scanner sc;

    public VueConsole(AppController ctrl) {
        this.ctrl = ctrl;
        this.sc = new Scanner(System.in);
    }

    public void lancer() {
        boolean continuer = true;
        while (continuer) {
            afficherBanniere();
            afficherSession();
            System.out.println("1. Se connecter en tant qu'administrateur");
            System.out.println("2. Se connecter en tant qu'abonné");
            System.out.println("3. Créer un compte abonné");
            System.out.println("4. Continuer en tant que visiteur");
            System.out.println("5. Sauvegarder");
            System.out.println("6. Quitter");
            int choix = lireEntier("Votre choix : ");
            try {
                switch (choix) {
                    case 1 -> { connecterAdmin(); menuAdmin(); }
                    case 2 -> { connecterAbonne(); menuAbonne(); }
                    case 3 -> creerCompte();
                    case 4 -> { ctrl.continuerCommeVisiteur(); menuVisiteur(); }
                    case 5 -> { ctrl.sauvegarder(); System.out.println("Sauvegarde ok."); pause(); }
                    case 6 -> { ctrl.sauvegarder(); continuer = false; }
                    default -> erreur("Choix invalide.");
                }
            } catch (AuthentificationException e) {
                erreur(e.getMessage()); pause();
            } catch (IOException e) {
                erreur("Erreur sauvegarde : " + e.getMessage()); continuer = false;
            }
        }
    }

    // ===== menus =====

    private void menuVisiteur() {
        boolean retour = false;
        while (!retour) {
            section("ESPACE VISITEUR"); afficherSession();
            System.out.println("1. Parcourir le catalogue complet");
            System.out.println("2. Rechercher dans le catalogue");
            System.out.println("3. Recherche avancée (genre, année, tri)");
            System.out.println("4. Explorer un album");
            System.out.println("5. Explorer un artiste ou un groupe");
            System.out.println("6. Écouter un morceau");
            System.out.println("7. Voir les détails/navigation d'un morceau");
            System.out.println("8. Retour au menu principal");
            switch (lireEntier("Votre choix : ")) {
                case 1 -> afficherCatalogue();
                case 2 -> rechercher();
                case 3 -> rechercheAvancee();
                case 4 -> explorerAlbum();
                case 5 -> explorerEntite();
                case 6 -> ecouter();
                case 7 -> detailsMorceau();
                case 8 -> retour = true;
                default -> erreur("Choix invalide.");
            }
            if (!retour) pause();
        }
        ctrl.deconnecter();
    }

    private void menuAbonne() {
        boolean retour = false;
        while (!retour) {
            section("ESPACE ABONNÉ"); afficherSession();
            System.out.println("1. Parcourir le catalogue complet");
            System.out.println("2. Rechercher dans le catalogue");
            System.out.println("3. Recherche avancée (genre, année, tri)");
            System.out.println("4. Explorer un album");
            System.out.println("5. Explorer un artiste ou un groupe");
            System.out.println("6. Écouter un morceau");
            System.out.println("7. Voir les détails/navigation d'un morceau");
            System.out.println("8. Gérer mes playlists");
            System.out.println("9. Voir mon historique d'écoute");
            System.out.println("10. Noter / commenter un morceau");
            System.out.println("11. Supprimer mon avis sur un morceau");
            System.out.println("12. Voir mes recommandations");
            System.out.println("13. Sauvegarder");
            System.out.println("14. Retour au menu principal");
            switch (lireEntier("Votre choix : ")) {
                case 1 -> afficherCatalogue();
                case 2 -> rechercher();
                case 3 -> rechercheAvancee();
                case 4 -> explorerAlbum();
                case 5 -> explorerEntite();
                case 6 -> ecouter();
                case 7 -> detailsMorceau();
                case 8 -> menuPlaylists();
                case 9 -> { section("HISTORIQUE"); listerElements(ctrl.getHistoriqueCourant()); }
                case 10 -> noterMorceau();
                case 11 -> supprimerAvis();
                case 12 -> { section("RECOMMANDATIONS"); listerElements(ctrl.getRecommandationsCourantes()); }
                case 13 -> sauvegarderQuiet();
                case 14 -> retour = true;
                default -> erreur("Choix invalide.");
            }
            if (!retour) pause();
        }
        ctrl.deconnecter();
    }

    private void menuAdmin() {
        boolean retour = false;
        while (!retour) {
            section("ESPACE ADMINISTRATEUR"); afficherSession();
            System.out.println("1. Parcourir le catalogue");
            System.out.println("2. Rechercher");
            System.out.println("3. Recherche avancée");
            System.out.println("4. Ajouter un artiste");
            System.out.println("5. Ajouter un groupe");
            System.out.println("6. Lier un artiste à un groupe");
            System.out.println("7. Ajouter un album");
            System.out.println("8. Ajouter un morceau");
            System.out.println("9. Supprimer un morceau");
            System.out.println("10. Supprimer un album");
            System.out.println("11. Supprimer un artiste / groupe");
            System.out.println("12. Suspendre un abonné");
            System.out.println("13. Réactiver un abonné");
            System.out.println("14. Supprimer un abonné");
            System.out.println("15. Lister les comptes");
            System.out.println("16. Statistiques");
            System.out.println("17. Sauvegarder");
            System.out.println("18. Retour");
            try {
                switch (lireEntier("Votre choix : ")) {
                    case 1 -> afficherCatalogue();
                    case 2 -> rechercher();
                    case 3 -> rechercheAvancee();
                    case 4 -> { ctrl.ajouterArtiste(lireTexte("Nom : "), lireTexte("Pays : ")); System.out.println("Artiste ajouté."); }
                    case 5 -> { ctrl.ajouterGroupe(lireTexte("Nom : "), lireTexte("Pays : ")); System.out.println("Groupe ajouté."); }
                    case 6 -> lierArtisteGroupe();
                    case 7 -> ajouterAlbum();
                    case 8 -> ajouterMorceau();
                    case 9 -> { Morceau m = choisirMorceau(); if (m != null) { ctrl.supprimerMorceau(m.getId()); System.out.println("Supprimé."); } }
                    case 10 -> { Album a = choisirAlbum(); if (a != null) { ctrl.supprimerAlbum(a.getId()); System.out.println("Supprimé."); } }
                    case 11 -> { EntiteArtistique e = choisirEntite(); if (e != null) { ctrl.supprimerEntite(e.getId()); System.out.println("Supprimé."); } }
                    case 12 -> ctrl.suspendreAbonne(lireTexte("Identifiant à suspendre : "));
                    case 13 -> ctrl.reactiverAbonne(lireTexte("Identifiant à réactiver : "));
                    case 14 -> ctrl.supprimerAbonne(lireTexte("Identifiant à supprimer : "));
                    case 15 -> listerElements(ctrl.getUtilisateurs());
                    case 16 -> { section("STATISTIQUES"); System.out.println(ctrl.getStatistiques()); }
                    case 17 -> sauvegarderQuiet();
                    case 18 -> retour = true;
                    default -> erreur("Choix invalide.");
                }
            } catch (JavazikException e) { erreur(e.getMessage()); }
            if (!retour) pause();
        }
        ctrl.deconnecter();
    }

    private void menuPlaylists() {
        boolean retour = false;
        while (!retour) {
            section("GESTION DES PLAYLISTS");
            listerElements(ctrl.getPlaylistsCourantes());
            System.out.println("1. Créer une playlist");
            System.out.println("2. Renommer");
            System.out.println("3. Supprimer");
            System.out.println("4. Afficher contenu");
            System.out.println("5. Ajouter un morceau");
            System.out.println("6. Retirer un morceau");
            System.out.println("7. Copier depuis une autre playlist");
            System.out.println("8. Retour");
            try {
                switch (lireEntier("Votre choix : ")) {
                    case 1 -> ctrl.creerPlaylist(lireTexte("Nom : "));
                    case 2 -> { Playlist p = choisirPlaylist(); if (p != null) ctrl.renommerPlaylist(p.getId(), lireTexte("Nouveau nom : ")); }
                    case 3 -> { Playlist p = choisirPlaylist(); if (p != null) ctrl.supprimerPlaylist(p.getId()); }
                    case 4 -> { Playlist p = choisirPlaylist(); if (p != null) System.out.println(p.descriptionDetaillee()); }
                    case 5 -> ajouterMorceauPlaylist();
                    case 6 -> retirerMorceauPlaylist();
                    case 7 -> copierPlaylist();
                    case 8 -> retour = true;
                    default -> erreur("Choix invalide.");
                }
            } catch (JavazikException e) { erreur(e.getMessage()); }
            if (!retour) pause();
        }
    }

    // ===== fonctionnalités =====

    private void afficherCatalogue() {
        section("CATALOGUE"); System.out.println("MORCEAUX");
        listerElements(ctrl.getCatalogue().getMorceaux());
        System.out.println("\nALBUMS"); listerElements(ctrl.getCatalogue().getAlbums());
        System.out.println("\nARTISTES / GROUPES"); listerElements(ctrl.getCatalogue().getEntitesArtistiques());
    }

    private void rechercher() {
        section("RECHERCHE"); String q = lireTexte("Texte : ");
        System.out.println("Morceaux :"); listerElements(ctrl.rechercherMorceaux(q));
        System.out.println("Albums :"); listerElements(ctrl.rechercherAlbums(q));
        System.out.println("Artistes/groupes :"); listerElements(ctrl.rechercherEntites(q));
    }

    private void rechercheAvancee() {
        section("RECHERCHE AVANCÉE");
        String q = lireTexte("Texte : ");
        Genre g = lireGenreOptionnel();
        Integer min = lireEntierOptionnel("Année min (vide si aucune) : ");
        Integer max = lireEntierOptionnel("Année max (vide si aucune) : ");
        ModeTri tri = lireTriOptionnel();
        listerElements(ctrl.rechercheAvancee(q, g, min, max, tri));
    }

    private void explorerAlbum() {
        section("EXPLORATION D'ALBUM");
        Album a = choisirAlbum();
        if (a == null) return;
        System.out.println(a.descriptionDetaillee());
        System.out.println("\nMorceaux :"); listerElements(ctrl.getMorceauxParAlbum(a));
    }

    private void explorerEntite() {
        section("EXPLORATION D'ARTISTE / GROUPE");
        EntiteArtistique e = choisirEntite();
        if (e == null) return;
        System.out.println(e.descriptionDetaillee());
        System.out.println("\nAlbums :"); listerElements(ctrl.getAlbumsParEntite(e));
        System.out.println("Morceaux :"); listerElements(ctrl.getMorceauxParEntite(e));
    }

    private void ecouter() {
        try {
            Morceau m = choisirMorceau();
            if (m == null) return;
            ctrl.ecouterMorceau(m);
            System.out.println("Lecture simulée : " + m.getLabelLecture());
            // petite barre de progression
            for (int i = 0; i <= 20; i++) {
                try { Thread.sleep(55); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                String barre = "#".repeat(i) + "-".repeat(20 - i);
                System.out.print("\r[" + barre + "] " + (i * 100 / 20) + "%");
            }
            System.out.println();
        } catch (JavazikException e) { erreur(e.getMessage()); }
    }

    private void detailsMorceau() {
        section("DÉTAILS / NAVIGATION");
        Morceau m = choisirMorceau();
        if (m != null) System.out.println(ctrl.detailsNavigation(m));
    }

    private void noterMorceau() {
        section("NOTER UN MORCEAU");
        try {
            Morceau m = choisirMorceau();
            if (m != null) {
                int note = lireEntier("Note de 1 à 5 : ");
                String comm = lireTexte("Commentaire : ");
                ctrl.noterMorceau(m, note, comm);
                System.out.println("Avis enregistré.");
            }
        } catch (JavazikException e) { erreur(e.getMessage()); }
    }

    private void supprimerAvis() {
        section("SUPPRIMER MON AVIS");
        try {
            Morceau m = choisirMorceau();
            if (m != null) { ctrl.supprimerAvis(m); System.out.println("Avis supprimé."); }
        } catch (JavazikException e) { erreur(e.getMessage()); }
    }

    private void ajouterMorceauPlaylist() throws JavazikException {
        Playlist p = choisirPlaylist();
        if (p == null) return;
        System.out.println("1. Depuis le catalogue");
        System.out.println("2. Depuis une autre playlist");
        int choix = lireEntier("Source : ");
        Morceau m = null;
        if (choix == 1) m = choisirMorceau();
        else if (choix == 2) { Playlist src = choisirPlaylist(); if (src != null) m = choisirDansListe(src.getMorceaux(), "Index : "); }
        if (m != null) { ctrl.ajouterMorceauAPlaylist(p.getId(), m); System.out.println("Ajouté."); }
    }

    private void retirerMorceauPlaylist() throws JavazikException {
        Playlist p = choisirPlaylist(); if (p == null) return;
        Morceau m = choisirDansListe(p.getMorceaux(), "Index du morceau : ");
        if (m != null) { ctrl.retirerMorceauDePlaylist(p.getId(), m); System.out.println("Retiré."); }
    }

    private void copierPlaylist() throws JavazikException {
        System.out.println("Playlist cible :"); Playlist c = choisirPlaylist();
        System.out.println("Playlist source :"); Playlist s = choisirPlaylist();
        if (c != null && s != null) System.out.println(ctrl.ajouterDepuisAutrePlaylist(c.getId(), s.getId()) + " morceau(x) ajouté(s).");
    }

    private void lierArtisteGroupe() throws JavazikException {
        Artiste a = choisirArtiste(); Groupe g = choisirGroupe();
        if (a != null && g != null) { ctrl.lierArtisteAuGroupe(g, a); System.out.println("Liaison créée."); }
    }

    private void ajouterAlbum() throws JavazikException {
        EntiteArtistique e = choisirEntite();
        if (e != null) { ctrl.ajouterAlbum(lireTexte("Titre : "), lireEntier("Année : "), e); System.out.println("Album ajouté."); }
    }

    private void ajouterMorceau() throws JavazikException {
        EntiteArtistique e = choisirEntite(); if (e == null) return;
        String titre = lireTexte("Titre : ");
        int duree = lireEntier("Durée en secondes : ");
        int annee = lireEntier("Année : ");
        Genre g = lireGenreObligatoire();
        List<Album> albums = choisirPlusieursAlbums();
        ctrl.ajouterMorceau(titre, duree, g, annee, e, albums);
        System.out.println("Morceau ajouté.");
    }

    // ===== sélection =====

    private Morceau choisirMorceau() { return choisirDansListe(ctrl.getCatalogue().getMorceaux(), "Index du morceau : "); }
    private Morceau choisirDansListe(List<Morceau> liste, String msg) {
        listerElements(liste); if (liste.isEmpty()) return null;
        int i = lireEntier(msg); return i >= 0 && i < liste.size() ? liste.get(i) : null;
    }
    private Playlist choisirPlaylist() {
        List<Playlist> l = ctrl.getPlaylistsCourantes(); listerElements(l);
        if (l.isEmpty()) return null; int i = lireEntier("Index : "); return i >= 0 && i < l.size() ? l.get(i) : null;
    }
    private Album choisirAlbum() {
        List<Album> l = ctrl.getCatalogue().getAlbums(); listerElements(l);
        if (l.isEmpty()) return null; int i = lireEntier("Index : "); return i >= 0 && i < l.size() ? l.get(i) : null;
    }
    private EntiteArtistique choisirEntite() {
        List<EntiteArtistique> l = ctrl.getCatalogue().getEntitesArtistiques(); listerElements(l);
        if (l.isEmpty()) return null; int i = lireEntier("Index : "); return i >= 0 && i < l.size() ? l.get(i) : null;
    }
    private Artiste choisirArtiste() {
        List<Artiste> l = ctrl.getCatalogue().getArtistes(); listerElements(l);
        if (l.isEmpty()) return null; int i = lireEntier("Index : "); return i >= 0 && i < l.size() ? l.get(i) : null;
    }
    private Groupe choisirGroupe() {
        List<Groupe> l = ctrl.getCatalogue().getGroupes(); listerElements(l);
        if (l.isEmpty()) return null; int i = lireEntier("Index : "); return i >= 0 && i < l.size() ? l.get(i) : null;
    }

    private List<Album> choisirPlusieursAlbums() {
        List<Album> l = ctrl.getCatalogue().getAlbums();
        if (l.isEmpty()) return List.of();
        listerElements(l);
        String txt = lireTexte("Index d'albums séparés par des virgules (vide si aucun) : ");
        if (txt.isBlank()) return List.of();
        List<Album> sel = new ArrayList<>();
        for (String s : txt.split(",")) {
            try { int idx = Integer.parseInt(s.trim()); if (idx >= 0 && idx < l.size()) sel.add(l.get(idx)); }
            catch (NumberFormatException ignored) {}
        }
        return sel;
    }

    // ===== saisie genre/tri =====

    private Genre lireGenreObligatoire() {
        while (true) {
            try { return Genre.valueOf(lireTexte("Genre " + Arrays.toString(Genre.values()) + " : ").trim().toUpperCase()); }
            catch (IllegalArgumentException e) { erreur("Genre invalide."); }
        }
    }
    private Genre lireGenreOptionnel() {
        while (true) {
            String t = lireTexte("Genre (vide ou " + Arrays.toString(Genre.values()) + ") : ");
            if (t.isBlank()) return null;
            try { return Genre.valueOf(t.trim().toUpperCase()); }
            catch (IllegalArgumentException e) { erreur("Genre invalide."); }
        }
    }
    private ModeTri lireTriOptionnel() {
        while (true) {
            String t = lireTexte("Tri (vide ou " + Arrays.toString(ModeTri.values()) + ") : ");
            if (t.isBlank()) return ModeTri.TITRE;
            try { return ModeTri.valueOf(t.trim().toUpperCase()); }
            catch (IllegalArgumentException e) { erreur("Tri invalide."); }
        }
    }
    private Integer lireEntierOptionnel(String msg) {
        while (true) {
            String t = lireTexte(msg); if (t.isBlank()) return null;
            try { return Integer.parseInt(t); }
            catch (NumberFormatException e) { erreur("Entier attendu ou laisser vide."); }
        }
    }

    // ===== utilitaires =====

    private void connecterAdmin() throws AuthentificationException { ctrl.connecterAdmin(lireTexte("Identifiant admin : "), lireTexte("Mot de passe : ")); }
    private void connecterAbonne() throws AuthentificationException { ctrl.connecterAbonne(lireTexte("Identifiant : "), lireTexte("Mot de passe : ")); }
    private void creerCompte() {
        try { ctrl.creerCompte(lireTexte("Identifiant : "), lireTexte("Mot de passe : "), lireTexte("Nom affiché : ")); System.out.println("Compte créé."); }
        catch (JavazikException e) { erreur(e.getMessage()); }
        pause();
    }
    private void sauvegarderQuiet() { try { ctrl.sauvegarder(); System.out.println("Sauvegarde ok."); } catch (IOException e) { erreur(e.getMessage()); } }

    private void listerElements(List<?> liste) {
        if (liste == null || liste.isEmpty()) { System.out.println("(aucun résultat)"); return; }
        for (int i = 0; i < liste.size(); i++) System.out.println("[" + i + "] " + liste.get(i));
    }
    private void afficherBanniere() {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║                  JAVAZIK - CONSOLE                  ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }
    private void section(String titre) { System.out.println("\n━━━ " + titre + " ━━━"); }
    private void afficherSession() { System.out.println("Session : " + ctrl.getSession()); }
    private void erreur(String msg) { System.out.println("[ERREUR] " + msg); }
    private String lireTexte(String msg) { System.out.print(msg); return sc.nextLine().trim(); }
    private int lireEntier(String msg) {
        while (true) { try { return Integer.parseInt(lireTexte(msg)); } catch (NumberFormatException e) { erreur("Entier attendu."); } }
    }
    private void pause() { System.out.print("Appuyez sur Entrée..."); sc.nextLine(); }
}
