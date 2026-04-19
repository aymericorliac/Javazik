package javazik.model.session;

import javazik.model.user.Abonne;
import javazik.model.user.Administrateur;
import javazik.model.user.Utilisateur;

/** Gère la session en cours (visiteur, abonné ou admin). */
public class ContexteSession {
    private TypeSession type;
    private Utilisateur utilisateurCourant;
    private int ecoutesRestantes; // uniquement pour le visiteur

    public ContexteSession() {
        fermer();
    }

    public TypeSession getType() { return type; }
    public Utilisateur getUtilisateurCourant() { return utilisateurCourant; }
    public int getEcoutesRestantes() { return ecoutesRestantes; }

    public void demarrerVisiteur() {
        this.type = TypeSession.VISITEUR;
        this.utilisateurCourant = null;
        this.ecoutesRestantes = 5;
    }

    public void connecter(Utilisateur user) {
        this.utilisateurCourant = user;
        if (user instanceof Administrateur) this.type = TypeSession.ADMIN;
        else if (user instanceof Abonne) this.type = TypeSession.ABONNE;
        else this.type = TypeSession.AUCUNE;
        this.ecoutesRestantes = 0;
    }

    public void fermer() {
        this.type = TypeSession.AUCUNE;
        this.utilisateurCourant = null;
        this.ecoutesRestantes = 0;
    }

    /** Consomme une écoute visiteur, retourne false si plus d'écoutes. */
    public boolean consommerEcouteVisiteur() {
        if (type == TypeSession.VISITEUR && ecoutesRestantes > 0) {
            ecoutesRestantes--;
            return true;
        }
        return false;
    }

    public boolean estVisiteur() { return type == TypeSession.VISITEUR; }
    public boolean estAbonne() { return type == TypeSession.ABONNE; }
    public boolean estAdmin() { return type == TypeSession.ADMIN; }

    public Abonne getAbonne() {
        return utilisateurCourant instanceof Abonne a ? a : null;
    }

    public Administrateur getAdmin() {
        return utilisateurCourant instanceof Administrateur a ? a : null;
    }

    @Override
    public String toString() {
        return switch (type) {
            case AUCUNE -> "Aucune session";
            case VISITEUR -> "Visiteur (" + ecoutesRestantes + " écoute(s) restante(s))";
            case ABONNE -> "Abonné - " + utilisateurCourant.getNomAffichage();
            case ADMIN -> "Administrateur - " + utilisateurCourant.getNomAffichage();
        };
    }
}
