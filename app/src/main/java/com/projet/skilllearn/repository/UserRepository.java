package com.projet.skilllearn.repository;

import androidx.annotation.NonNull;

import com.projet.skilllearn.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.projet.skilllearn.utils.UserProgressManager;

public class UserRepository {
    private final FirebaseAuth auth;
    private final DatabaseReference usersRef;

    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String message);
    }

    public UserRepository() {
        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    // Modifiez votre méthode registerUser existante
    public void registerUser(String name, String email, String password, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String userId = task.getResult().getUser().getUid();

                        User user = new User(name, email, password);
                        user.setUserId(userId);

                        usersRef.child(userId).setValue(user)
                                .addOnSuccessListener(aVoid -> {
                                    // Initialiser les données de progression pour le nouvel utilisateur
                                    UserProgressManager.getInstance().initializeUserProgress(userId);
                                    callback.onSuccess(user);
                                })
                                .addOnFailureListener(e -> callback.onError(e.getMessage()));
                    } else {
                        callback.onError(task.getException() != null ?
                                task.getException().getMessage() : "Registration failed");
                    }
                });
    }
    public void loginUser(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String userId = task.getResult().getUser().getUid();

                        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User user = snapshot.getValue(User.class);
                                if (user != null) {
                                    callback.onSuccess(user);
                                } else {
                                    callback.onError("User data not found");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                callback.onError(error.getMessage());
                            }
                        });
                    } else {
                        callback.onError(task.getException() != null ?
                                task.getException().getMessage() : "Login failed");
                    }
                });
    }

    public void logoutUser() {
        auth.signOut();
    }

}