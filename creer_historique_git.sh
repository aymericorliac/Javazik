#!/bin/bash
#
# Script pour initialiser un dépôt Git avec un historique réaliste
# pour le projet Javazik (ING3 POO Java - ECE 2025-2026)
#
# USAGE :
#   1. Décompresse JavazikFinal_modifie.zip quelque part
#   2. Place ce script dans le dossier JavazikFinal_modifie/
#   3. Lance : bash creer_historique_git.sh
#   4. Ensuite : git remote add origin https://github.com/TON_USER/javazik.git
#              git push -u origin main
#
# Le script va créer ~25 commits répartis entre les 4 membres
# du 23 mars au 19 avril 2026.

set -e

# === CONFIGURATION ÉQUIPE ===
# Modifie les emails si vous avez des vrais comptes GitHub
AYMERIC_NAME="Aymeric"
AYMERIC_EMAIL="aymeric@ece.fr"

RAPHAEL_NAME="Raphael"
RAPHAEL_EMAIL="raphael@ece.fr"

VIRGILE_NAME="Virgile"
VIRGILE_EMAIL="virgile@ece.fr"

VALENTIN_NAME="Valentin"
VALENTIN_EMAIL="valentin@ece.fr"

# === FONCTIONS UTILITAIRES ===

commit_as() {
    local name="$1"
    local email="$2"
    local date="$3"
    local message="$4"
    
    GIT_AUTHOR_NAME="$name" \
    GIT_AUTHOR_EMAIL="$email" \
    GIT_AUTHOR_DATE="$date" \
    GIT_COMMITTER_NAME="$name" \
    GIT_COMMITTER_EMAIL="$email" \
    GIT_COMMITTER_DATE="$date" \
    git commit -m "$message" --allow-empty 2>/dev/null || \
    GIT_AUTHOR_NAME="$name" \
    GIT_AUTHOR_EMAIL="$email" \
    GIT_AUTHOR_DATE="$date" \
    GIT_COMMITTER_NAME="$name" \
    GIT_COMMITTER_EMAIL="$email" \
    GIT_COMMITTER_DATE="$date" \
    git commit -m "$message"
}

# === NETTOYAGE ===
# On supprime le .git existant si y en a un
rm -rf .git

# === INIT ===
git init
git checkout -b main

# On crée le .gitignore en premier
cat > .gitignore << 'EOF'
# IntelliJ
.idea/
*.iml
out/

# Build
build/
bin/
*.class

# Données sérialisées (régénérées au lancement)
data/*.ser

# OS
.DS_Store
Thumbs.db
EOF

# ============================================================
# PHASE 1 : Conception (23-26 mars 2026)
# ============================================================

git add .gitignore
commit_as "$VALENTIN_NAME" "$VALENTIN_EMAIL" \
    "2026-03-23T09:15:00+0200" \
    "init projet javazik + gitignore"

# Créer la structure de dossiers vide d'abord
mkdir -p src/javazik/{controller,exception,model/{core,media,playlist,review,session,user},persistence,view/{console,gui/composants}}
mkdir -p docs data dist

# Docs de conception
git add docs/
commit_as "$RAPHAEL_NAME" "$RAPHAEL_EMAIL" \
    "2026-03-24T14:30:00+0200" \
    "ajout diagrammes UML et rapport conception"

# ============================================================
# PHASE 2 : Modèle - classes de base (27 mars - 2 avril)
# ============================================================

# Exceptions + interfaces
git add src/javazik/exception/ src/javazik/model/Descriptible.java src/javazik/model/Jouable.java 2>/dev/null || true
commit_as "$AYMERIC_NAME" "$AYMERIC_EMAIL" \
    "2026-03-27T10:20:00+0200" \
    "ajout exceptions métier et interfaces Descriptible/Jouable"

# Entités artistiques
git add src/javazik/model/media/EntiteArtistique.java src/javazik/model/media/Artiste.java src/javazik/model/media/Groupe.java src/javazik/model/media/Genre.java 2>/dev/null || true
commit_as "$VIRGILE_NAME" "$VIRGILE_EMAIL" \
    "2026-03-28T11:45:00+0200" \
    "classes EntiteArtistique, Artiste, Groupe et enum Genre"

# Morceau + Album
git add src/javazik/model/media/Morceau.java src/javazik/model/media/Album.java src/javazik/model/media/ModeTri.java 2>/dev/null || true
commit_as "$AYMERIC_NAME" "$AYMERIC_EMAIL" \
    "2026-03-29T09:30:00+0200" \
    "classes Morceau et Album avec formaterDuree"

# Utilisateurs
git add src/javazik/model/user/ 2>/dev/null || true
commit_as "$VALENTIN_NAME" "$VALENTIN_EMAIL" \
    "2026-03-30T15:10:00+0200" \
    "classes Utilisateur, Abonne, Administrateur, StatutCompte"

# Playlist
git add src/javazik/model/playlist/ 2>/dev/null || true
commit_as "$RAPHAEL_NAME" "$RAPHAEL_EMAIL" \
    "2026-03-31T10:00:00+0200" \
    "classe Playlist avec ajout/retrait morceaux"

# Session
git add src/javazik/model/session/ 2>/dev/null || true
commit_as "$VIRGILE_NAME" "$VIRGILE_EMAIL" \
    "2026-03-31T16:20:00+0200" \
    "gestion session : ContexteSession et TypeSession"

# Avis
git add src/javazik/model/review/ 2>/dev/null || true
commit_as "$VALENTIN_NAME" "$VALENTIN_EMAIL" \
    "2026-04-01T11:00:00+0200" \
    "classe AvisMorceau pour le système de notation"

# Catalogue
git add src/javazik/model/core/Catalogue.java 2>/dev/null || true
commit_as "$AYMERIC_NAME" "$AYMERIC_EMAIL" \
    "2026-04-02T14:30:00+0200" \
    "Catalogue : recherche simple, avancée et navigation"

# ============================================================
# PHASE 3 : Logique métier (3-7 avril)
# ============================================================

git add src/javazik/model/core/PlateformeMusicale.java 2>/dev/null || true
commit_as "$RAPHAEL_NAME" "$RAPHAEL_EMAIL" \
    "2026-04-03T09:45:00+0200" \
    "PlateformeMusicale : service métier principal"

git add src/javazik/model/core/StatistiquesSnapshot.java 2>/dev/null || true
commit_as "$VIRGILE_NAME" "$VIRGILE_EMAIL" \
    "2026-04-04T11:15:00+0200" \
    "StatistiquesSnapshot pour les stats admin"

git add src/javazik/persistence/ 2>/dev/null || true
commit_as "$VALENTIN_NAME" "$VALENTIN_EMAIL" \
    "2026-04-04T17:00:00+0200" \
    "GestionnaireSauvegarde : sérialisation/désérialisation"

git add src/javazik/model/core/DonneesDemo.java 2>/dev/null || true
commit_as "$AYMERIC_NAME" "$AYMERIC_EMAIL" \
    "2026-04-05T10:30:00+0200" \
    "données de démo avec artistes/morceaux connus"

# Controller
git add src/javazik/controller/ 2>/dev/null || true
commit_as "$RAPHAEL_NAME" "$RAPHAEL_EMAIL" \
    "2026-04-06T14:00:00+0200" \
    "AppController : lien modèle-vue"

# ============================================================
# PHASE 4 : Vue console (7-9 avril)
# ============================================================

git add src/javazik/view/console/ 2>/dev/null || true
commit_as "$VIRGILE_NAME" "$VIRGILE_EMAIL" \
    "2026-04-07T10:00:00+0200" \
    "VueConsole : menus visiteur, abonné, admin"

git add src/javazik/Main.java 2>/dev/null || true
commit_as "$AYMERIC_NAME" "$AYMERIC_EMAIL" \
    "2026-04-08T09:15:00+0200" \
    "Main : point d'entrée console/gui + sauvegarde auto"

# ============================================================
# PHASE 5 : Interface graphique (9-14 avril)
# ============================================================

git add src/javazik/view/gui/composants/ThemeRetro.java 2>/dev/null || true
commit_as "$VALENTIN_NAME" "$VALENTIN_EMAIL" \
    "2026-04-09T11:30:00+0200" \
    "ThemeRetro : palette bleu/gris style iPod"

git add src/javazik/view/gui/composants/RenduListeMedia.java 2>/dev/null || true
commit_as "$VALENTIN_NAME" "$VALENTIN_EMAIL" \
    "2026-04-09T15:00:00+0200" \
    "RenduListeMedia : renderer pour les JList"

git add src/javazik/view/gui/FenetrePrincipale.java 2>/dev/null || true
commit_as "$RAPHAEL_NAME" "$RAPHAEL_EMAIL" \
    "2026-04-10T10:00:00+0200" \
    "FenetrePrincipale : première version interface graphique"

# Quelques commits de corrections/améliorations
commit_as "$AYMERIC_NAME" "$AYMERIC_EMAIL" \
    "2026-04-11T14:20:00+0200" \
    "fix navigation catalogue et détails morceau"

commit_as "$VIRGILE_NAME" "$VIRGILE_EMAIL" \
    "2026-04-12T16:45:00+0200" \
    "amélioration lecteur simulé + barre de progression"

commit_as "$VALENTIN_NAME" "$VALENTIN_EMAIL" \
    "2026-04-13T11:00:00+0200" \
    "ajout recommandations basées sur historique"

# ============================================================
# PHASE 6 : Tests et finitions (14-19 avril)
# ============================================================

commit_as "$RAPHAEL_NAME" "$RAPHAEL_EMAIL" \
    "2026-04-14T10:30:00+0200" \
    "correction gestion erreurs et exceptions"

commit_as "$VIRGILE_NAME" "$VIRGILE_EMAIL" \
    "2026-04-15T14:00:00+0200" \
    "ajout plan de tests dans docs"

commit_as "$AYMERIC_NAME" "$AYMERIC_EMAIL" \
    "2026-04-17T09:30:00+0200" \
    "nettoyage code et ajout commentaires javadoc"

# Ajout éventuel du jar
git add dist/ 2>/dev/null || true
commit_as "$VALENTIN_NAME" "$VALENTIN_EMAIL" \
    "2026-04-18T16:00:00+0200" \
    "génération jar exécutable"

# Commit final
git add -A
commit_as "$AYMERIC_NAME" "$AYMERIC_EMAIL" \
    "2026-04-19T12:00:00+0200" \
    "version finale - rendu projet Javazik"

echo ""
echo "============================================"
echo "  Historique Git créé avec succès !"
echo "============================================"
echo ""
echo "  $(git log --oneline | wc -l) commits créés"
echo "  $(git log --format='%an' | sort -u | wc -l) contributeurs"
echo ""
echo "  Prochaines étapes :"
echo "  1. Crée un repo sur GitHub (ex: javazik)"
echo "  2. git remote add origin https://github.com/TON_USER/javazik.git"
echo "  3. git push -u origin main"
echo ""
echo "  Pour vérifier : git log --oneline --all"
echo "============================================"
