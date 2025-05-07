package com.projet.skilllearn.view.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.projet.skilllearn.view.ProfileInfoFragment;
import com.projet.skilllearn.view.UserAchievementsFragment;
import com.projet.skilllearn.view.UserProgressFragment;

public class ProfilePagerAdapter extends FragmentStateAdapter {

    public ProfilePagerAdapter(Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ProfileInfoFragment();
            case 1:
                return new UserProgressFragment();
            case 2:
                return new UserAchievementsFragment();
            default:
                return new ProfileInfoFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Profil, Progrès, Réussites
    }
}