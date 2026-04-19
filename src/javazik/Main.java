package javazik;

import javazik.controller.AppController;
import javazik.model.core.DonneesDemo;
import javazik.model.core.PlateformeMusicale;
import javazik.persistence.GestionnaireSauvegarde;
import javazik.view.console.VueConsole;
import javazik.view.gui.FenetrePrincipale;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.io.IOException;

/**
 * Point d'entrée de Javazik.
 * Lance le mode console ou GUI selon l'argument passé.
 */
public final class Main {
    private static final String CHEMIN_DONNEES = "data/javazik_donnees.ser";

    private Main() { }

    public static void main(String[] args) {
        PlateformeMusicale plateforme = GestionnaireSauvegarde.chargerOuCreer(CHEMIN_DONNEES);
        DonneesDemo.remplirSiVide(plateforme);
        AppController controleur = new AppController(plateforme, CHEMIN_DONNEES);

        // sauvegarde auto à la fermeture
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                controleur.sauvegarder();
            } catch (IOException e) {
                System.err.println("Sauvegarde auto impossible : " + e.getMessage());
            }
        }));

        String mode = args.length == 0 ? "gui" : args[0].trim().toLowerCase();
        if ("console".equals(mode)) {
            new VueConsole(controleur).lancer();
        } else {
            configurerLookAndFeel();
            SwingUtilities.invokeLater(() -> new FenetrePrincipale(controleur).setVisible(true));
        }
    }

    private static void configurerLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        } catch (Exception e) {
            // on garde le L&F par défaut si Nimbus pas dispo
        }
    }
}
