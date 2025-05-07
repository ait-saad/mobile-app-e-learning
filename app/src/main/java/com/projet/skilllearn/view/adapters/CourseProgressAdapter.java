package com.projet.skilllearn.view.adapters;

import android.content.Context;
import android.content.Intent;
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
import com.projet.skilllearn.view.CourseDetailActivity;

import java.util.List;

public class CourseProgressAdapter extends RecyclerView.Adapter<CourseProgressAdapter.ViewHolder> {

    private final Context context;
    private List<Course> courses;

    public CourseProgressAdapter(Context context, List<Course> courses) {
        this.context = context;
        this.courses = courses;
    }

    public void updateCourses(List<Course> newCourses) {
        this.courses = newCourses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course_progress, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.bind(course);
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final ImageView ivCourseImage;
        private final TextView tvCourseTitle;
        private final ProgressBar progressBar;
        private final TextView tvProgressPercentage;
        private final TextView tvLastStudied;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_course_progress);
            ivCourseImage = itemView.findViewById(R.id.iv_course_image);
            tvCourseTitle = itemView.findViewById(R.id.tv_course_title);
            progressBar = itemView.findViewById(R.id.progress_bar);
            tvProgressPercentage = itemView.findViewById(R.id.tv_progress_percentage);
            tvLastStudied = itemView.findViewById(R.id.tv_last_studied);

            cardView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Course course = courses.get(position);
                    Intent intent = new Intent(context, CourseDetailActivity.class);
                    intent.putExtra("courseId", course.getCourseId());
                    context.startActivity(intent);
                }
            });
        }

        public void bind(Course course) {
            tvCourseTitle.setText(course.getTitle());
            progressBar.setProgress(course.getUserProgress());
            tvProgressPercentage.setText(course.getUserProgress() + "%");

            if (course.getLastStudiedTimestamp() > 0) {
                tvLastStudied.setText(formatLastStudiedDate(course.getLastStudiedTimestamp()));
                tvLastStudied.setVisibility(View.VISIBLE);
            } else {
                tvLastStudied.setVisibility(View.GONE);
            }

            if (course.getImageUrl() != null && !course.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(course.getImageUrl())
                        .placeholder(R.drawable.placeholder_course)
                        .error(R.drawable.error_course)
                        .centerCrop()
                        .into(ivCourseImage);
            } else {
                ivCourseImage.setImageResource(R.drawable.placeholder_course);
            }
        }

        private String formatLastStudiedDate(long timestamp) {
            // Formater la date en "il y a X jours/heures/minutes"
            long now = System.currentTimeMillis();
            long diff = now - timestamp;

            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (days > 0) {
                return "Dernière étude il y a " + days + (days == 1 ? " jour" : " jours");
            } else if (hours > 0) {
                return "Dernière étude il y a " + hours + (hours == 1 ? " heure" : " heures");
            } else {
                return "Dernière étude il y a " + (minutes == 0 ? 1 : minutes) + (minutes == 1 ? " minute" : " minutes");
            }
        }
    }
}