package com.projet.skilllearn.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.projet.skilllearn.R;
import com.projet.skilllearn.model.Achievement;
import com.projet.skilllearn.utils.UserProgressManager;
import com.projet.skilllearn.view.adapters.AchievementAdapter;

import java.util.ArrayList;
import java.util.List;

public class UserAchievementsFragment extends Fragment {

    private RecyclerView rvAchievements;
    private ProgressBar progressBar;
    private TextView tvNoAchievements;
    private UserProgressManager progressManager;
    private AchievementAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_achievements, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialiser les vues
        rvAchievements = view.findViewById(R.id.rv_achievements);
        progressBar = view.findViewById(R.id.progress_bar);
        tvNoAchievements = view.findViewById(R.id.tv_no_achievements);

        // Initialiser le gestionnaire de progrès
        progressManager = UserProgressManager.getInstance();

        // Configurer RecyclerView
        adapter = new AchievementAdapter(requireContext(), new ArrayList<>());
        rvAchievements.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvAchievements.setAdapter(adapter);

        // Charger les données
        loadAchievements();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recharger les données à chaque fois que le fragment devient visible
        loadAchievements();
    }

    private void loadAchievements() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            tvNoAchievements.setText(R.string.login_to_view_achievements);
            tvNoAchievements.setVisibility(View.VISIBLE);
            rvAchievements.setVisibility(View.GONE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        progressManager.getUserAchievements(new UserProgressManager.AchievementsCallback() {
            @Override
            public void onAchievementsLoaded(List<Achievement> achievements) {
                progressBar.setVisibility(View.GONE);

                if (achievements.isEmpty()) {
                    tvNoAchievements.setVisibility(View.VISIBLE);
                    rvAchievements.setVisibility(View.GONE);
                } else {
                    tvNoAchievements.setVisibility(View.GONE);
                    rvAchievements.setVisibility(View.VISIBLE);
                    adapter.updateAchievements(achievements);
                }
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}