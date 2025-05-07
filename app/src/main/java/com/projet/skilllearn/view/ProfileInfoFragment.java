package com.projet.skilllearn.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.projet.skilllearn.R;

public class ProfileInfoFragment extends Fragment {

    private ImageView ivProfilePicture;
    private TextView tvName;
    private TextView tvEmail;
    private Button btnEditProfile;
    private Button btnLogout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialiser les vues
        ivProfilePicture = view.findViewById(R.id.iv_profile_picture);
        tvName = view.findViewById(R.id.tv_name);
        tvEmail = view.findViewById(R.id.tv_email);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnLogout = view.findViewById(R.id.btn_logout);

        // Configurer les écouteurs de clics
        btnEditProfile.setOnClickListener(v -> navigateToEditProfile());
        btnLogout.setOnClickListener(v -> logout());

        // Charger les informations du profil
        loadProfileInfo();
    }

    private void loadProfileInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Afficher le nom et l'email
            String displayName = user.getDisplayName();
            String email = user.getEmail();

            if (displayName != null && !displayName.isEmpty()) {
                tvName.setText(displayName);
            } else {
                tvName.setText(R.string.user);
            }

            if (email != null && !email.isEmpty()) {
                tvEmail.setText(email);
            }

            // Vous pouvez également charger l'image de profil ici si disponible
        }
    }

    private void navigateToEditProfile() {
        // Naviguer vers l'écran de modification de profil
        if (getActivity() != null) {
            getActivity().startActivity(new Intent(getActivity(), EditProfileActivity.class));
        }
    }

    private void logout() {
        // Déconnexion de Firebase
        FirebaseAuth.getInstance().signOut();

        // Naviguer vers l'écran de connexion
        if (getActivity() != null) {
            getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }
}