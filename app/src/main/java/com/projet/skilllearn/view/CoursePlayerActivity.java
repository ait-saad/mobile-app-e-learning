package com.projet.skilllearn.view;


import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;


import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.projet.skilllearn.R;
import com.projet.skilllearn.model.Course;
import com.projet.skilllearn.model.CourseSection;
import com.projet.skilllearn.utils.UserProgressManager;
import com.projet.skilllearn.view.adapters.CoursePagerAdapter;
import com.projet.skilllearn.view.adapters.CourseSectionAdapter;
import com.projet.skilllearn.view.fragments.ContentFragment;
import com.projet.skilllearn.view.fragments.QuizFragment;
import com.projet.skilllearn.viewmodel.CourseViewModel;


import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;


public class CoursePlayerActivity extends AppCompatActivity implements
        CourseSectionAdapter.OnSectionClickListener,
        Player.Listener {


    private FrameLayout videoContainer;
    private PlayerView playerView;
    private FrameLayout youtubePlayerContainer;
    private YouTubePlayerView youtubePlayerView;
    private ExoPlayer player;
    private TextView tvTitle;
    private TextView tvDescription;
    private ProgressBar progressBar;
    private ImageButton btnPrevious;
    private ImageButton btnNext;
    private Button btnMarkComplete;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private RecyclerView rvSections;


    private CourseViewModel viewModel;
    private UserProgressManager progressManager;
    private String courseId;
    private String sectionId;
    private List<CourseSection> sections;
    private int currentSectionIndex = 0;
    private boolean videoCompleted = false;
    private YouTubePlayer youTubePlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_player);


        // Récupérer les identifiants depuis l'intent
        courseId = getIntent().getStringExtra("courseId");
        sectionId = getIntent().getStringExtra("sectionId");


        if (courseId == null) {
            Toast.makeText(this, "Erreur: ID de cours manquant", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        // Initialiser les vues
        initViews();


        // Initialiser le ViewModel
        viewModel = new ViewModelProvider(this).get(CourseViewModel.class);


        // Initialiser le gestionnaire de progrès
        progressManager = UserProgressManager.getInstance();


        // Initialiser les lecteurs vidéo
        initializePlayer();


        // Configurer les onglets et ViewPager
        setupViewPager();


        // Observer les données du ViewModel
        observeViewModel();


        // Charger le cours
        viewModel.selectCourse(courseId);
    }


    private void initViews() {
        videoContainer = findViewById(R.id.video_container);
        playerView = findViewById(R.id.player_view);
        youtubePlayerContainer = findViewById(R.id.youtube_player_container);
        tvTitle = findViewById(R.id.tv_title);
        tvDescription = findViewById(R.id.tv_description);
        progressBar = findViewById(R.id.progress_bar);
        btnPrevious = findViewById(R.id.btn_previous);
        btnNext = findViewById(R.id.btn_next);
        btnMarkComplete = findViewById(R.id.btn_mark_complete);
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);
        rvSections = findViewById(R.id.rv_sections);


        // Configurer RecyclerView
        rvSections.setLayoutManager(new LinearLayoutManager(this));


        // Configurer les listeners
        btnPrevious.setOnClickListener(v -> navigateToPreviousSection());
        btnNext.setOnClickListener(v -> navigateToNextSection());
        btnMarkComplete.setOnClickListener(v -> markSectionAsCompleted());
    }


    private void initializePlayer() {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        player.addListener(this);
    }


    private void setupViewPager() {
        CoursePagerAdapter pagerAdapter = new CoursePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);


        // Configurer les onglets
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            String tabText;
            String contentDescription;


            switch (position) {
                case 0:
                    tabText = getString(R.string.content);
                    contentDescription = getString(R.string.content_tab_description);
                    break;
                case 1:
                    tabText = getString(R.string.notes);
                    contentDescription = getString(R.string.notes_tab_description);
                    break;
                case 2:
                    tabText = getString(R.string.quiz);
                    contentDescription = getString(R.string.quiz_tab_description);
                    break;
                default:
                    tabText = "Tab " + (position + 1);
                    contentDescription = "Tab " + (position + 1);
                    break;
            }


            tab.setText(tabText);
            tab.setContentDescription(contentDescription);
        }).attach();
    }


    private String getTabContentDescription(int tabPosition) {
        switch (tabPosition) {
            case 0:
                return getString(R.string.content_tab_description);
            case 1:
                return getString(R.string.notes_tab_description);
            case 2:
                return getString(R.string.quiz_tab_description);
            default:
                return "Tab " + (tabPosition + 1);
        }
    }


    private void observeViewModel() {
        // Observer le cours sélectionné
        viewModel.getSelectedCourse().observe(this, course -> {
            if (course != null) {
                loadSections(course);
            }
        });


        // Observer l'état de chargement
        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });


        // Observer les erreurs
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                viewModel.clearError();
            }
        });
    }


    private void loadSections(Course course) {
        Log.d("CoursePlayerActivity", "loadSections appelé pour le cours: " + course.getTitle());

        sections = course.getSections();

        // Vérification des sections avec logging détaillé
        if (sections == null) {
            Log.e("CoursePlayerActivity", "La liste des sections est null pour le cours: " + course.getCourseId());
            Toast.makeText(this, "Ce cours ne contient aucune section", Toast.LENGTH_SHORT).show();
            return;
        }

        if (sections.isEmpty()) {
            Log.e("CoursePlayerActivity", "La liste des sections est vide pour le cours: " + course.getCourseId());
            Toast.makeText(this, "Ce cours ne contient aucune section", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("CoursePlayerActivity", "Nombre de sections trouvées: " + sections.size());

        // Configurer l'adaptateur des sections
        try {
            CourseSectionAdapter adapter = new CourseSectionAdapter(this, sections, this);
            rvSections.setAdapter(adapter);
            Log.d("CoursePlayerActivity", "Adaptateur de sections configuré avec succès");
        } catch (Exception e) {
            Log.e("CoursePlayerActivity", "Erreur lors de la configuration de l'adaptateur", e);
            Toast.makeText(this, "Erreur lors du chargement des sections", Toast.LENGTH_SHORT).show();
            return;
        }

        // Déterminer l'index de la section initiale
        if (sectionId != null) {
            Log.d("CoursePlayerActivity", "Recherche de la section spécifique avec ID: " + sectionId);
            boolean sectionFound = false;

            for (int i = 0; i < sections.size(); i++) {
                // Vérifier si la section a un ID
                String currentSectionId = sections.get(i).getSectionId();
                Log.d("CoursePlayerActivity", "Section " + i + " a l'ID: " + currentSectionId);

                if (currentSectionId != null && currentSectionId.equals(sectionId)) {
                    currentSectionIndex = i;
                    sectionFound = true;
                    Log.d("CoursePlayerActivity", "Section trouvée à l'index: " + i);
                    break;
                }
            }

            if (!sectionFound) {
                Log.w("CoursePlayerActivity", "Section avec ID " + sectionId + " non trouvée, utilisation de l'index 0");
            }
        } else {
            Log.d("CoursePlayerActivity", "Aucun ID de section spécifié, utilisation de l'index 0");
        }

        // Charger la section initiale
        Log.d("CoursePlayerActivity", "Chargement de la section à l'index: " + currentSectionIndex);
        loadSection(currentSectionIndex);
    }


    private void loadSection(int index) {
        if (index < 0 || index >= sections.size()) {
            return;
        }


        CourseSection section = sections.get(index);
        currentSectionIndex = index;


        // Mettre à jour l'interface
        tvTitle.setText(section.getTitle());
        tvDescription.setText(section.getDescription());


        // Mettre à jour le contenu des fragments
        refreshFragments(section);


        // Charger la vidéo si disponible
        String videoUrl = section.getVideoUrl();
        if (videoUrl != null && !videoUrl.isEmpty()) {
            videoContainer.setVisibility(View.VISIBLE);


            // Déterminer si c'est une vidéo YouTube ou une vidéo standard
            if (isYouTubeUrl(videoUrl)) {
                loadYouTubeVideo(getYouTubeVideoId(videoUrl));
            } else {
                loadStandardVideo(videoUrl);
            }
        } else {
            videoContainer.setVisibility(View.GONE);
            stopAllPlayers();
        }


        // Mettre à jour l'état des boutons de navigation
        updateNavigationButtons();


        // Réinitialiser l'état de complétion
        videoCompleted = false;
        updateCompleteButtonState();
    }


    private boolean isYouTubeUrl(String url) {
        String pattern = "(?:youtube\\.com/(?:[^/]+/.+/|(?:v|e(?:mbed)?)/|.*[?&]v=)|youtu\\.be/)([^\"&?/\\s]{11})";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);
        return matcher.find();
    }


    private String getYouTubeVideoId(String url) {
        String pattern = "(?:youtube\\.com/(?:[^/]+/.+/|(?:v|e(?:mbed)?)/|.*[?&]v=)|youtu\\.be/)([^\"&?/\\s]{11})";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);


        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }


    private void loadYouTubeVideo(String videoId) {
        if (videoId == null) {
            Toast.makeText(this, "ID de vidéo YouTube invalide", Toast.LENGTH_SHORT).show();
            videoContainer.setVisibility(View.GONE);
            return;
        }


        // Arrêter d'abord tous les lecteurs
        stopAllPlayers();


        playerView.setVisibility(View.GONE);
        youtubePlayerContainer.setVisibility(View.VISIBLE);


        // Créer et initialiser le lecteur YouTube
        if (youtubePlayerView == null) {
            youtubePlayerView = new YouTubePlayerView(this);
            youtubePlayerContainer.removeAllViews(); // Assurez-vous que le conteneur est vide
            youtubePlayerContainer.addView(youtubePlayerView);
            getLifecycle().addObserver(youtubePlayerView);


            youtubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer player) {
                    youTubePlayer = player;
                    player.loadVideo(videoId, 0);
                }


                @Override
                public void onStateChange(@NonNull YouTubePlayer player, @NonNull PlayerConstants.PlayerState state) {
                    if (state == PlayerConstants.PlayerState.ENDED) {
                        videoCompleted = true;
                        updateCompleteButtonState();
                        showVideoCompletedDialog();
                    }
                }
            });
        } else {
            if (youTubePlayer != null) {
                youTubePlayer.loadVideo(videoId, 0);
            }
        }
    }


    private void loadStandardVideo(String videoUrl) {
        // Arrêter d'abord tous les lecteurs
        stopAllPlayers();


        playerView.setVisibility(View.VISIBLE);
        youtubePlayerContainer.setVisibility(View.GONE);


        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
    }


    private void stopAllPlayers() {
        // Arrêter ExoPlayer
        if (player != null) {
            player.stop();
        }


        // Arrêter YouTube Player
        if (youTubePlayer != null) {
            youTubePlayer.pause();
        }
    }


    private void showVideoCompletedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Vidéo terminée")
                .setMessage("Voulez-vous marquer cette section comme terminée ?")
                .setPositiveButton("Oui", (dialog, which) -> markSectionAsCompleted())
                .setNegativeButton("Non", (dialog, which) -> dialog.dismiss())
                .show();
    }


    private void refreshFragments(CourseSection section) {
        // Mettre à jour le fragment de contenu
        ContentFragment contentFragment = ContentFragment.getInstance();
        if (contentFragment != null) {
            contentFragment.updateContent(section.getContent());
        }




        // Mettre à jour le fragment de quiz
        QuizFragment quizFragment = QuizFragment.getInstance();
        if (quizFragment != null && section.getQuiz() != null) {
            quizFragment.updateQuiz(section.getQuiz());
        }
    }




    private void updateNavigationButtons() {
        btnPrevious.setEnabled(currentSectionIndex > 0);
        btnNext.setEnabled(currentSectionIndex < sections.size() - 1);
    }




    private void updateCompleteButtonState() {


        btnMarkComplete.setEnabled(videoCompleted || videoContainer.getVisibility() == View.GONE);
    }


    private void navigateToPreviousSection() {
        if (currentSectionIndex > 0) {
            loadSection(currentSectionIndex - 1);
        }
    }


    private void navigateToNextSection() {
        if (currentSectionIndex < sections.size() - 1) {
            loadSection(currentSectionIndex + 1);
        }
    }


    private void markSectionAsCompleted() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Veuillez vous connecter pour enregistrer votre progression", Toast.LENGTH_SHORT).show();
            return;
        }


        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String sectionId = sections.get(currentSectionIndex).getSectionId();


        progressManager.markSectionCompleted(courseId, sectionId, sections.size());


        Toast.makeText(this, "Section marquée comme terminée", Toast.LENGTH_SHORT).show();


        // Vérifier s'il y a une autre section
        if (currentSectionIndex < sections.size() - 1) {
            showNextSectionDialog();
        } else {
            showCourseCompletedDialog();
        }
    }


    private void showNextSectionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Section terminée")
                .setMessage("Félicitations ! Voulez-vous passer à la section suivante ?")
                .setPositiveButton("Oui", (dialog, which) -> navigateToNextSection())
                .setNegativeButton("Non", (dialog, which) -> dialog.dismiss())
                .show();
    }




    private void showCourseCompletedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cours terminé")
                .setMessage("Félicitations ! Vous avez terminé toutes les sections de ce cours.")
                .setPositiveButton("OK", (dialog, which) -> finish())
                .show();
    }




    @Override
    public void onSectionClick(int position) {
        loadSection(position);
    }


    @Override
    public void onPlaybackStateChanged(int playbackState) {
        if (playbackState == Player.STATE_ENDED) {
            videoCompleted = true;
            updateCompleteButtonState();
            showVideoCompletedDialog();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Reprendre la lecture vidéo si nécessaire
        if (playerView.getVisibility() == View.VISIBLE && !player.isPlaying()) {
            player.play();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        // Mettre en pause la lecture vidéo
        if (player.isPlaying()) {
            player.pause();
        }


        // Mettre en pause YouTube si actif
        if (youTubePlayer != null) {
            youTubePlayer.pause();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Libérer les ressources du lecteur
        if (player != null) {
            player.release();
            player = null;
        }


        // Libérer le lecteur YouTube
        if (youtubePlayerView != null) {
            youtubePlayerView.release();
        }
    }
}
