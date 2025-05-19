package com.projet.skilllearn.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.projet.skilllearn.R;
import com.projet.skilllearn.model.Achievement;
import com.projet.skilllearn.view.adapters.AchievementAdapter;
import com.projet.skilllearn.viewmodel.ProfileViewModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    private ImageView ivProfile;
    private TextView tvName, tvEmail;
    private ProgressBar progressLearning;
    private RecyclerView rvAchievements;
    private Button btnEditProfile, btnLogout;

    private ProfileViewModel viewModel;
    private AchievementAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        try {
            return inflater.inflate(R.layout.fragment_profile, container, false);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'inflation du layout", e);
            // Fallback vers un layout simple en cas d'erreur
            return new View(requireContext());
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");

        try {
            // Initialiser les vues
            initializeViews(view);

            // Initialiser le ViewModel
            viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

            // Configurer le RecyclerView pour les réalisations
            setupRecyclerView();

            // Observer les données du ViewModel
            setupObservers();

            // Configurer les clics sur les boutons
            setupClickListeners();

            // Charger les données du profil
            loadProfileData();
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'initialisation du fragment", e);
            Toast.makeText(requireContext(), "Erreur lors du chargement du profil", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeViews(View view) {
        try {
            ivProfile = view.findViewById(R.id.iv_profile);
            tvName = view.findViewById(R.id.tv_name);
            tvEmail = view.findViewById(R.id.tv_email);
            progressLearning = view.findViewById(R.id.progress_learning);
            rvAchievements = view.findViewById(R.id.rv_achievements);
            btnEditProfile = view.findViewById(R.id.btn_edit_profile);
            btnLogout = view.findViewById(R.id.btn_logout);

            // Définir des valeurs par défaut
            tvName.setText("Nom d'utilisateur");
            tvEmail.setText("email@exemple.com");
            progressLearning.setProgress(0);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'initialisation des vues", e);
        }
    }

    private void setupRecyclerView() {
        try {
            adapter = new AchievementAdapter(requireContext(), new ArrayList<>());
            if (rvAchievements != null) {
                rvAchievements.setAdapter(adapter);
                rvAchievements.setLayoutManager(new GridLayoutManager(requireContext(), 3));
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la configuration du RecyclerView", e);
        }
    }

    private void setupObservers() {
        try {
            // Observer les changements dans les données de progression
            viewModel.getLearningProgress().observe(getViewLifecycleOwner(), progress -> {
                if (progress != null && progressLearning != null) {
                    progressLearning.setProgress(progress);
                }
            });

            // Observer les réalisations
            viewModel.getAchievements().observe(getViewLifecycleOwner(), achievements -> {
                if (achievements != null && adapter != null) {
                    adapter.updateAchievements(achievements);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la configuration des observateurs", e);
        }
    }

    private void setupClickListeners() {
        try {
            // Configuration du bouton Modifier le profil
            if (btnEditProfile != null) {
                btnEditProfile.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(requireActivity(), EditProfileActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur lors du lancement de EditProfileActivity", e);
                        Toast.makeText(requireContext(), "Erreur lors de l'ouverture de l'éditeur de profil", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // Configuration du bouton Déconnexion
            if (btnLogout != null) {
                btnLogout.setOnClickListener(v -> {
                    try {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(requireActivity(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur lors de la déconnexion", e);
                        Toast.makeText(requireContext(), "Erreur lors de la déconnexion", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la configuration des listeners", e);
        }
    }

    private void loadProfileData() {
        try {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser != null) {
                // Définir le nom et l'email
                if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                    tvName.setText(currentUser.getDisplayName());
                }

                tvEmail.setText(currentUser.getEmail());

                // Charger la photo de profil
                if (currentUser.getPhotoUrl() != null) {
                    try {
                        Picasso.get()
                                .load(currentUser.getPhotoUrl())
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .error(R.drawable.ic_launcher_foreground)
                                .into(ivProfile);
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur lors du chargement de la photo de profil", e);
                        ivProfile.setImageResource(R.drawable.ic_launcher_foreground);
                    }
                } else {
                    ivProfile.setImageResource(R.drawable.ic_launcher_foreground);
                }

                // Charger la progression et les réalisations
                viewModel.loadUserData(currentUser.getUid());
            } else {
                // L'utilisateur n'est pas connecté, rediriger vers la page de connexion
                try {
                    Intent intent = new Intent(requireActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Erreur lors de la redirection vers LoginActivity", e);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du chargement des données du profil", e);
            Toast.makeText(requireContext(), "Erreur lors du chargement du profil", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        try {
            // Recharger les données à chaque retour sur le fragment
            loadProfileData();
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans onResume", e);
        }
    }
}