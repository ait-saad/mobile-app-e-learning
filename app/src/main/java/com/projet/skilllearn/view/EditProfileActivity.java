package com.projet.skilllearn.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.recyclerview.widget.GridLayoutManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.projet.skilllearn.R;
import com.squareup.picasso.Picasso;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView ivProfilePic;
    private EditText etName;
    private Button btnSaveProfile, btnChangeAvatar;
    private ProgressBar progressBar;

    private FirebaseUser currentUser;
    private int selectedAvatarResId = R.drawable.avatar_1; // Avatar par défaut

    // Tableau des ressources d'avatars disponibles
    private final int[] avatarResources = {
            R.drawable.avatar_1,
            R.drawable.avatar_2,
            R.drawable.avatar_3,
            R.drawable.avatar_4,
            R.drawable.avatar_5
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialiser les vues
        ivProfilePic = findViewById(R.id.iv_profile_pic);
        etName = findViewById(R.id.et_name);
        btnSaveProfile = findViewById(R.id.btn_save_profile);
        btnChangeAvatar = findViewById(R.id.btn_change_avatar);
        progressBar = findViewById(R.id.progress_bar);

        // Obtenir l'utilisateur actuel
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // Afficher les informations actuelles
            if (currentUser.getDisplayName() != null) {
                etName.setText(currentUser.getDisplayName());
            }

            // Charger l'avatar actuellement sélectionné
            loadCurrentAvatar();
        } else {
            // Rediriger vers la page de connexion si l'utilisateur n'est pas connecté
            finish();
        }

        // Configurer les écouteurs de clics
        btnChangeAvatar.setOnClickListener(v -> showAvatarSelectionDialog());

        btnSaveProfile.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                etName.setError("Le nom ne peut pas être vide");
                return;
            }

            updateProfile(name);
        });
    }

    private void loadCurrentAvatar() {
        // Récupérer l'index d'avatar stocké dans Firebase Database
        FirebaseDatabase.getInstance().getReference("users")
                .child(currentUser.getUid())
                .child("avatarIndex")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Integer avatarIndex = snapshot.getValue(Integer.class);
                        if (avatarIndex != null && avatarIndex >= 0 && avatarIndex < avatarResources.length) {
                            selectedAvatarResId = avatarResources[avatarIndex];
                            ivProfilePic.setImageResource(selectedAvatarResId);
                        } else {
                            // Avatar par défaut
                            ivProfilePic.setImageResource(avatarResources[0]);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Gérer l'erreur
                        Toast.makeText(EditProfileActivity.this, "Erreur de chargement: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAvatarSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choisir un avatar");

        // Créer une vue personnalisée avec les avatars
        View view = getLayoutInflater().inflate(R.layout.dialog_avatar_selection, null);
        builder.setView(view);

        RecyclerView rvAvatars = view.findViewById(R.id.rv_avatars);
        rvAvatars.setLayoutManager(new GridLayoutManager(this, 3));

        // Adapter pour la liste d'avatars
        AvatarAdapter adapter = new AvatarAdapter(this, avatarResources, position -> {
            selectedAvatarResId = avatarResources[position];
            ivProfilePic.setImageResource(selectedAvatarResId);
            // Fermer le dialogue
            builder.create().dismiss();
        });

        rvAvatars.setAdapter(adapter);
        builder.create().show();
    }

    private void updateProfile(String name) {
        progressBar.setVisibility(View.VISIBLE);
        btnSaveProfile.setEnabled(false);

        // Mettre à jour le nom dans Firebase Auth
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Trouver l'index de l'avatar sélectionné
                        int avatarIndex = 0;
                        for (int i = 0; i < avatarResources.length; i++) {
                            if (avatarResources[i] == selectedAvatarResId) {
                                avatarIndex = i;
                                break;
                            }
                        }

                        // Stocker l'index d'avatar dans la base de données
                        FirebaseDatabase.getInstance().getReference("users")
                                .child(currentUser.getUid())
                                .child("avatarIndex")
                                .setValue(avatarIndex)
                                .addOnCompleteListener(task2 -> {
                                    progressBar.setVisibility(View.GONE);
                                    btnSaveProfile.setEnabled(true);

                                    if (task2.isSuccessful()) {
                                        Toast.makeText(EditProfileActivity.this, "Profil mis à jour", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(EditProfileActivity.this, "Échec de la mise à jour: " + task2.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnSaveProfile.setEnabled(true);
                        Toast.makeText(EditProfileActivity.this, "Échec de la mise à jour: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Adapter pour la sélection d'avatar
    private static class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder> {

        private final Context context;
        private final int[] avatars;
        private final OnAvatarClickListener listener;

        interface OnAvatarClickListener {
            void onAvatarClick(int position);
        }

        AvatarAdapter(Context context, int[] avatars, OnAvatarClickListener listener) {
            this.context = context;
            this.avatars = avatars;
            this.listener = listener;
        }

        @NonNull
        @Override
        public AvatarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_avatar, parent, false);
            return new AvatarViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AvatarViewHolder holder, int position) {
            holder.ivAvatar.setImageResource(avatars[position]);
            holder.itemView.setOnClickListener(v -> listener.onAvatarClick(position));
        }

        @Override
        public int getItemCount() {
            return avatars.length;
        }

        static class AvatarViewHolder extends RecyclerView.ViewHolder {
            ImageView ivAvatar;

            AvatarViewHolder(@NonNull View itemView) {
                super(itemView);
                ivAvatar = itemView.findViewById(R.id.iv_avatar);
            }
        }
    }
}