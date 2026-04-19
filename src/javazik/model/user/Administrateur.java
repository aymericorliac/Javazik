package javazik.model.user;

public class Administrateur extends Utilisateur {
    private static final long serialVersionUID = 1L;

    public Administrateur(String identifiant, String motDePasse, String nomAffichage) {
        super(identifiant, motDePasse, nomAffichage);
    }

    @Override
    public String getRole() { return "Administrateur"; }
    // pas besoin de méthodes spécifiques pour l'instant
}
