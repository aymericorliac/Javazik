# Conception générale

## Objectif
Javazik est une application locale de gestion d'un catalogue musical et de playlists inspirée des plateformes de streaming. Le projet respecte les contraintes suivantes :
- Java standard uniquement ;
- architecture MVC stricte ;
- version console complète ;
- version graphique Swing ;
- persistance par sérialisation.

## Acteurs
- **Visiteur** : consulte le catalogue et écoute un nombre limité de morceaux.
- **Abonné** : dispose en plus de playlists, d'un historique, de recommandations et d'avis.
- **Administrateur** : gère le catalogue, les comptes abonnés et les statistiques.

## Principes de conception
- **Model** : stockage des données et règles métier.
- **View** : affichage console et Swing.
- **Controller** : médiation unique entre les vues et le modèle.
- **Exceptions métier** : erreurs de validation, de sécurité, de doublon, etc.
- **Collections** : `ArrayList`, `LinkedHashMap`, `EnumMap`.
- **POO** : encapsulation, héritage, classes abstraites, interfaces.

## Extensions retenues
- Notes et avis sur les morceaux.
- Recommandations basées sur l'historique.
- Recherche avancée et statistiques évoluées.
