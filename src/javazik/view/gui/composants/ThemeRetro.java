package javazik.view.gui.composants;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Font;

/**
 * Palette rétro inspirée de l'iPod Touch / iPod Classic :
 * tons gris, chrome, noir, avec des accents discrets.
 */
public final class ThemeRetro {
    // fond général
    public static final Color FOND = new Color(225, 228, 232);
    // cartes / panneaux
    public static final Color CARTE = new Color(242, 243, 245);
    // bleu acier — bouton principal (actions importantes)
    public static final Color BOUTON_PRIMAIRE = new Color(55, 90, 130);
    // gris moyen — bouton secondaire (navigation, accents)
    public static final Color BOUTON_ACCENT = new Color(105, 110, 118);
    // bleu clair — actions "positives" (ajouter, créer, lire)
    public static final Color BOUTON_SUCCES = new Color(70, 115, 165);
    // gris foncé — actions d'alerte (suspendre, renommer)
    public static final Color BOUTON_ALERTE = new Color(90, 95, 100);
    // gris très foncé — suppressions
    public static final Color BOUTON_DANGER = new Color(65, 68, 75);
    // texte principal
    public static final Color TEXTE = new Color(25, 28, 32);
    // texte secondaire
    public static final Color TEXTE_SECONDAIRE = new Color(85, 90, 100);
    // sélection dans les listes — bleu très pâle
    public static final Color SELECTION = new Color(200, 215, 235);
    // bordures
    public static final Color BORDURE = new Color(175, 180, 188);

    public static final Font TITRE = new Font("SansSerif", Font.BOLD, 26);
    public static final Font SOUS_TITRE = new Font("SansSerif", Font.BOLD, 15);
    public static final Font CORPS = new Font("SansSerif", Font.PLAIN, 13);

    private ThemeRetro() {}

    public static Border bordureCarte(String titre) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(BORDURE, 1, true), titre),
                BorderFactory.createEmptyBorder(6, 6, 6, 6));
    }

    public static JLabel creerTitre(String texte) {
        JLabel l = new JLabel(texte);
        l.setFont(TITRE);
        l.setForeground(TEXTE);
        return l;
    }

    public static void appliquerStyleCarte(JComponent c) {
        c.setBackground(CARTE);
        c.setForeground(TEXTE);
        c.setFont(CORPS);
    }
}
