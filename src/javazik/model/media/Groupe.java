package javazik.model.media;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Groupe extends EntiteArtistique {
    private static final long serialVersionUID = 1L;

    private final List<Artiste> membres;

    public Groupe(String nom, String pays) {
        super(nom, pays);
        this.membres = new ArrayList<>();
    }

    @Override
    public String getTypeLabel() { return "Groupe"; }

    public void ajouterMembre(Artiste artiste) {
        if (artiste != null && !membres.contains(artiste)) {
            membres.add(artiste);
            artiste.ajouterGroupe(this);
        }
    }

    public List<Artiste> getMembres() {
        return Collections.unmodifiableList(membres);
    }

    @Override
    public String descriptionDetaillee() {
        StringBuilder sb = new StringBuilder(super.descriptionDetaillee());
        if (!membres.isEmpty()) {
            sb.append(System.lineSeparator()).append("Membres : ");
            for (int i = 0; i < membres.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(membres.get(i).getNom());
            }
        }
        return sb.toString();
    }
}
