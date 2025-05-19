package com.projet.skilllearn.view;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.projet.skilllearn.R;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Passez savedInstanceState à la méthode
        setupBottomNavigation(savedInstanceState);
    }

    // Modifiez la signature de la méthode pour accepter savedInstanceState
    private void setupBottomNavigation(Bundle savedInstanceState) {
        try {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                Fragment selectedFragment = null;
                String fragmentTag = "";

                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                    fragmentTag = "home";
                } else if (itemId == R.id.nav_catalog) {
                    selectedFragment = new CatalogFragment();
                    fragmentTag = "catalog";
                } else if (itemId == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                    fragmentTag = "profile";
                }

                if (selectedFragment != null) {
                    try {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, selectedFragment, fragmentTag)
                                .commit();
                        return true;
                    } catch (Exception e) {
                        Log.e("MainActivity", "Erreur lors du chargement du fragment: " + fragmentTag, e);
                        Toast.makeText(this, "Erreur lors du chargement de la page", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }

                return false;
            });

            // Fragment par défaut
            if (savedInstanceState == null) {
                try {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new HomeFragment(), "home")
                            .commit();
                } catch (Exception e) {
                    Log.e("MainActivity", "Erreur lors du chargement du fragment par défaut", e);
                    Toast.makeText(this, "Erreur lors du chargement de la page d'accueil", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Erreur lors de la configuration de la navigation", e);
        }
    }
}