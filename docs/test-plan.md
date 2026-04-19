# Plan de tests

| Fonctionnalité | Scénario | Données d'entrée | Résultat attendu |
|---|---|---|---|
| Connexion admin | principal | admin / admin123 | connexion acceptée |
| Connexion abonné | principal | alice / alice123 | connexion acceptée |
| Connexion refusée | exception | alice / fauxmdp | message d'erreur |
| Visiteur limité | exception | 6 écoutes successives | blocage à la 6e écoute |
| Création playlist | principal | nom valide | playlist créée |
| Ajout doublon playlist | exception | même morceau deux fois | erreur sans crash |
| Ajout morceau admin | principal | données valides | morceau ajouté au catalogue |
| Suppression morceau absent | exception | identifiant invalide | message d'erreur |
| Suspension abonné | principal | bob | compte suspendu |
| Persistance | principal | sauvegarde puis relance | données restaurées |
| Note / avis | principal | note 5, commentaire | avis visible dans le détail |
| Recherche avancée | principal | genre=ROCK, tri=ECOUTES | liste filtrée et triée |
| Statistiques | principal | session admin | indicateurs affichés |
