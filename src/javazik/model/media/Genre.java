package javazik.model.media;

/**
 * Genres musicaux gérés par l'appli.
 */
public enum Genre {
    POP("Pop"),
    ROCK("Rock"),
    ELECTRO("Électro"),
    SOUL("Soul"),
    HIP_HOP("Hip-hop"),
    CLASSIQUE("Classique"),
    JAZZ("Jazz"),
    RNB("R&B"),
    FUNK("Funk"),
    AUTRE("Autre");

    private final String libelle;

    Genre(String libelle) {
        this.libelle = libelle;
    }

    @Override
    public String toString() { return libelle; }
}
