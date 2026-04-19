package javazik.model.core;

import javazik.exception.DoublonException;
import javazik.exception.SaisieInvalideException;
import javazik.model.media.Album;
import javazik.model.media.Artiste;
import javazik.model.media.Genre;
import javazik.model.media.Groupe;
import javazik.model.media.Morceau;
import javazik.model.user.Administrateur;

import java.util.List;

/**
 * Jeu de données de démo avec des artistes/morceaux connus
 * pour rendre la soutenance plus parlante.
 */
public final class DonneesDemo {
    private DonneesDemo() {}

    /** Remplit la plateforme si elle est vide (premier lancement). */
    public static void remplirSiVide(PlateformeMusicale plateforme) {
        if (!plateforme.getCatalogue().getMorceaux().isEmpty()
                || !plateforme.getUtilisateurs().isEmpty()) {
            return; // déjà rempli, on touche à rien
        }
        try {
            chargerDonnees(plateforme);
        } catch (SaisieInvalideException | DoublonException e) {
            throw new IllegalStateException("Erreur init données de démo", e);
        }
    }

    private static void chargerDonnees(PlateformeMusicale p) throws SaisieInvalideException, DoublonException {
        // compte admin
        Administrateur admin = new Administrateur("admin", "admin123", "Admin Javazik");
        p.ajouterUtilisateur(admin);

        // comptes abonnés
        var aymeric = p.creerCompteAbonne("aymeric", "aymeric123", "Aymeric");
        var raphael = p.creerCompteAbonne("raphael", "raphael123", "Raphael");
        var virgile = p.creerCompteAbonne("virgile", "virgile123", "Virgile");

        // --- artistes ---
        Artiste adele = p.ajouterArtiste("Adele", "Royaume-Uni");
        Artiste freddie = p.ajouterArtiste("Freddie Mercury", "Royaume-Uni");
        Artiste chrisMartin = p.ajouterArtiste("Chris Martin", "Royaume-Uni");
        Artiste mj = p.ajouterArtiste("Michael Jackson", "États-Unis");
        Artiste beyonce = p.ajouterArtiste("Beyoncé", "États-Unis");
        Artiste edSheeran = p.ajouterArtiste("Ed Sheeran", "Royaume-Uni");
        Artiste brunoMars = p.ajouterArtiste("Bruno Mars", "États-Unis");
        Artiste ladyGaga = p.ajouterArtiste("Lady Gaga", "États-Unis");
        Artiste thomas = p.ajouterArtiste("Thomas Bangalter", "France");
        Artiste guyManuel = p.ajouterArtiste("Guy-Manuel de Homem-Christo", "France");
        Artiste danReynolds = p.ajouterArtiste("Dan Reynolds", "États-Unis");

        // --- groupes ---
        Groupe queen = p.ajouterGroupe("Queen", "Royaume-Uni");
        p.lierArtisteAuGroupe(queen, freddie);
        Groupe coldplay = p.ajouterGroupe("Coldplay", "Royaume-Uni");
        p.lierArtisteAuGroupe(coldplay, chrisMartin);
        Groupe daftPunk = p.ajouterGroupe("Daft Punk", "France");
        p.lierArtisteAuGroupe(daftPunk, thomas);
        p.lierArtisteAuGroupe(daftPunk, guyManuel);
        Groupe beatles = p.ajouterGroupe("The Beatles", "Royaume-Uni");
        Groupe abba = p.ajouterGroupe("ABBA", "Suède");
        Groupe imagineDragons = p.ajouterGroupe("Imagine Dragons", "États-Unis");
        p.lierArtisteAuGroupe(imagineDragons, danReynolds);

        // --- albums ---
        Album nightAtOpera = p.ajouterAlbum("A Night at the Opera", 1975, queen);
        Album jazz = p.ajouterAlbum("Jazz", 1978, queen);
        Album discovery = p.ajouterAlbum("Discovery", 2001, daftPunk);
        Album ram = p.ajouterAlbum("Random Access Memories", 2013, daftPunk);
        Album album25 = p.ajouterAlbum("25", 2015, adele);
        Album album21 = p.ajouterAlbum("21", 2011, adele);
        Album parachutes = p.ajouterAlbum("Parachutes", 2000, coldplay);
        Album vivaLaVida = p.ajouterAlbum("Viva la Vida or Death and All His Friends", 2008, coldplay);
        Album thriller = p.ajouterAlbum("Thriller", 1982, mj);
        Album dangerous = p.ajouterAlbum("Dangerous", 1991, mj);
        Album lemonade = p.ajouterAlbum("Lemonade", 2016, beyonce);
        Album sashaFierce = p.ajouterAlbum("I Am... Sasha Fierce", 2008, beyonce);
        Album divide = p.ajouterAlbum("÷", 2017, edSheeran);
        Album multiply = p.ajouterAlbum("x", 2014, edSheeran);
        Album dooWops = p.ajouterAlbum("Doo-Wops & Hooligans", 2010, brunoMars);
        Album unorthodox = p.ajouterAlbum("Unorthodox Jukebox", 2012, brunoMars);
        Album fameMonster = p.ajouterAlbum("The Fame Monster", 2009, ladyGaga);
        Album beatles1 = p.ajouterAlbum("1", 2000, beatles);
        Album abbeyRoad = p.ajouterAlbum("Abbey Road", 1969, beatles);
        Album gold = p.ajouterAlbum("Gold", 1992, abba);
        Album evolve = p.ajouterAlbum("Evolve", 2017, imagineDragons);
        Album nightVisions = p.ajouterAlbum("Night Visions", 2012, imagineDragons);

        // --- morceaux ---
        // Queen
        Morceau bohemian = p.ajouterMorceau("Bohemian Rhapsody", 354, Genre.ROCK, 1975, queen, List.of(nightAtOpera));
        Morceau loveOfMyLife = p.ajouterMorceau("Love of My Life", 219, Genre.ROCK, 1975, queen, List.of(nightAtOpera));
        Morceau dontStopMe = p.ajouterMorceau("Don't Stop Me Now", 209, Genre.ROCK, 1978, queen, List.of(jazz));

        // Daft Punk
        Morceau oneMoreTime = p.ajouterMorceau("One More Time", 320, Genre.ELECTRO, 2000, daftPunk, List.of(discovery));
        Morceau harderBetter = p.ajouterMorceau("Harder, Better, Faster, Stronger", 225, Genre.ELECTRO, 2001, daftPunk, List.of(discovery));
        Morceau getLucky = p.ajouterMorceau("Get Lucky", 369, Genre.FUNK, 2013, daftPunk, List.of(ram));
        Morceau instantCrush = p.ajouterMorceau("Instant Crush", 337, Genre.POP, 2013, daftPunk, List.of(ram));

        // Adele
        Morceau hello = p.ajouterMorceau("Hello", 295, Genre.POP, 2015, adele, List.of(album25));
        Morceau sendMyLove = p.ajouterMorceau("Send My Love", 223, Genre.POP, 2015, adele, List.of(album25));
        Morceau rollingDeep = p.ajouterMorceau("Rolling in the Deep", 228, Genre.POP, 2011, adele, List.of(album21));
        Morceau someoneLikeYou = p.ajouterMorceau("Someone Like You", 285, Genre.POP, 2011, adele, List.of(album21));

        // Coldplay
        Morceau yellow = p.ajouterMorceau("Yellow", 269, Genre.ROCK, 2000, coldplay, List.of(parachutes));
        Morceau trouble = p.ajouterMorceau("Trouble", 271, Genre.ROCK, 2000, coldplay, List.of(parachutes));
        Morceau clocks = p.ajouterMorceau("Clocks", 307, Genre.ROCK, 2002, coldplay, List.of());
        Morceau viva = p.ajouterMorceau("Viva la Vida", 242, Genre.ROCK, 2008, coldplay, List.of(vivaLaVida));

        // Michael Jackson
        Morceau billieJean = p.ajouterMorceau("Billie Jean", 294, Genre.POP, 1982, mj, List.of(thriller));
        Morceau beatIt = p.ajouterMorceau("Beat It", 258, Genre.ROCK, 1982, mj, List.of(thriller));
        Morceau thrillerTrack = p.ajouterMorceau("Thriller", 357, Genre.POP, 1982, mj, List.of(thriller));
        Morceau blackOrWhite = p.ajouterMorceau("Black or White", 262, Genre.POP, 1991, mj, List.of(dangerous));

        // Beyoncé
        Morceau halo = p.ajouterMorceau("Halo", 261, Genre.RNB, 2008, beyonce, List.of(sashaFierce));
        Morceau singleLadies = p.ajouterMorceau("Single Ladies", 193, Genre.RNB, 2008, beyonce, List.of(sashaFierce));
        Morceau formation = p.ajouterMorceau("Formation", 206, Genre.RNB, 2016, beyonce, List.of(lemonade));
        Morceau crazyInLove = p.ajouterMorceau("Crazy in Love", 236, Genre.RNB, 2003, beyonce, List.of());

        // Ed Sheeran
        Morceau shapeOfYou = p.ajouterMorceau("Shape of You", 233, Genre.POP, 2017, edSheeran, List.of(divide));
        Morceau perfect = p.ajouterMorceau("Perfect", 263, Genre.POP, 2017, edSheeran, List.of(divide));
        Morceau thinkingOutLoud = p.ajouterMorceau("Thinking Out Loud", 281, Genre.POP, 2014, edSheeran, List.of(multiply));

        // Bruno Mars
        Morceau justTheWay = p.ajouterMorceau("Just the Way You Are", 221, Genre.POP, 2010, brunoMars, List.of(dooWops));
        Morceau lockedOut = p.ajouterMorceau("Locked Out of Heaven", 233, Genre.POP, 2012, brunoMars, List.of(unorthodox));
        Morceau grenade = p.ajouterMorceau("Grenade", 223, Genre.POP, 2010, brunoMars, List.of(dooWops));

        // Lady Gaga
        Morceau badRomance = p.ajouterMorceau("Bad Romance", 294, Genre.POP, 2009, ladyGaga, List.of(fameMonster));
        Morceau pokerFace = p.ajouterMorceau("Poker Face", 238, Genre.POP, 2008, ladyGaga, List.of());

        // Beatles
        Morceau comeTogether = p.ajouterMorceau("Come Together", 260, Genre.ROCK, 1969, beatles, List.of(abbeyRoad, beatles1));
        Morceau hereComesSun = p.ajouterMorceau("Here Comes the Sun", 185, Genre.ROCK, 1969, beatles, List.of(abbeyRoad));
        Morceau letItBe = p.ajouterMorceau("Let It Be", 243, Genre.ROCK, 1970, beatles, List.of(beatles1));
        Morceau heyJude = p.ajouterMorceau("Hey Jude", 431, Genre.ROCK, 1968, beatles, List.of(beatles1));

        // ABBA
        Morceau dancingQueen = p.ajouterMorceau("Dancing Queen", 231, Genre.POP, 1976, abba, List.of(gold));
        Morceau mammaMia = p.ajouterMorceau("Mamma Mia", 215, Genre.POP, 1975, abba, List.of(gold));

        // Imagine Dragons
        Morceau believer = p.ajouterMorceau("Believer", 204, Genre.ROCK, 2017, imagineDragons, List.of(evolve));
        Morceau thunder = p.ajouterMorceau("Thunder", 187, Genre.ROCK, 2017, imagineDragons, List.of(evolve));
        Morceau radioactive = p.ajouterMorceau("Radioactive", 186, Genre.ROCK, 2012, imagineDragons, List.of(nightVisions));
        Morceau demons = p.ajouterMorceau("Demons", 177, Genre.ROCK, 2012, imagineDragons, List.of(nightVisions));

        // simuler quelques écoutes pour les stats
        simulerEcoutes(bohemian, 18);
        simulerEcoutes(oneMoreTime, 15);
        simulerEcoutes(billieJean, 14);
        simulerEcoutes(hello, 12);
        simulerEcoutes(yellow, 11);
        simulerEcoutes(getLucky, 10);
        simulerEcoutes(shapeOfYou, 10);
        simulerEcoutes(viva, 9);
        simulerEcoutes(halo, 9);
        simulerEcoutes(badRomance, 8);
        simulerEcoutes(dancingQueen, 8);
        simulerEcoutes(believer, 8);
        simulerEcoutes(comeTogether, 7);

        // quelques avis et playlists pour la démo
        try {
            p.noterMorceau(p.authentifierAbonne("aymeric", "aymeric123"), bohemian, 5, "Un classique absolu.");
            p.noterMorceau(p.authentifierAbonne("aymeric", "aymeric123"), oneMoreTime, 5, "Parfait pour la démo.");
            p.noterMorceau(p.authentifierAbonne("aymeric", "aymeric123"), billieJean, 5, "Rythme légendaire.");
            p.noterMorceau(p.authentifierAbonne("raphael", "raphael123"), hello, 5, "Interprétation incroyable.");
            p.noterMorceau(p.authentifierAbonne("raphael", "raphael123"), yellow, 4, "Toujours aussi beau.");
            p.noterMorceau(p.authentifierAbonne("raphael", "raphael123"), halo, 5, "Très puissante.");
            p.noterMorceau(p.authentifierAbonne("virgile", "virgile123"), shapeOfYou, 4, "Très efficace.");
            p.noterMorceau(p.authentifierAbonne("virgile", "virgile123"), dancingQueen, 5, "Inratable en soirée.");

            var pl1 = p.creerPlaylist(aymeric, "Soirée Pop-Rock");
            p.ajouterMorceauAPlaylist(aymeric, pl1.getId(), bohemian);
            p.ajouterMorceauAPlaylist(aymeric, pl1.getId(), billieJean);
            p.ajouterMorceauAPlaylist(aymeric, pl1.getId(), dontStopMe);
            p.ajouterMorceauAPlaylist(aymeric, pl1.getId(), shapeOfYou);

            var pl2 = p.creerPlaylist(raphael, "Chill & émotion");
            p.ajouterMorceauAPlaylist(raphael, pl2.getId(), hello);
            p.ajouterMorceauAPlaylist(raphael, pl2.getId(), someoneLikeYou);
            p.ajouterMorceauAPlaylist(raphael, pl2.getId(), yellow);
            p.ajouterMorceauAPlaylist(raphael, pl2.getId(), halo);

            var pl3 = p.creerPlaylist(virgile, "Énergie & classiques");
            p.ajouterMorceauAPlaylist(virgile, pl3.getId(), oneMoreTime);
            p.ajouterMorceauAPlaylist(virgile, pl3.getId(), getLucky);
            p.ajouterMorceauAPlaylist(virgile, pl3.getId(), believer);
            p.ajouterMorceauAPlaylist(virgile, pl3.getId(), dancingQueen);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static void simulerEcoutes(Morceau m, int nb) {
        for (int i = 0; i < nb; i++) m.incrementerEcoutes();
    }
}
