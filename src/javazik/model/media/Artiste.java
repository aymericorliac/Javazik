package javazik.model.media;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Artiste solo, qui peut appartenir à un ou plusieurs groupes. */
public class Artiste extends EntiteArtistique {
    private static final long serialVersionUID = 1L;

    private final List<Groupe> groupes;

    public Artiste(String nom, String pays) {
        super(nom, pays);
        this.groupes = new ArrayList<>();
    }

    @Override
    public String getTypeLabel() { return "Artiste"; }

    public void ajouterGroupe(Groupe g) {
        if (g != null && !groupes.contains(g)) {
            groupes.add(g);
        }
    }

    public List<Groupe> getGroupes() {
        return Collections.unmodifiableList(groupes);
    }

    @Override
    public String descriptionDetaillee() {
        StringBuilder sb = new StringBuilder(super.descriptionDetaillee());
        if (!groupes.isEmpty()) {
            sb.append(System.lineSeparator()).append("Membre de : ");
            for (int i = 0; i < groupes.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(groupes.get(i).getNom());
            }
        }
        return sb.toString();
    }
}
