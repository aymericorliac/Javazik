package javazik.model.user;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/** Classe mère pour les abonnés et les admins. */
public abstract class Utilisateur implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String identifiant;
    private String motDePasse;
    private String nomAffichage;
    private StatutCompte statut;

    protected Utilisateur(String identifiant, String motDePasse, String nomAffichage) {
        this.id = UUID.randomUUID().toString();
        this.identifiant = Objects.requireNonNull(identifiant).trim();
        this.motDePasse = Objects.requireNonNull(motDePasse);
        this.nomAffichage = Objects.requireNonNull(nomAffichage).trim();
        this.statut = StatutCompte.ACTIF;
    }

    public String getId() { return id; }
    public String getIdentifiant() { return identifiant; }
    public String getNomAffichage() { return nomAffichage; }

    public void setNomAffichage(String nom) {
        this.nomAffichage = Objects.requireNonNull(nom).trim();
    }

    public boolean verifieMotDePasse(String mdp) { return this.motDePasse.equals(mdp); }
    public void changerMotDePasse(String nouveau) { this.motDePasse = Objects.requireNonNull(nouveau); }

    public StatutCompte getStatut() { return statut; }
    public boolean estActif() { return statut == StatutCompte.ACTIF; }
    public void suspendre() { this.statut = StatutCompte.SUSPENDU; }
    public void reactiver() { this.statut = StatutCompte.ACTIF; }

    public abstract String getRole();

    @Override
    public String toString() {
        return getRole() + " - " + identifiant + " (" + nomAffichage + ", " + statut + ")";
    }
}
