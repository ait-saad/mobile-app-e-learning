package com.projet.skilllearn.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.projet.skilllearn.R;

public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialiser les composants de l'interface utilisateur
        initUI(view);

        // Configurer les observateurs et les clics
        setupListeners();
    }

    private void initUI(View view) {
        // Initialiser les références aux vues
        // Par exemple:
        // tvWelcome = view.findViewById(R.id.tv_welcome);
    }

    private void setupListeners() {
        // Configurer les listeners pour les boutons, etc.
        // Par exemple:
        // btnExplore.setOnClickListener(v -> navigateToCatalog());
    }

    // Vous pouvez ajouter d'autres méthodes selon les besoins de votre application
}