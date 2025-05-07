package com.projet.skilllearn.view.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.projet.skilllearn.view.fragments.ContentFragment;
import com.projet.skilllearn.view.fragments.NotesFragment;
import com.projet.skilllearn.view.fragments.QuizFragment;

public class CoursePagerAdapter extends FragmentStateAdapter {

    public CoursePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return ContentFragment.getInstance();
            case 1:
                return NotesFragment.getInstance();
            case 2:
                return QuizFragment.getInstance();
            default:
                return ContentFragment.getInstance();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Contenu, Notes, Quiz
    }
}