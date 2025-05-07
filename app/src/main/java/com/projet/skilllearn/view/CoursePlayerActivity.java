package com.projet.skilllearn.view;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.projet.skilllearn.R;
import com.projet.skilllearn.model.Course;
import com.projet.skilllearn.model.CourseSection;
import com.projet.skilllearn.model.Note;
import com.projet.skilllearn.utils.UserProgressManager;
import com.projet.skilllearn.view.adapters.CoursePagerAdapter;
import com.projet.skilllearn.view.adapters.CourseSectionAdapter;
import com.projet.skilllearn.view.adapters.NoteAdapter;
import com.projet.skilllearn.view.fragments.ContentFragment;
import com.projet.skilllearn.view.fragments.NotesFragment;
import com.projet.skilllearn.view.fragments.QuizFragment;
import com.projet.skilllearn.viewmodel.CourseViewModel;

import java.util.ArrayList;
import java.util.List;

public class CoursePlayerActivity extends AppCompatActivity implements
        CourseSectionAdapter.OnSectionClickListener,
        Player.Listener {

    private PlayerView playerView;
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

        // Initialiser le lecteur vidéo
        initializePlayer();

        // Configurer les onglets et ViewPager
        setupViewPager();

        // Observer les données du ViewModel
        observeViewModel();

        // Charger le cours
        viewModel.selectCourse(courseId);
    }

    private void initViews() {
        playerView = findViewById(R.id.player_view);
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
            switch (position) {
                case 0:
                    tab.setText(R.string.content);
                    break;
                case 1:
                    tab.setText(R.string.notes);
                    break;
                case 2:
                    tab.setText(R.string.quiz);
                    break;
            }
        }).attach();
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
        sections = course.getSections();
        if (sections == null || sections.isEmpty()) {
            Toast.makeText(this, "Ce cours ne contient aucune section", Toast.LENGTH_SHORT).show();
            return;
        }

        // Configurer l'adaptateur des sections
        CourseSectionAdapter adapter = new CourseSectionAdapter(this, sections, this);
        rvSections.setAdapter(adapter);

        // Déterminer l'index de la section initiale
        if (sectionId != null) {
            for (int i = 0; i < sections.size(); i++) {
                if (sections.get(i).getSectionId().equals(sectionId)) {
                    currentSectionIndex = i;
                    break;
                }
            }
        }

        // Charger la section initiale
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
            playerView.setVisibility(View.VISIBLE);
            loadVideo(videoUrl);
        } else {
            playerView.setVisibility(View.GONE);
            player.stop();
        }

        // Mettre à jour l'état des boutons de navigation
        updateNavigationButtons();

        // Réinitialiser l'état de complétion
        videoCompleted = false;
        updateCompleteButtonState();
    }

    private void loadVideo(String videoUrl) {
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
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
        btnMarkComplete.setEnabled(videoCompleted || playerView.getVisibility() == View.GONE);
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

            // Optionnel : afficher un dialogue pour marquer la section comme terminée
            new AlertDialog.Builder(this)
                    .setTitle("Vidéo terminée")
                    .setMessage("Voulez-vous marquer cette section comme terminée ?")
                    .setPositiveButton("Oui", (dialog, which) -> markSectionAsCompleted())
                    .setNegativeButton("Non", (dialog, which) -> dialog.dismiss())
                    .show();
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Libérer les ressources du lecteur
        if (player != null) {
            player.release();
            player = null;
        }
    }
}