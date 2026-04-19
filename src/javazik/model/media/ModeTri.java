package javazik.model.media;

/** Modes de tri pour la recherche avancée. */
public enum ModeTri {
    TITRE("Titre"),
    ANNEE("Année"),
    DUREE("Durée"),
    ECOUTES("Nombre d'écoutes"),
    NOTE_MOYENNE("Note moyenne");

    private final String libelle;

    ModeTri(String libelle) { this.libelle = libelle; }

    @Override
    public String toString() { return libelle; }
}
