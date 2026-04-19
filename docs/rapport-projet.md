# Rapport de projet – JAVAZIK

## 1. Présentation générale
Javazik est une application de gestion d'un catalogue musical et de playlists inspirée des services de streaming. Le projet a été développé en Java avec une architecture MVC stricte et propose deux vues distinctes : une vue console intégrale et une vue graphique Swing.

## 2. Planning et répartition des tâches
Voir `planning.md`.

## 3. Modélisation UML
### 3.1 Diagramme de cas d'utilisation
Voir `usecase.puml`.

### 3.2 Diagramme de classes
Voir `classdiagram.puml`.

### 3.3 Explications
- Le modèle rassemble le catalogue, les utilisateurs, les playlists et les statistiques.
- Les vues n'accèdent jamais directement au modèle : elles passent par `AppController`.
- Le modèle reste totalement indépendant des mécanismes d'affichage.

## 4. Scénarios d'interactions entre objets
Voir `scenarios.md`.

## 5. Maquettes et storyboard
Voir `storyboard.md`.

## 6. Plan de tests
Voir `test-plan.md`.

## 7. Bilans
Voir `bilan.md`.

## 8. Références
Voir `references.md`.
