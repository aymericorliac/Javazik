# Scénarios d'interactions entre objets

## Scénario 1 – Écoute d'un morceau par un visiteur
1. La vue demande au contrôleur d'écouter un morceau.
2. Le contrôleur transmet la demande au modèle `PlateformeMusicale`.
3. `PlateformeMusicale` interroge `SessionContext`.
4. Si le quota visiteur n'est pas épuisé, `SessionContext` décrémente le compteur.
5. Le `Morceau` incrémente son nombre d'écoutes.
6. Le contrôleur renvoie le résultat à la vue qui affiche la progression simulée.

## Scénario 2 – Ajout d'un morceau à une playlist
1. L'abonné sélectionne une `Playlist` et un `Morceau`.
2. La vue appelle `AppController.ajouterMorceauAPlaylist(...)`.
3. Le contrôleur vérifie la session abonné.
4. `PlateformeMusicale` recherche la playlist cible.
5. `Playlist.ajouterMorceau(...)` ajoute le morceau s'il n'est pas déjà présent.
6. Le `Morceau` incrémente son compteur d'ajouts en playlist.

## Scénario 3 – Suspension d'un abonné par l'administrateur
1. L'administrateur choisit un identifiant.
2. La vue appelle `AppController.suspendreAbonne(...)`.
3. Le contrôleur vérifie le rôle administrateur.
4. `PlateformeMusicale` récupère l'objet `Utilisateur`.
5. Si c'est bien un `Abonne`, le statut passe à `SUSPENDU`.

## Scénario d'exception – Tentative d'ajout d'un doublon dans une playlist
1. L'abonné tente d'ajouter un morceau déjà présent.
2. `Playlist.ajouterMorceau(...)` refuse l'ajout.
3. `PlateformeMusicale` lève une `DuplicateItemException`.
4. Le contrôleur transmet le message à la vue.
5. La vue affiche une erreur, sans crash.
