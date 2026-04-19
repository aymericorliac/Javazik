package javazik.persistence;

import javazik.model.core.PlateformeMusicale;

import java.io.*;

/** Sauvegarde et chargement du modèle par sérialisation. */
public final class GestionnaireSauvegarde {
    private GestionnaireSauvegarde() {}

    public static PlateformeMusicale chargerOuCreer(String chemin) {
        File fichier = new File(chemin);
        if (!fichier.exists()) return new PlateformeMusicale();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fichier))) {
            Object obj = ois.readObject();
            if (obj instanceof PlateformeMusicale pm) return pm;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Chargement impossible, on repart de zéro : " + e.getMessage());
        }
        return new PlateformeMusicale();
    }

    public static void sauvegarder(PlateformeMusicale plateforme, String chemin) throws IOException {
        File fichier = new File(chemin);
        File dossier = fichier.getParentFile();
        if (dossier != null && !dossier.exists()) dossier.mkdirs();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fichier))) {
            oos.writeObject(plateforme);
        }
    }
}
