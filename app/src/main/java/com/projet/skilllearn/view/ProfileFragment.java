package com.projet.skilllearn.view;

import android.content.Intent;
import android.os.Bundle;
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

    private ImageView ivProfile;
    private TextView tvName, tvEmail;
    private ProgressBar progressLearning;
    private RecyclerView rvAchievements;
    private Button btnEditProfile, btnLogout;

    private ProfileViewModel viewModel;
    private AchievementAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialiser les vues
        ivProfile = view.findViewById(R.id.iv_profile);
        tvName = view.findViewById(R.id.tv_name);
        tvEmail = view.findViewById(R.id.tv_email);
        progressLearning = view.findViewById(R.id.progress_learning);
        rvAchievements = view.findViewById(R.id.rv_achievements);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnLogout = view.findViewById(R.id.btn_logout);

        // Initialiser le ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Configurer le RecyclerView pour les réalisations
        adapter = new AchievementAdapter(requireContext(), new ArrayList<>());
        rvAchievements.setAdapter(adapter);
        rvAchievements.setLayoutManager(new GridLayoutManager(requireContext(), 3));

        // Observer les données du ViewModel
        setupObservers();

        // Configurer les clics sur les boutons
        setupClickListeners();

        // Charger les données du profil
        loadProfileData();
    }

    private void setupObservers() {
        // Observer les changements dans les données de progression
        viewModel.getLearningProgress().observe(getViewLifecycleOwner(), progress -> {
            if (progress != null) {
                progressLearning.setProgress(progress);
            }
        });

        // Observer les réalisations
        viewModel.getAchievements().observe(getViewLifecycleOwner(), achievements -> {
            if (achievements != null) {
                adapter.updateAchievements(achievements);
            }
        });
    }

    private void setupClickListeners() {
        // Configuration du bouton Modifier le profil
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        // Configuration du bouton Déconnexion
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void loadProfileData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // Définir le nom et l'email
            if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                tvName.setText(currentUser.getDisplayName());
            } else {
                tvName.setText("Nom Utilisateur");
            }

            tvEmail.setText(currentUser.getEmail());

            // Charger la photo de profil
            if (currentUser.getPhotoUrl() != null) {
                Picasso.get()
                        .load(currentUser.getPhotoUrl())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(ivProfile);
            }

            // Charger la progression et les réalisations
            viewModel.loadUserData(currentUser.getUid());
        } else {
            // L'utilisateur n'est pas connecté, rediriger vers la page de connexion
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recharger les données à chaque retour sur le fragment
        loadProfileData();
    }
}