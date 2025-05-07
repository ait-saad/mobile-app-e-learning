package com.projet.skilllearn.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.projet.skilllearn.R;
import com.projet.skilllearn.model.Achievement;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.ViewHolder> {

    private final Context context;
    private List<Achievement> achievements;

    public AchievementAdapter(Context context, List<Achievement> achievements) {
        this.context = context;
        this.achievements = achievements;
    }

    public void updateAchievements(List<Achievement> newAchievements) {
        this.achievements = newAchievements;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_achievement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Achievement achievement = achievements.get(position);
        holder.bind(achievement);
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivBadge;
        private final TextView tvTitle;
        private final TextView tvDescription;
        private final TextView tvEarnedDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBadge = itemView.findViewById(R.id.iv_badge);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvEarnedDate = itemView.findViewById(R.id.tv_earned_date);
        }

        public void bind(Achievement achievement) {
            tvTitle.setText(achievement.getTitle());
            tvDescription.setText(achievement.getDescription());

            // Formater la date
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(new Date(achievement.getEarnedAt()));
            tvEarnedDate.setText("Obtenu le " + formattedDate);

            // Charger l'icône du badge
            if (achievement.getIconUrl() != null && !achievement.getIconUrl().isEmpty()) {
                Glide.with(context)
                        .load(achievement.getIconUrl())
                        .placeholder(R.drawable.placeholder_badge)
                        .error(R.drawable.default_badge)
                        .into(ivBadge);
            } else {
                // Utiliser une icône par défaut en fonction du type de succès
                int badgeResId;
                switch (achievement.getType()) {
                    case "course_completion":
                        badgeResId = R.drawable.badge_course_completion;
                        break;
                    case "milestone":
                        badgeResId = R.drawable.badge_milestone;
                        break;
                    case "quiz_master":
                        badgeResId = R.drawable.badge_quiz;
                        break;
                    default:
                        badgeResId = R.drawable.default_badge;
                        break;
                }
                ivBadge.setImageResource(badgeResId);
            }
        }
    }
}