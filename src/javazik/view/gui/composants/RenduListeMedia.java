package javazik.view.gui.composants;

import javazik.model.media.Album;
import javazik.model.media.EntiteArtistique;
import javazik.model.media.Morceau;
import javazik.model.playlist.Playlist;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.border.Border;
import java.awt.Component;

/** Rendu uniforme pour les JList de l'interface graphique. */
public class RenduListeMedia extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        String texte;
        if (value instanceof Morceau m) {
            texte = m.getTitre() + "  •  " + m.getInterprete().getNom() + "  •  " + m.getGenre();
        } else if (value instanceof Album a) {
            texte = a.getTitre() + " (" + a.getAnnee() + ")  •  " + a.getArtiste().getNom();
        } else if (value instanceof EntiteArtistique e) {
            texte = e.getTypeLabel() + "  •  " + e.getNom() + "  •  " + e.getPays();
        } else if (value instanceof Playlist p) {
            texte = p.getNom() + "  •  " + p.getMorceaux().size() + " morceau(x)";
        } else {
            texte = String.valueOf(value);
        }
        setText(texte);
        Border marge = BorderFactory.createEmptyBorder(5, 8, 5, 8);
        Border sep = BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeRetro.BORDURE);
        setBorder(BorderFactory.createCompoundBorder(sep, marge));
        setBackground(isSelected ? ThemeRetro.SELECTION : ThemeRetro.CARTE);
        setForeground(ThemeRetro.TEXTE);
        return this;
    }
}
