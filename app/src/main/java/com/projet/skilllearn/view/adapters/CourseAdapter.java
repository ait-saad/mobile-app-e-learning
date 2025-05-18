package com.projet.skilllearn.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.projet.skilllearn.R;
import com.projet.skilllearn.model.Course;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private final Context context;
    private final List<Course> courses;
    private final OnCourseClickListener listener;

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    public CourseAdapter(Context context, List<Course> courses, OnCourseClickListener listener) {
        this.context = context;
        this.courses = courses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.bind(course);
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    class CourseViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final ImageView ivCourseImage;
        private final TextView tvCourseTitle;
        private final TextView tvCourseDescription;
        private final TextView tvCourseDuration;
        private final TextView tvCourseCategory;
        private final TextView tvCourseLevel;
        private final ProgressBar progressBar;
        private final TextView tvProgressPercentage;
        private final TextView tvCourseAuthor;
        private final TextView tvEnrolledCount;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_course);
            ivCourseImage = itemView.findViewById(R.id.iv_course_image);
            tvCourseTitle = itemView.findViewById(R.id.tv_course_title);
            tvCourseDescription = itemView.findViewById(R.id.tv_course_description);
            tvCourseDuration = itemView.findViewById(R.id.tv_course_duration);
            tvCourseCategory = itemView.findViewById(R.id.tv_course_category);
            tvCourseLevel = itemView.findViewById(R.id.tv_course_level);
            progressBar = itemView.findViewById(R.id.progress_bar_course);
            tvProgressPercentage = itemView.findViewById(R.id.tv_progress_percentage);
            tvCourseAuthor = itemView.findViewById(R.id.tv_courses_title);
            tvEnrolledCount = itemView.findViewById(R.id.tv_enrolled_count);

            // Définir le gestionnaire de clics
            cardView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCourseClick(courses.get(position));
                }
            });
        }

        public void bind(Course course) {
            // Définir les informations du cours
            tvCourseTitle.setText(course.getTitle());

            // Limiter la description à 100 caractères
            String description = course.getDescription();
            if (description != null && description.length() > 100) {
                description = description.substring(0, 97) + "...";
            }
            tvCourseDescription.setText(description);

            // Afficher la durée du cours
            tvCourseDuration.setText(String.format("%d min", course.getDurationMinutes()));

            // Afficher la catégorie et le niveau
            tvCourseCategory.setText(course.getCategory());
            tvCourseLevel.setText(course.getLevel());

            // Afficher l'auteur et le nombre d'inscrits
            if (course.getAuthor() != null) {
                tvCourseAuthor.setText(String.format("Par: %s", course.getAuthor()));
                tvCourseAuthor.setVisibility(View.VISIBLE);
            } else {
                tvCourseAuthor.setVisibility(View.GONE);
            }

            if (course.getEnrolledCount() > 0) {
                tvEnrolledCount.setText(String.format("%d inscrits", course.getEnrolledCount()));
                tvEnrolledCount.setVisibility(View.VISIBLE);
            } else {
                tvEnrolledCount.setVisibility(View.GONE);
            }

            // Afficher la progression si disponible
            int progress = course.getUserProgress();
            if (progress > 0) {
                progressBar.setProgress(progress);
                tvProgressPercentage.setText(String.format("%d%%", progress));
                progressBar.setVisibility(View.VISIBLE);
                tvProgressPercentage.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
                tvProgressPercentage.setVisibility(View.GONE);
            }

            // Charger l'image du cours avec Glide
            if (course.getImageUrl() != null && !course.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(course.getImageUrl())
                        .placeholder(R.drawable.placeholder_course)
                        .error(R.drawable.error_course)
                        .centerCrop()
                        .into(ivCourseImage);
            } else {
                // Image par défaut si aucune URL d'image n'est disponible
                ivCourseImage.setImageResource(R.drawable.placeholder_course);
            }
        }
    }
}