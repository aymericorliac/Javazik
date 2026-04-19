package javazik.view.gui;

import javazik.controller.AppController;
import javazik.exception.AuthentificationException;
import javazik.exception.JavazikException;
import javazik.model.core.StatistiquesSnapshot;
import javazik.model.media.Album;
import javazik.model.media.Artiste;
import javazik.model.media.EntiteArtistique;
import javazik.model.media.Genre;
import javazik.model.media.Groupe;
import javazik.model.media.Morceau;
import javazik.model.media.ModeTri;
import javazik.model.playlist.Playlist;
import javazik.model.session.ContexteSession;
import javazik.model.user.Utilisateur;
import javazik.view.gui.composants.RenduListeMedia;
import javazik.view.gui.composants.ThemeRetro;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/** 
 * Interface graphique principale de Javazik.
 *
 * <p>Cette version conserve l'esprit de l'ancienne interface (grandes zones catalogue,
 * playlists et administration), tout en améliorant la lisibilité, la séparation des rôles,
 * et l'expérience de lecture simulée.</p>
 */

public class FenetrePrincipale extends JFrame {
    private final AppController controller;

    // Session / header
    private final JLabel sessionLabel = new JLabel();
    private final JLabel subtitleLabel = new JLabel();
    private final CardLayout sessionCards = new CardLayout();
    private final JPanel sessionZonePanel = new JPanel(sessionCards);
    private static final int SIMULATED_PLAYBACK_SECONDS = 20;

    private final JButton accessHubButton = new JButton("Centre d'accès");
    private final JButton saveButton = new JButton("Sauvegarder");
    private final JButton logoutButton = new JButton("Déconnexion");

    // Main tabs
    private final JTabbedPane mainTabs = new JTabbedPane();

    // Catalogue
    private final JTextField searchField = new JTextField(18);
    private final JComboBox<Genre> genreCombo = new JComboBox<>();
    private final JComboBox<ModeTri> sortCombo = new JComboBox<>(ModeTri.values());
    private final JSpinner yearMinSpinner = new JSpinner(new SpinnerNumberModel(1900, 1900, 2026, 1));
    private final JSpinner yearMaxSpinner = new JSpinner(new SpinnerNumberModel(2026, 1900, 2026, 1));
    private final JCheckBox yearFilterCheck = new JCheckBox("Filtrer par année");

    private final DefaultListModel<Morceau> songsModel = new DefaultListModel<>();
    private final DefaultListModel<Album> albumsModel = new DefaultListModel<>();
    private final DefaultListModel<EntiteArtistique> artistsModel = new DefaultListModel<>();
    private final JList<Morceau> songsList = new JList<>(songsModel);
    private final JList<Album> albumsList = new JList<>(albumsModel);
    private final JList<EntiteArtistique> artistsList = new JList<>(artistsModel);
    private final JTextArea detailArea = createTextArea();
    private final JButton addToPlaylistButton = new JButton("Ajouter à une playlist");

    // Playlists / library
    private final DefaultListModel<Playlist> playlistModel = new DefaultListModel<>();
    private final DefaultListModel<Morceau> playlistSongsModel = new DefaultListModel<>();
    private final DefaultListModel<Morceau> historyModel = new DefaultListModel<>();
    private final DefaultListModel<Morceau> recoModel = new DefaultListModel<>();
    private final JList<Playlist> playlistList = new JList<>(playlistModel);
    private final JList<Morceau> playlistSongsList = new JList<>(playlistSongsModel);
    private final JList<Morceau> historyList = new JList<>(historyModel);
    private final JList<Morceau> recoList = new JList<>(recoModel);

    // Admin / stats
    private final JTextArea adminArea = createTextArea();
    private final JTextArea statsArea = createTextArea();

    // Player
    private final JLabel nowPlayingLabel = new JLabel("Aucun morceau en cours");
    private final JLabel queueLabel = new JLabel("Sélectionnez un morceau pour lancer la lecture");
    private final JLabel currentTimeLabel = new JLabel("00:00");
    private final JLabel totalTimeLabel = new JLabel("00:00");
    private final JSlider positionSlider = new JSlider(0, 0, 0);
    private final JButton previousButton = new JButton("⏮");
    private final JButton rewindButton = new JButton("-10s");
    private final JButton playPauseButton = new JButton("▶");
    private final JButton forwardButton = new JButton("+10s");
    private final JButton nextButton = new JButton("⏭");
    private final JButton stopButton = new JButton("⏹");
    private final Timer playbackTimer;
    private boolean sliderInternalUpdate = false;
    private boolean sliderUserEditing = false;
    private List<Morceau> currentQueue = List.of();
    private int currentQueueIndex = -1;
    private int currentPositionSeconds = 0;
    private boolean currentlyPlaying = false;

    public FenetrePrincipale(AppController controller) {
        super("Javazik");
        this.controller = controller;
        this.playbackTimer = new Timer(1000, e -> onPlaybackTick());
        this.playbackTimer.setInitialDelay(1000);

        configureWindow();
        initializeThemeAndComponents();
        buildUi();
        refreshAll();
    }

    private void configureWindow() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1240, 820);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);
    }

    private void initializeThemeAndComponents() {
        setBackground(ThemeRetro.FOND);
        RenduListeMedia renderer = new RenduListeMedia();
        for (JList<?> list : Arrays.asList(songsList, albumsList, artistsList, playlistList, playlistSongsList, historyList, recoList)) {
            list.setCellRenderer(renderer);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setFont(ThemeRetro.CORPS);
        }

        for (Genre genre : Genre.values()) {
            genreCombo.addItem(genre);
        }
        genreCombo.insertItemAt(null, 0);
        genreCombo.setSelectedIndex(0);

        subtitleLabel.setForeground(new Color(80, 80, 80));
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sessionLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        accessHubButton.addActionListener(e -> openAccessHub());
        saveButton.addActionListener(e -> saveNow());
        logoutButton.addActionListener(e -> {
            stopPlayback();
            controller.continuerCommeVisiteur();
            refreshAll();
        });

        songsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Morceau morceau = songsList.getSelectedValue();
                if (morceau != null) {
                    detailArea.setText(morceau.descriptionDetaillee());
                }
            }
        });
        albumsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Album album = albumsList.getSelectedValue();
                if (album != null) {
                    detailArea.setText(album.descriptionDetaillee() + System.lineSeparator() + System.lineSeparator()
                            + "Morceaux :" + System.lineSeparator()
                            + formatSongList(controller.getMorceauxParAlbum(album)));
                }
            }
        });
        artistsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                EntiteArtistique entite = artistsList.getSelectedValue();
                if (entite != null) {
                    detailArea.setText(entite.descriptionDetaillee() + System.lineSeparator() + System.lineSeparator()
                            + "Albums :" + System.lineSeparator()
                            + formatAlbumList(controller.getAlbumsParEntite(entite)) + System.lineSeparator() + System.lineSeparator()
                            + "Morceaux :" + System.lineSeparator()
                            + formatSongList(controller.getMorceauxParEntite(entite)));
                }
            }
        });
        playlistList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                refreshPlaylistSongs();
            }
        });

        installDoubleClickToPlay(songsList, this::queueFromSongsModel);
        installDoubleClickToPlay(playlistSongsList, this::queueFromPlaylistSongsModel);
        installDoubleClickToPlay(historyList, this::queueFromHistoryModel);
        installDoubleClickToPlay(recoList, this::queueFromRecoModel);

        addToPlaylistButton.addActionListener(e -> addSelectedSongToPlaylist());

        previousButton.addActionListener(e -> playPreviousTrack());
        nextButton.addActionListener(e -> playNextTrack());
        rewindButton.addActionListener(e -> shiftPlayback(-10));
        forwardButton.addActionListener(e -> shiftPlayback(10));
        stopButton.addActionListener(e -> stopPlayback());
        playPauseButton.addActionListener(e -> onPlayPauseClicked());

        positionSlider.addChangeListener(this::onSliderChanged);

        adminArea.setText("Zone administrateur : utilisez les boutons ci-dessous pour gérer le catalogue et les comptes.");
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.setBackground(ThemeRetro.FOND);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildMainTabs(), BorderLayout.CENTER);
        root.add(buildPlayerPanel(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ThemeRetro.FOND);

        JPanel titleCard = createRoundedHeroPanel();
        titleCard.setLayout(new BorderLayout(12, 12));
        titleCard.setOpaque(false);
        titleCard.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel sectionLabel = new JLabel("Plateforme musicale");
        sectionLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        sectionLabel.setForeground(new Color(60, 60, 60));
        sectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = ThemeRetro.creerTitre("JAVAZIK");
        title.setForeground(new Color(20, 20, 20));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel hook = new JLabel("Streaming local, démonstration propre, catalogue connu et navigation fluide.");
        hook.setForeground(new Color(50, 50, 50));
        hook.setAlignmentX(Component.LEFT_ALIGNMENT);

        sessionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sessionLabel.setForeground(new Color(30, 30, 30));
        sessionLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitleLabel.setForeground(new Color(70, 70, 70));
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

        textPanel.add(sectionLabel);
        textPanel.add(Box.createVerticalStrut(10));
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(10));
        textPanel.add(hook);
        textPanel.add(Box.createVerticalStrut(12));
        textPanel.add(sessionLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subtitleLabel);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        accessHubButton.setText("Connexion");
        styleButton(accessHubButton, ThemeRetro.BOUTON_PRIMAIRE, Color.WHITE);
        styleButton(saveButton, ThemeRetro.BOUTON_PRIMAIRE, Color.WHITE);
        styleButton(logoutButton, ThemeRetro.BOUTON_ACCENT, Color.WHITE);

        actions.add(accessHubButton);
        actions.add(saveButton);
        actions.add(logoutButton);

        titleCard.add(textPanel, BorderLayout.CENTER);
        titleCard.add(actions, BorderLayout.EAST);

        header.add(titleCard, BorderLayout.CENTER);
        return header;
    }

    private JPanel createRoundedHeroPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 28;
                int w = getWidth();
                int h = getHeight();

                // Dégradé style ancien Swing / iPod
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(210, 210, 215),
                        0, h, new Color(170, 170, 178)
                );

                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);

                // Petite lumière en haut
                g2.setColor(new Color(255, 255, 255, 90));
                g2.fillRoundRect(2, 2, w - 4, h / 3, arc, arc);

                // Bord principal
                g2.setColor(new Color(100, 100, 100));
                g2.setStroke(new BasicStroke(1.3f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

                // Bord intérieur léger
                g2.setColor(new Color(255, 255, 255, 80));
                g2.drawRoundRect(2, 2, w - 5, h - 5, arc - 4, arc - 4);

                g2.dispose();
            }
        };
    }


    private JTabbedPane buildMainTabs() {
        mainTabs.setFont(ThemeRetro.CORPS);
        mainTabs.addTab("Catalogue", buildCatalogTab());
        return mainTabs;
    }

    private JPanel buildCatalogTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(ThemeRetro.FOND);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JPanel filters = createCardPanel("Catalogue musical et navigation");
        filters.setLayout(new BorderLayout(8, 8));

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        topRow.setOpaque(false);
        topRow.add(new JLabel("Recherche :"));
        topRow.add(searchField);
        topRow.add(new JLabel("Genre :"));
        topRow.add(genreCombo);
        topRow.add(new JLabel("Tri :"));
        topRow.add(sortCombo);

        JPanel secondRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        secondRow.setOpaque(false);
        secondRow.add(yearFilterCheck);
        secondRow.add(new JLabel("Année min :"));
        secondRow.add(yearMinSpinner);
        secondRow.add(new JLabel("Année max :"));
        secondRow.add(yearMaxSpinner);
        JButton searchButton = new JButton("Actualiser");
        JButton listenButton = new JButton("Lire la sélection");
        JButton detailsButton = new JButton("Afficher les détails" );
        styleButton(searchButton, ThemeRetro.BOUTON_PRIMAIRE, Color.WHITE);
        styleButton(listenButton, ThemeRetro.BOUTON_PRIMAIRE, Color.WHITE);
        styleButton(detailsButton, ThemeRetro.BOUTON_ACCENT, Color.WHITE);
        styleButton(addToPlaylistButton, ThemeRetro.BOUTON_PRIMAIRE, Color.WHITE);
        searchButton.addActionListener(e -> refreshCatalogue());
        listenButton.addActionListener(e -> playSelectionFromCatalogue());
        detailsButton.addActionListener(e -> showDetailsForSelectedSong());
        secondRow.add(searchButton);
        secondRow.add(listenButton);
        secondRow.add(detailsButton);
        secondRow.add(addToPlaylistButton);

        JPanel rows = new JPanel();
        rows.setOpaque(false);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.add(topRow);
        rows.add(secondRow);
        filters.add(rows, BorderLayout.CENTER);

        JTabbedPane subTabs = new JTabbedPane();
        subTabs.addTab("Morceaux", wrapListInCard(songsList, "Résultats morceaux"));
        subTabs.addTab("Albums", wrapListInCard(albumsList, "Albums"));
        subTabs.addTab("Artistes / groupes", wrapListInCard(artistsList, "Artistes et groupes"));

        JPanel detailCard = createCardPanel("Détails et navigation");
        detailCard.setLayout(new BorderLayout());
        detailCard.add(new JScrollPane(detailArea), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, subTabs, detailCard);
        splitPane.setResizeWeight(0.58);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        panel.add(filters, BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildPlaylistsTab() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 8, 8));
        panel.setBackground(ThemeRetro.FOND);
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JPanel playlistsCard = createCardPanel("Mes playlists");
        playlistsCard.setLayout(new BorderLayout(8, 8));
        playlistsCard.add(new JScrollPane(playlistList), BorderLayout.CENTER);
        JPanel playlistButtons = new JPanel(new GridLayout(0, 1, 4, 4));
        playlistButtons.setOpaque(false);
        playlistButtons.add(makeActionButton("Créer", ThemeRetro.BOUTON_PRIMAIRE, e -> createPlaylist()));
        playlistButtons.add(makeActionButton("Renommer", ThemeRetro.BOUTON_ACCENT, e -> renamePlaylist()));
        playlistButtons.add(makeActionButton("Supprimer", ThemeRetro.BOUTON_ACCENT, e -> deletePlaylist()));
        playlistButtons.add(makeActionButton("Ajouter le morceau du catalogue", ThemeRetro.BOUTON_PRIMAIRE, e -> addSelectedSongToPlaylist()));
        playlistButtons.add(makeActionButton("Retirer le morceau", ThemeRetro.BOUTON_ACCENT, e -> removeSongFromPlaylist()));
        playlistButtons.add(makeActionButton("Copier depuis une autre playlist", ThemeRetro.BOUTON_ACCENT, e -> copyFromOtherPlaylist()));
        playlistsCard.add(playlistButtons, BorderLayout.SOUTH);

        JPanel contentCard = createCardPanel("Contenu, lecture et avis");
        contentCard.setLayout(new BorderLayout(8, 8));
        contentCard.add(new JScrollPane(playlistSongsList), BorderLayout.CENTER);
        JPanel contentButtons = new JPanel(new GridLayout(0, 1, 4, 4));
        contentButtons.setOpaque(false);
        contentButtons.add(makeActionButton("Lire la sélection", ThemeRetro.BOUTON_PRIMAIRE, e -> playSelectionFromPlaylist()));
        contentButtons.add(makeActionButton("Noter / commenter", ThemeRetro.BOUTON_PRIMAIRE, e -> reviewSelectedSong()));
        contentButtons.add(makeActionButton("Supprimer mon avis", ThemeRetro.BOUTON_ACCENT, e -> deleteReviewSelectedSong()));
        contentCard.add(contentButtons, BorderLayout.SOUTH);

        JPanel right = new JPanel(new GridLayout(2, 1, 8, 8));
        right.setOpaque(false);
        JPanel historyCard = createCardPanel("Historique d'écoute");
        historyCard.setLayout(new BorderLayout(8, 8));
        historyCard.add(new JScrollPane(historyList), BorderLayout.CENTER);
        historyCard.add(makeActionButton("Lire depuis l'historique", ThemeRetro.BOUTON_PRIMAIRE, e -> playSelectionFromHistory()), BorderLayout.SOUTH);

        JPanel recoCard = createCardPanel("Recommandations");
        recoCard.setLayout(new BorderLayout(8, 8));
        recoCard.add(new JScrollPane(recoList), BorderLayout.CENTER);
        recoCard.add(makeActionButton("Lire une recommandation", ThemeRetro.BOUTON_PRIMAIRE, e -> playSelectionFromRecommendations()), BorderLayout.SOUTH);

        right.add(historyCard);
        right.add(recoCard);

        panel.add(playlistsCard);
        panel.add(contentCard);
        panel.add(right);
        return panel;
    }

    private JPanel buildAdminTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(ThemeRetro.FOND);
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JPanel infoCard = createCardPanel("Actions administrateur");
        infoCard.setLayout(new BorderLayout());
        infoCard.add(new JScrollPane(adminArea), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(0, 2, 6, 6));
        buttons.setOpaque(false);
        buttons.add(makeActionButton("Ajouter artiste", ThemeRetro.BOUTON_PRIMAIRE, e -> adminAddArtist()));
        buttons.add(makeActionButton("Ajouter groupe", ThemeRetro.BOUTON_PRIMAIRE, e -> adminAddGroup()));
        buttons.add(makeActionButton("Lier artiste à un groupe", ThemeRetro.BOUTON_PRIMAIRE, e -> adminLinkArtistGroup()));
        buttons.add(makeActionButton("Ajouter album", ThemeRetro.BOUTON_PRIMAIRE, e -> adminAddAlbum()));
        buttons.add(makeActionButton("Ajouter morceau", ThemeRetro.BOUTON_PRIMAIRE, e -> adminAddSong()));
        buttons.add(makeActionButton("Supprimer morceau sélectionné", ThemeRetro.BOUTON_ACCENT, e -> adminRemoveSong()));
        buttons.add(makeActionButton("Supprimer album sélectionné", ThemeRetro.BOUTON_ACCENT, e -> adminRemoveAlbum()));
        buttons.add(makeActionButton("Supprimer artiste / groupe sélectionné", ThemeRetro.BOUTON_ACCENT, e -> adminRemoveArtist()));
        buttons.add(makeActionButton("Suspendre un abonné", ThemeRetro.BOUTON_ACCENT, e -> adminSuspendUser()));
        buttons.add(makeActionButton("Réactiver un abonné", ThemeRetro.BOUTON_ACCENT, e -> adminReactivateUser()));
        buttons.add(makeActionButton("Supprimer un abonné", ThemeRetro.BOUTON_ACCENT, e -> adminDeleteUser()));
        infoCard.add(buttons, BorderLayout.SOUTH);

        panel.add(infoCard, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStatsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(ThemeRetro.FOND);
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JPanel card = createCardPanel("Statistiques administrateur");
        card.setLayout(new BorderLayout(8, 8));
        JButton refreshButton = makeActionButton("Rafraîchir", ThemeRetro.BOUTON_PRIMAIRE, e -> refreshStats());
        card.add(refreshButton, BorderLayout.NORTH);
        card.add(new JScrollPane(statsArea), BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildPlayerPanel() {
        JPanel player = createCardPanel("Lecteur musical simulé (20 s par morceau • durée réelle visible dans les détails)");
        player.setLayout(new BorderLayout(8, 8));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        nowPlayingLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        queueLabel.setForeground(new Color(80, 80, 80));
        info.add(nowPlayingLabel);
        info.add(Box.createVerticalStrut(4));
        info.add(queueLabel);

        JPanel timeline = new JPanel(new BorderLayout(8, 8));
        timeline.setOpaque(false);
        positionSlider.setEnabled(false);
        timeline.add(currentTimeLabel, BorderLayout.WEST);
        timeline.add(positionSlider, BorderLayout.CENTER);
        timeline.add(totalTimeLabel, BorderLayout.EAST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        controls.setOpaque(false);
        for (JButton button : List.of(previousButton, rewindButton, playPauseButton, forwardButton, nextButton, stopButton)) {
            styleButton(button, ThemeRetro.BOUTON_ACCENT, Color.WHITE);
            controls.add(button);
        }

        player.add(info, BorderLayout.NORTH);
        player.add(timeline, BorderLayout.CENTER);
        player.add(controls, BorderLayout.SOUTH);
        return player;
    }

    private JPanel wrapListInCard(JList<?> list, String title) {
        JPanel card = createCardPanel(title);
        card.setLayout(new BorderLayout());
        card.add(new JScrollPane(list), BorderLayout.CENTER);
        return card;
    }

    private JPanel createCardPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(ThemeRetro.CARTE);
        panel.setBorder(compoundBorder(title));
        return panel;
    }

    private Border compoundBorder(String title) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ThemeRetro.BORDURE), title),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        );
    }

    private JTextArea createTextArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setBackground(Color.WHITE);
        return area;
    }

    private JButton makeActionButton(String text, Color color, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        styleButton(button, color, Color.WHITE);
        button.addActionListener(action);
        return button;
    }

    private void styleButton(AbstractButton button, Color background, Color foreground) {
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Dialog", Font.BOLD, 12));
        button.setForeground(foreground);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.CENTER);
        button.setMargin(new Insets(6, 12, 6, 12));
        button.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        button.putClientProperty("baseColor", background);
        button.putClientProperty("hover", false);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.putClientProperty("hover", true);
                button.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.putClientProperty("hover", false);
                button.repaint();
            }
        });

        button.setUI(new BasicButtonUI() {
            @Override
            public void installDefaults(AbstractButton b) {
                super.installDefaults(b);
                b.setOpaque(false);
            }

            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton b = (AbstractButton) c;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = b.getWidth();
                int h = b.getHeight();
                int arc = 14;

                Color base = (Color) b.getClientProperty("baseColor");
                boolean hover = Boolean.TRUE.equals(b.getClientProperty("hover"));

                Color fill = hover ? base.brighter() : base;
                Color border = hover ? base.darker().darker() : base.darker();

                g2.setColor(fill);
                g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);

                g2.setColor(border);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

                if (hover) {
                    g2.setColor(new Color(255, 255, 255, 35));
                    g2.fillRoundRect(1, 1, w - 2, h / 2, arc, arc);
                }

                g2.dispose();
                super.paint(g, c);
            }
        });
    }

    private void installDoubleClickToPlay(JList<Morceau> list, java.util.function.Supplier<List<Morceau>> queueSupplier) {
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && list.getSelectedValue() != null) {
                    startPlayback(queueSupplier.get(), list.getSelectedValue());
                }
            }
        });
    }

    private void openAccessHub() {
        JDialog dialog = new JDialog(this, "Centre d'accès Javazik", true);
        dialog.setSize(420, 250);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Visiteur", buildAccessVisitorTab(dialog));
        tabs.addTab("Abonné", buildAccessLoginTab(dialog, false));
        tabs.addTab("Administrateur", buildAccessLoginTab(dialog, true));
        tabs.addTab("Créer un compte", buildCreateAccountTab(dialog));

        dialog.setContentPane(tabs);
        dialog.setVisible(true);
    }

    private JPanel buildAccessVisitorTab(JDialog dialog) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JLabel text = new JLabel("Le visiteur reste limité au catalogue et à 5 écoutes par session.");
        text.setForeground(ThemeRetro.TEXTE_SECONDAIRE);
        JButton button = makeActionButton("Rester en mode visiteur", ThemeRetro.BOUTON_ACCENT, e -> {
            stopPlayback();
            controller.continuerCommeVisiteur();
            dialog.dispose();
            refreshAll();
        });
        panel.add(text, BorderLayout.CENTER);
        panel.add(button, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildAccessLoginTab(JDialog dialog, boolean admin) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JTextField loginField = compactTextField();
        JPasswordField passwordField = compactPasswordField();
        JPanel form = buildCompactFormPanel(new String[]{admin ? "Identifiant admin" : "Identifiant abonné", "Mot de passe"},
                new JComponent[]{loginField, passwordField});

        JLabel hint = new JLabel(admin
                ? "Connexion centralisée conforme au menu principal du sujet."
                : "Accès abonné : playlists, historique, avis et recommandations.");
        hint.setForeground(ThemeRetro.TEXTE_SECONDAIRE);

        JButton button = makeActionButton(admin ? "Se connecter comme admin" : "Se connecter comme abonné", ThemeRetro.BOUTON_PRIMAIRE, e -> {
            try {
                stopPlayback();
                if (admin) {
                    controller.connecterAdmin(loginField.getText().trim(), new String(passwordField.getPassword()));
                } else {
                    controller.connecterAbonne(loginField.getText().trim(), new String(passwordField.getPassword()));
                }
                dialog.dispose();
                refreshAll();
            } catch (AuthentificationException ex) {
                showError(ex.getMessage());
            }
        });

        panel.add(hint, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        panel.add(button, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildCreateAccountTab(JDialog dialog) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JTextField loginField = compactTextField();
        JPasswordField passwordField = compactPasswordField();
        JTextField displayNameField = compactTextField();
        JPanel form = buildCompactFormPanel(new String[]{"Identifiant", "Mot de passe", "Nom affiché"},
                new JComponent[]{loginField, passwordField, displayNameField});

        JButton button = makeActionButton("Créer le compte", ThemeRetro.BOUTON_PRIMAIRE, e -> {
            try {
                controller.creerCompte(loginField.getText().trim(), new String(passwordField.getPassword()), displayNameField.getText().trim());
                showInfo("Compte créé. Vous pouvez maintenant vous connecter comme abonné.");
                dialog.dispose();
                refreshAll();
            } catch (JavazikException ex) {
                showError(ex.getMessage());
            }
        });

        panel.add(new JLabel("Création centralisée d'un compte abonné."), BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        panel.add(button, BorderLayout.SOUTH);
        return panel;
    }

    private JTextField compactTextField() {
        JTextField field = new JTextField();
        field.setColumns(12);
        return field;
    }

    private JPasswordField compactPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setColumns(12);
        return field;
    }

    private JPanel buildCompactFormPanel(String[] labels, JComponent[] fields) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0;
            form.add(new JLabel(labels[i] + " :"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1;
            form.add(fields[i], gbc);
        }
        return form;
    }

    private void refreshAll() {
        refreshSessionZone();
        rebuildTabsForCurrentSession();
        refreshCatalogue();
        refreshPlaylists();
        refreshStats();
        updatePlaylistAccessState();
        repaint();
    }

    private void refreshSessionZone() {
        ContexteSession session = controller.getSession();
        sessionLabel.setText("Session : " + session);
        saveButton.setEnabled(true);

        if (session.estVisiteur()) {
            saveButton.setVisible(false);
            logoutButton.setVisible(false);
            logoutButton.setEnabled(false);
            sessionLabel.setText("Session : visiteur • " + session.getEcoutesRestantes() + " écoute(s) restante(s)");
            subtitleLabel.setText("Catalogue, recherche, navigation et écoute limitée.");
        } else if (session.estAbonne()) {
            saveButton.setVisible(true);
            logoutButton.setVisible(true);
            logoutButton.setEnabled(true);
            logoutButton.setText("Retour visiteur");
            int playlists = controller.getPlaylistsCourantes().size();
            int historique = controller.getHistoriqueCourant().size();
            sessionLabel.setText("Session : abonné • " + session.getUtilisateurCourant().getIdentifiant());
            subtitleLabel.setText("Playlists : " + playlists + " • historique : " + historique + " morceau(x) • avis et recommandations.");
        } else if (session.estAdmin()) {
            saveButton.setVisible(true);
            logoutButton.setVisible(true);
            logoutButton.setEnabled(true);
            logoutButton.setText("Retour visiteur");
            sessionLabel.setText("Session : administrateur • " + session.getUtilisateurCourant().getIdentifiant());
            subtitleLabel.setText("Gestion du catalogue, des comptes abonnés et des statistiques.");
        } else {
            logoutButton.setEnabled(false);
            logoutButton.setText("Aucune session");
            subtitleLabel.setText("Choisissez un profil depuis le centre d'accès centralisé.");
        }
    }

    private void rebuildTabsForCurrentSession() {
        String selectedTitle = mainTabs.getTabCount() == 0 ? "Catalogue" : mainTabs.getTitleAt(mainTabs.getSelectedIndex());
        mainTabs.removeAll();
        mainTabs.addTab("Catalogue", buildCatalogTab());
        ContexteSession session = controller.getSession();
        if (session.estAbonne()) {
            mainTabs.addTab("Playlists", buildPlaylistsTab());
        }
        if (session.estAdmin()) {
            mainTabs.addTab("Administration", buildAdminTab());
            mainTabs.addTab("Statistiques", buildStatsTab());
        }
        selectTab(selectedTitle);
    }

    private void selectTab(String title) {
        for (int i = 0; i < mainTabs.getTabCount(); i++) {
            if (mainTabs.getTitleAt(i).equals(title)) {
                mainTabs.setSelectedIndex(i);
                return;
            }
        }
        if (mainTabs.getTabCount() > 0) {
            mainTabs.setSelectedIndex(0);
        }
    }

    private void refreshCatalogue() {
        songsModel.clear();
        albumsModel.clear();
        artistsModel.clear();
        String query = searchField.getText().trim();
        Genre genre = (Genre) genreCombo.getSelectedItem();
        ModeTri sortMode = (ModeTri) sortCombo.getSelectedItem();
        Integer yearMin = yearFilterCheck.isSelected() ? (Integer) yearMinSpinner.getValue() : null;
        Integer yearMax = yearFilterCheck.isSelected() ? (Integer) yearMaxSpinner.getValue() : null;

        for (Morceau morceau : controller.rechercheAvancee(query, genre, yearMin, yearMax, sortMode)) {
            songsModel.addElement(morceau);
        }
        for (Album album : controller.rechercherAlbums(query)) {
            albumsModel.addElement(album);
        }
        for (EntiteArtistique entite : controller.rechercherEntites(query)) {
            artistsModel.addElement(entite);
        }
    }

    private void refreshPlaylists() {
        playlistModel.clear();
        playlistSongsModel.clear();
        historyModel.clear();
        recoModel.clear();
        if (!controller.getSession().estAbonne()) {
            return;
        }
        for (Playlist playlist : controller.getPlaylistsCourantes()) {
            playlistModel.addElement(playlist);
        }
        for (Morceau morceau : controller.getHistoriqueCourant()) {
            historyModel.addElement(morceau);
        }
        for (Morceau morceau : controller.getRecommandationsCourantes()) {
            recoModel.addElement(morceau);
        }
        if (!playlistModel.isEmpty()) {
            playlistList.setSelectedIndex(0);
        }
        refreshPlaylistSongs();
    }

    private void refreshPlaylistSongs() {
        playlistSongsModel.clear();
        Playlist playlist = playlistList.getSelectedValue();
        if (playlist != null) {
            for (Morceau morceau : playlist.getMorceaux()) {
                playlistSongsModel.addElement(morceau);
            }
        }
    }

    private void refreshStats() {
        if (!controller.getSession().estAdmin()) {
            statsArea.setText("Statistiques réservées aux administrateurs.");
            return;
        }
        try {
            StatistiquesSnapshot stats = controller.getStatistiques();
            statsArea.setText(stats.toString());
        } catch (JavazikException e) {
            statsArea.setText(e.getMessage());
        }
    }

    private void updatePlaylistAccessState() {
        boolean abonne = controller.getSession().estAbonne();
        addToPlaylistButton.setVisible(abonne);
        addToPlaylistButton.setEnabled(abonne);
    }

    private void showDetailsForSelectedSong() {
        Morceau morceau = getSelectedSongFromAnyList();
        if (morceau == null) {
            showError("Sélectionnez un morceau.");
            return;
        }
        detailArea.setText(controller.detailsNavigation(morceau));
        selectTab("Catalogue");
    }

    private void addSelectedSongToPlaylist() {
        if (!controller.getSession().estAbonne()) {
            showError("Seul un abonné peut ajouter un morceau à une playlist.");
            return;
        }
        Playlist playlist = choosePlaylistFromDialog();
        Morceau morceau = songsList.getSelectedValue();
        if (playlist == null || morceau == null) {
            showError("Sélectionnez un morceau dans le catalogue puis une playlist.");
            return;
        }
        try {
            controller.ajouterMorceauAPlaylist(playlist.getId(), morceau);
            refreshPlaylists();
            showInfo("Morceau ajouté à la playlist « " + playlist.getNom() + " ».");
        } catch (JavazikException e) {
            showError(e.getMessage());
        }
    }

    private void createPlaylist() {
        String name = JOptionPane.showInputDialog(this, "Nom de la playlist :");
        if (name == null) {
            return;
        }
        try {
            controller.creerPlaylist(name.trim());
            refreshPlaylists();
        } catch (JavazikException e) {
            showError(e.getMessage());
        }
    }

    private void renamePlaylist() {
        Playlist playlist = playlistList.getSelectedValue();
        if (playlist == null) {
            showError("Sélectionnez une playlist.");
            return;
        }
        String newName = JOptionPane.showInputDialog(this, "Nouveau nom :", playlist.getNom());
        if (newName == null) {
            return;
        }
        try {
            controller.renommerPlaylist(playlist.getId(), newName.trim());
            refreshPlaylists();
        } catch (JavazikException e) {
            showError(e.getMessage());
        }
    }

    private void deletePlaylist() {
        Playlist playlist = playlistList.getSelectedValue();
        if (playlist == null) {
            showError("Sélectionnez une playlist.");
            return;
        }
        try {
            controller.supprimerPlaylist(playlist.getId());
            refreshPlaylists();
        } catch (JavazikException e) {
            showError(e.getMessage());
        }
    }

    private void removeSongFromPlaylist() {
        Playlist playlist = playlistList.getSelectedValue();
        Morceau morceau = playlistSongsList.getSelectedValue();
        if (playlist == null || morceau == null) {
            showError("Sélectionnez une playlist et un morceau.");
            return;
        }
        try {
            controller.retirerMorceauDePlaylist(playlist.getId(), morceau);
            refreshPlaylists();
        } catch (JavazikException e) {
            showError(e.getMessage());
        }
    }

    private void copyFromOtherPlaylist() {
        Playlist target = playlistList.getSelectedValue();
        if (target == null) {
            showError("Sélectionnez d'abord la playlist cible.");
            return;
        }
        List<Playlist> all = controller.getPlaylistsCourantes();
        if (all.size() < 2) {
            showError("Il faut au moins deux playlists pour copier des morceaux.");
            return;
        }
        Playlist source = (Playlist) JOptionPane.showInputDialog(
                this,
                "Playlist source :",
                "Copier depuis une playlist",
                JOptionPane.QUESTION_MESSAGE,
                null,
                all.toArray(),
                all.get(0)
        );
        if (source == null) {
            return;
        }
        try {
            controller.ajouterDepuisAutrePlaylist(target.getId(), source.getId());
            refreshPlaylists();
        } catch (JavazikException e) {
            showError(e.getMessage());
        }
    }

    private void reviewSelectedSong() {
        Morceau morceau = getSelectedSongFromLibrary();
        if (morceau == null) {
            showError("Sélectionnez un morceau dans la bibliothèque, l'historique ou les recommandations.");
            return;
        }
        JTextField noteField = new JTextField();
        JTextField commentField = new JTextField();
        Object[] message = {"Note (1..5) :", noteField, "Commentaire :", commentField};
        if (JOptionPane.showConfirmDialog(this, message, "Noter un morceau", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                controller.noterMorceau(morceau, Integer.parseInt(noteField.getText().trim()), commentField.getText().trim());
                refreshAll();
            } catch (NumberFormatException e) {
                showError("La note doit être un entier entre 1 et 5.");
            } catch (JavazikException e) {
                showError(e.getMessage());
            }
        }
    }

    private void deleteReviewSelectedSong() {
        Morceau morceau = getSelectedSongFromLibrary();
        if (morceau == null) {
            showError("Sélectionnez un morceau.");
            return;
        }
        try {
            controller.supprimerAvis(morceau);
            refreshAll();
        } catch (JavazikException e) {
            showError(e.getMessage());
        }
    }

    private void adminAddArtist() {
        JTextField nomField = new JTextField();
        JTextField paysField = new JTextField();
        Object[] message = {"Nom :", nomField, "Pays :", paysField};
        if (JOptionPane.showConfirmDialog(this, message, "Ajouter un artiste", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                controller.ajouterArtiste(nomField.getText().trim(), paysField.getText().trim());
                refreshAll();
            } catch (JavazikException e) {
                showError(e.getMessage());
            }
        }
    }

    private void adminAddGroup() {
        JTextField nomField = new JTextField();
        JTextField paysField = new JTextField();
        Object[] message = {"Nom :", nomField, "Pays :", paysField};
        if (JOptionPane.showConfirmDialog(this, message, "Ajouter un groupe", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                controller.ajouterGroupe(nomField.getText().trim(), paysField.getText().trim());
                refreshAll();
            } catch (JavazikException e) {
                showError(e.getMessage());
            }
        }
    }

    private void adminLinkArtistGroup() {
        List<Artiste> artistes = controller.getCatalogue().getArtistes();
        List<Groupe> groupes = controller.getCatalogue().getGroupes();
        if (artistes.isEmpty() || groupes.isEmpty()) {
            showError("Ajoutez d'abord au moins un artiste et un groupe.");
            return;
        }
        Artiste artiste = (Artiste) JOptionPane.showInputDialog(this, "Artiste :", "Choisir un artiste",
                JOptionPane.QUESTION_MESSAGE, null, artistes.toArray(), artistes.get(0));
        Groupe groupe = (Groupe) JOptionPane.showInputDialog(this, "Groupe :", "Choisir un groupe",
                JOptionPane.QUESTION_MESSAGE, null, groupes.toArray(), groupes.get(0));
        if (artiste == null || groupe == null) {
            return;
        }
        try {
            controller.lierArtisteAuGroupe(groupe, artiste);
            refreshAll();
        } catch (JavazikException e) {
            showError(e.getMessage());
        }
    }

    private void adminAddAlbum() {
        List<EntiteArtistique> entities = controller.getCatalogue().getEntitesArtistiques();
        if (entities.isEmpty()) {
            showError("Ajoutez d'abord un artiste ou un groupe.");
            return;
        }
        EntiteArtistique entity = (EntiteArtistique) JOptionPane.showInputDialog(this, "Interprète :", "Ajouter un album",
                JOptionPane.QUESTION_MESSAGE, null, entities.toArray(), entities.get(0));
        if (entity == null) {
            return;
        }
        JTextField titleField = new JTextField();
        JTextField yearField = new JTextField();
        Object[] message = {"Titre :", titleField, "Année :", yearField};
        if (JOptionPane.showConfirmDialog(this, message, "Ajouter un album", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                controller.ajouterAlbum(titleField.getText().trim(), Integer.parseInt(yearField.getText().trim()), entity);
                refreshAll();
            } catch (NumberFormatException e) {
                showError("L'année doit être un entier valide.");
            } catch (JavazikException e) {
                showError(e.getMessage());
            }
        }
    }

    private void adminAddSong() {
        List<EntiteArtistique> entities = controller.getCatalogue().getEntitesArtistiques();
        if (entities.isEmpty()) {
            showError("Ajoutez d'abord un artiste ou un groupe.");
            return;
        }
        EntiteArtistique entity = (EntiteArtistique) JOptionPane.showInputDialog(this, "Interprète :", "Ajouter un morceau",
                JOptionPane.QUESTION_MESSAGE, null, entities.toArray(), entities.get(0));
        if (entity == null) {
            return;
        }
        JTextField titleField = compactTextField();
        JTextField yearField = compactTextField();
        JComboBox<Genre> genreField = new JComboBox<>(Genre.values());
        JTextField durationField = compactTextField();
        durationField.setText("210");
        Object[] message = {"Titre :", titleField, "Durée réelle (secondes) :", durationField, "Lecture simulée :", new JLabel("20 secondes"), "Année :", yearField, "Genre :", genreField};
        if (JOptionPane.showConfirmDialog(this, message, "Ajouter un morceau", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                controller.ajouterMorceau(
                        titleField.getText().trim(),
                        Integer.parseInt(durationField.getText().trim()),
                        (Genre) genreField.getSelectedItem(),
                        Integer.parseInt(yearField.getText().trim()),
                        entity,
                        chooseAlbumsOptional()
                );
                refreshAll();
            } catch (NumberFormatException e) {
                showError("La durée et l'année doivent être des entiers valides. L'année doit rester comprise entre 1900 et 2026.");
            } catch (JavazikException e) {
                showError(e.getMessage());
            }
        }
    }

    private List<Album> chooseAlbumsOptional() {
        List<Album> albums = controller.getCatalogue().getAlbums();
        if (albums.isEmpty()) {
            return List.of();
        }
        JList<Album> chooserList = new JList<>(albums.toArray(new Album[0]));
        chooserList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        chooserList.setCellRenderer(new RenduListeMedia());
        if (JOptionPane.showConfirmDialog(this, new JScrollPane(chooserList),
                "Associer le morceau à un ou plusieurs albums (facultatif)", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            return chooserList.getSelectedValuesList();
        }
        return List.of();
    }

    private void adminRemoveSong() {
        Morceau morceau = songsList.getSelectedValue();
        if (morceau == null) {
            showError("Sélectionnez un morceau dans l'onglet Catalogue.");
            return;
        }
        try {
            controller.supprimerMorceau(morceau.getId());
            refreshAll();
        } catch (JavazikException e) {
            showError(e.getMessage());
        }
    }

    private void adminRemoveAlbum() {
        Album album = albumsList.getSelectedValue();
        if (album == null) {
            showError("Sélectionnez un album dans l'onglet Catalogue.");
            return;
        }
        try {
            controller.supprimerAlbum(album.getId());
            refreshAll();
        } catch (JavazikException e) {
            showError(e.getMessage());
        }
    }

    private void adminRemoveArtist() {
        EntiteArtistique entity = artistsList.getSelectedValue();
        if (entity == null) {
            showError("Sélectionnez un artiste ou un groupe dans l'onglet Catalogue.");
            return;
        }
        try {
            controller.supprimerEntite(entity.getId());
            refreshAll();
        } catch (JavazikException e) {
            showError(e.getMessage());
        }
    }

    private void adminSuspendUser() {
        adminAccountAction("Suspendre quel abonné ?", AccountAction.SUSPENDRE);
    }

    private void adminReactivateUser() {
        adminAccountAction("Réactiver quel abonné ?", AccountAction.REACTIVER);
    }

    private void adminDeleteUser() {
        adminAccountAction("Supprimer quel abonné ?", AccountAction.SUPPRIMER);
    }

    private void adminAccountAction(String prompt, AccountAction action) {
        List<String> abonnes;
        try {
            abonnes = controller.getUtilisateurs().stream()
                    .filter(user -> "Abonné".equals(user.getRole()))
                    .map(Utilisateur::getIdentifiant)
                    .toList();
        } catch (JavazikException e) {
            showError(e.getMessage());
            return;
        }
        if (abonnes.isEmpty()) {
            showError("Aucun abonné disponible.");
            return;
        }
        String id = (String) JOptionPane.showInputDialog(this, prompt, "Choix d'un abonné",
                JOptionPane.QUESTION_MESSAGE, null, abonnes.toArray(), abonnes.get(0));
        if (id == null) {
            return;
        }
        try {
            switch (action) {
                case SUSPENDRE -> controller.suspendreAbonne(id);
                case REACTIVER -> controller.reactiverAbonne(id);
                case SUPPRIMER -> controller.supprimerAbonne(id);
            }
            refreshAll();
        } catch (JavazikException e) {
            showError(e.getMessage());
        }
    }

    private void saveNow() {
        try {
            controller.sauvegarder();
            showInfo("Sauvegarde effectuée.");
        } catch (IOException e) {
            showError("Erreur de sauvegarde : " + e.getMessage());
        }
    }

    // ===== Simulated player =====

    private void playSelectionFromCatalogue() {
        Morceau morceau = songsList.getSelectedValue();
        if (morceau == null) {
            showError("Sélectionnez un morceau du catalogue.");
            return;
        }
        startPlayback(queueFromSongsModel(), morceau);
    }

    private void playSelectionFromPlaylist() {
        Morceau morceau = playlistSongsList.getSelectedValue();
        if (morceau == null) {
            showError("Sélectionnez un morceau de playlist.");
            return;
        }
        startPlayback(queueFromPlaylistSongsModel(), morceau);
    }

    private void playSelectionFromHistory() {
        Morceau morceau = historyList.getSelectedValue();
        if (morceau == null) {
            showError("Sélectionnez un morceau dans l'historique.");
            return;
        }
        startPlayback(queueFromHistoryModel(), morceau);
    }

    private void playSelectionFromRecommendations() {
        Morceau morceau = recoList.getSelectedValue();
        if (morceau == null) {
            showError("Sélectionnez une recommandation.");
            return;
        }
        startPlayback(queueFromRecoModel(), morceau);
    }

    private void startPlayback(List<Morceau> queue, Morceau morceau) {
        if (queue == null || queue.isEmpty() || morceau == null) {
            return;
        }
        int index = queue.indexOf(morceau);
        if (index < 0) {
            index = 0;
        }
        playbackTimer.stop();
        currentQueue = List.copyOf(queue);
        currentQueueIndex = index;
        currentPositionSeconds = 0;
        beginCurrentTrack();
    }

    private void beginCurrentTrack() {
        Morceau morceau = getCurrentTrack();
        if (morceau == null) {
            stopPlayback();
            return;
        }
        try {
            controller.ecouterMorceau(morceau);
        } catch (JavazikException e) {
            showError(e.getMessage());
            stopPlayback();
            return;
        }
        currentlyPlaying = true;
        playbackTimer.restart();
        updatePlayerUi();
        refreshSessionZone();
        refreshPlaylists();
        refreshStats();
    }

    private void onPlayPauseClicked() {
        if (getCurrentTrack() == null) {
            Morceau selected = getSelectedSongFromAnyList();
            if (selected == null) {
                showError("Sélectionnez un morceau à lire.");
                return;
            }
            startPlayback(resolveQueueForSelectedSong(selected), selected);
            return;
        }
        currentlyPlaying = !currentlyPlaying;
        if (currentlyPlaying) {
            playbackTimer.restart();
        } else {
            playbackTimer.stop();
        }
        updatePlayerUi();
    }

    private void playPreviousTrack() {
        if (currentQueue.isEmpty()) {
            return;
        }
        if (currentQueueIndex > 0) {
            currentQueueIndex--;
            currentPositionSeconds = 0;
            beginCurrentTrack();
        } else {
            currentPositionSeconds = 0;
            updatePlayerUi();
        }
    }

    private void playNextTrack() {
        if (currentQueue.isEmpty()) {
            return;
        }
        if (currentQueueIndex < currentQueue.size() - 1) {
            currentQueueIndex++;
            currentPositionSeconds = 0;
            beginCurrentTrack();
        } else {
            stopPlayback();
        }
    }

    private void shiftPlayback(int delta) {
        Morceau morceau = getCurrentTrack();
        if (morceau == null) {
            return;
        }
        currentPositionSeconds = Math.max(0, Math.min(getSimulatedDuration(morceau), currentPositionSeconds + delta));
        updatePlayerUi();
    }

    private void stopPlayback() {
        playbackTimer.stop();
        currentlyPlaying = false;
        currentQueue = List.of();
        currentQueueIndex = -1;
        currentPositionSeconds = 0;
        updatePlayerUi();
    }

    private void onPlaybackTick() {
        if (!currentlyPlaying) {
            return;
        }
        Morceau morceau = getCurrentTrack();
        if (morceau == null) {
            stopPlayback();
            return;
        }
        currentPositionSeconds++;
        if (currentPositionSeconds >= getSimulatedDuration(morceau)) {
            currentPositionSeconds = getSimulatedDuration(morceau);
            updatePlayerUi();
            if (currentQueueIndex < currentQueue.size() - 1) {
                currentQueueIndex++;
                currentPositionSeconds = 0;
                beginCurrentTrack();
            } else {
                stopPlayback();
            }
            return;
        }
        updatePlayerUi();
    }

    private void onSliderChanged(ChangeEvent event) {
        if (sliderInternalUpdate) {
            return;
        }
        if (positionSlider.getValueIsAdjusting()) {
            sliderUserEditing = true;
            currentTimeLabel.setText(Morceau.formaterDuree(positionSlider.getValue()));
            return;
        }
        if (sliderUserEditing) {
            sliderUserEditing = false;
            currentPositionSeconds = positionSlider.getValue();
            updatePlayerUi();
        }
    }

    private void updatePlayerUi() {
        Morceau morceau = getCurrentTrack();
        if (morceau == null) {
            nowPlayingLabel.setText("Aucun morceau en cours");
            queueLabel.setText("Sélectionnez un morceau dans le catalogue, une playlist, l'historique ou les recommandations.");
            currentTimeLabel.setText("00:00");
            totalTimeLabel.setText("00:00");
            sliderInternalUpdate = true;
            positionSlider.setMaximum(0);
            positionSlider.setValue(0);
            positionSlider.setEnabled(false);
            sliderInternalUpdate = false;
            playPauseButton.setText("▶");
            return;
        }
        nowPlayingLabel.setText(morceau.getTitre() + " • " + morceau.getInterprete().getNom());
        queueLabel.setText("File : " + (currentQueueIndex + 1) + "/" + currentQueue.size() + " • " + morceau.getGenre() + " • " + (currentlyPlaying ? "Lecture" : "Pause"));
        currentTimeLabel.setText(Morceau.formaterDuree(currentPositionSeconds));
        totalTimeLabel.setText(Morceau.formaterDuree(getSimulatedDuration(morceau)));
        sliderInternalUpdate = true;
        positionSlider.setMaximum(getSimulatedDuration(morceau));
        positionSlider.setValue(currentPositionSeconds);
        positionSlider.setEnabled(true);
        sliderInternalUpdate = false;
        playPauseButton.setText(currentlyPlaying ? "⏸" : "▶");
    }

    private int getSimulatedDuration(Morceau morceau) {
        return morceau == null ? SIMULATED_PLAYBACK_SECONDS : SIMULATED_PLAYBACK_SECONDS;
    }

    private Morceau getCurrentTrack() {
        if (currentQueueIndex < 0 || currentQueueIndex >= currentQueue.size()) {
            return null;
        }
        return currentQueue.get(currentQueueIndex);
    }

    private List<Morceau> resolveQueueForSelectedSong(Morceau selected) {
        if (songsList.getSelectedValue() == selected) {
            return queueFromSongsModel();
        }
        if (playlistSongsList.getSelectedValue() == selected) {
            return queueFromPlaylistSongsModel();
        }
        if (historyList.getSelectedValue() == selected) {
            return queueFromHistoryModel();
        }
        if (recoList.getSelectedValue() == selected) {
            return queueFromRecoModel();
        }
        return List.of(selected);
    }

    private List<Morceau> queueFromSongsModel() { return listModelToList(songsModel); }
    private List<Morceau> queueFromPlaylistSongsModel() { return listModelToList(playlistSongsModel); }
    private List<Morceau> queueFromHistoryModel() { return listModelToList(historyModel); }
    private List<Morceau> queueFromRecoModel() { return listModelToList(recoModel); }

    private List<Morceau> listModelToList(DefaultListModel<Morceau> model) {
        List<Morceau> queue = new ArrayList<>();
        for (int i = 0; i < model.size(); i++) {
            queue.add(model.getElementAt(i));
        }
        return queue;
    }

    // ===== Selection / formatting helpers =====

    private Morceau getSelectedSongFromAnyList() {
        if (songsList.getSelectedValue() != null) return songsList.getSelectedValue();
        if (playlistSongsList.getSelectedValue() != null) return playlistSongsList.getSelectedValue();
        if (historyList.getSelectedValue() != null) return historyList.getSelectedValue();
        return recoList.getSelectedValue();
    }

    private Morceau getSelectedSongFromLibrary() {
        if (playlistSongsList.getSelectedValue() != null) return playlistSongsList.getSelectedValue();
        if (historyList.getSelectedValue() != null) return historyList.getSelectedValue();
        return recoList.getSelectedValue();
    }

    private Playlist choosePlaylistFromDialog() {
        List<Playlist> playlists = controller.getPlaylistsCourantes();
        if (playlists.isEmpty()) {
            showError("Créez d'abord une playlist.");
            return null;
        }
        return (Playlist) JOptionPane.showInputDialog(this, "Choisir une playlist :", "Playlists",
                JOptionPane.QUESTION_MESSAGE, null, playlists.toArray(), playlists.get(0));
    }

    private String formatSongList(List<Morceau> songs) {
        if (songs == null || songs.isEmpty()) {
            return "- Aucun morceau";
        }
        StringBuilder builder = new StringBuilder();
        for (Morceau morceau : songs) {
            builder.append("- ").append(morceau.getTitre())
                    .append(" • ").append(Morceau.formaterDuree(morceau.getDureeSecondes()))
                    .append(System.lineSeparator());
        }
        return builder.toString().trim();
    }

    private String formatAlbumList(List<Album> albums) {
        if (albums == null || albums.isEmpty()) {
            return "- Aucun album";
        }
        StringBuilder builder = new StringBuilder();
        for (Album album : albums) {
            builder.append("- ").append(album.getTitre())
                    .append(" (").append(album.getAnnee()).append(")")
                    .append(System.lineSeparator());
        }
        return builder.toString().trim();
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    private enum AccountAction {
        SUSPENDRE,
        REACTIVER,
        SUPPRIMER
    }
}
