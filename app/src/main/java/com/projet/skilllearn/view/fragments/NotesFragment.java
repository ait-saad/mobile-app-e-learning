package com.projet.skilllearn.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.projet.skilllearn.R;
import com.projet.skilllearn.model.Note;
import com.projet.skilllearn.view.adapters.NoteAdapter;

import java.util.ArrayList;
import java.util.List;

public class NotesFragment extends Fragment {
    private static NotesFragment instance;
    private RecyclerView rvNotes;
    private EditText etNewNote;
    private Button btnAddNote;
    private List<Note> notes = new ArrayList<>();
    private NoteAdapter adapter;
    private String courseId;
    private String sectionId;

    public static NotesFragment getInstance() {
        if (instance == null) {
            instance = new NotesFragment();
        }
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvNotes = view.findViewById(R.id.rv_notes);
        etNewNote = view.findViewById(R.id.et_new_note);
        btnAddNote = view.findViewById(R.id.btn_add_note);

        // Configurer RecyclerView
        adapter = new NoteAdapter(requireContext(), notes);
        rvNotes.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvNotes.setAdapter(adapter);

        // Configurer le bouton d'ajout de note
        btnAddNote.setOnClickListener(v -> addNewNote());

        // Récupérer les IDs depuis l'activité parente
        if (getActivity() != null) {
            courseId = getActivity().getIntent().getStringExtra("courseId");
            sectionId = getActivity().getIntent().getStringExtra("sectionId");

            // Charger les notes
            if (courseId != null && sectionId != null) {
                loadNotes();
            }
        }
    }

    public void updateSection(String courseId, String sectionId) {
        this.courseId = courseId;
        this.sectionId = sectionId;
        loadNotes();
    }

    private void loadNotes() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(requireContext(), "Connectez-vous pour voir vos notes", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference notesRef = FirebaseDatabase.getInstance().getReference("notes")
                .child(userId).child(courseId).child(sectionId);

        notesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notes.clear();
                for (DataSnapshot noteSnapshot : snapshot.getChildren()) {
                    Note note = noteSnapshot.getValue(Note.class);
                    if (note != null) {
                        note.setId(noteSnapshot.getKey());
                        notes.add(note);
                    }
                }
                adapter.updateNotes(notes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Erreur: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addNewNote() {
        String noteText = etNewNote.getText().toString().trim();
        if (noteText.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez saisir une note", Toast.LENGTH_SHORT).show();
            return;
        }

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(requireContext(), "Connectez-vous pour ajouter des notes", Toast.LENGTH_SHORT).show();
            return;
        }

        if (courseId == null || sectionId == null) {
            Toast.makeText(requireContext(), "Erreur: informations manquantes", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference notesRef = FirebaseDatabase.getInstance().getReference("notes")
                .child(userId).child(courseId).child(sectionId);

        // Créer une nouvelle note
        Note newNote = new Note(null, noteText, System.currentTimeMillis());

        // Ajouter à Firebase
        DatabaseReference newNoteRef = notesRef.push();
        newNoteRef.setValue(newNote).addOnSuccessListener(unused -> {
            etNewNote.setText("");
            Toast.makeText(requireContext(), "Note ajoutée", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}