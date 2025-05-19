package com.projet.skilllearn.view.adapters;

import android.content.Context;
import android.util.Log;
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
    private static final String TAG = "AchievementAdapter";

    private final Context context;
    private List<Achievement> achievements;

    public AchievementAdapter(Context context, List<Achievement> achievements) {
        this.context = context;
        this.achievements = achievements;
    }

    public void updateAchievements(List<Achievement> newAchievements) {
        try {
            this.achievements = newAchievements;
            notifyDataSetChanged();
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la mise à jour des achievements", e);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_achievement, parent, false);
            return new ViewHolder(view);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la création du ViewHolder", e);
            // Fallback sur une vue simple en cas d'erreur
            View fallbackView = new View(context);
            fallbackView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            return new ViewHolder(fallbackView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            Achievement achievement = achievements.get(position);
            holder.bind(achievement);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du binding à la position " + position, e);
        }
    }

    @Override
    public int getItemCount() {
        return achievements != null ? achievements.size() : 0;
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
            try {
                if (achievement == null) {
                    Log.e(TAG, "Achievement null lors du binding");
                    return;
                }

                // Vérifier que toutes les vues sont non nulles
                if (tvTitle != null) {
                    tvTitle.setText(achievement.getTitle());
                }

                if (tvDescription != null) {
                    tvDescription.setText(achievement.getDescription());
                }

                // Formater la date
                if (tvEarnedDate != null) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String formattedDate = dateFormat.format(new Date(achievement.getEarnedAt()));
                    tvEarnedDate.setText("Obtenu le " + formattedDate);
                }

                // Charger l'icône du badge
                if (ivBadge != null) {
                    if (achievement.getIconUrl() != null && !achievement.getIconUrl().isEmpty()) {
                        try {
                            Glide.with(context)
                                    .load(achievement.getIconUrl())
                                    .placeholder(R.drawable.placeholder_badge)
                                    .error(R.drawable.default_badge)
                                    .into(ivBadge);
                        } catch (Exception e) {
                            Log.e(TAG, "Erreur lors du chargement de l'icône", e);
                            ivBadge.setImageResource(R.drawable.default_badge);
                        }
                    } else {
                        // Utiliser une icône par défaut en fonction du type de succès
                        int badgeResId;
                        String type = achievement.getType();
                        if (type != null) {
                            switch (type) {
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
                        } else {
                            badgeResId = R.drawable.default_badge;
                        }
                        ivBadge.setImageResource(badgeResId);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Erreur lors du binding de l'achievement", e);
            }
        }
    }
}