package com.projet.skilllearn.view.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.projet.skilllearn.R;
import com.projet.skilllearn.model.Course;
import com.projet.skilllearn.view.CourseDetailActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    // Interface pour le clic sur un cours
    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    private final Context context;
    private final List<Course> courses;
    private OnCourseClickListener listener;

    // Constructeur avec listener
    public CourseAdapter(Context context, List<Course> courses, OnCourseClickListener listener) {
        this.context = context;
        this.courses = courses;
        this.listener = listener;
    }

    // Constructeur sans listener (pour la compatibilité)
    public CourseAdapter(Context context, List<Course> courses) {
        this.context = context;
        this.courses = courses;
        this.listener = null;
    }

    // Méthode pour mettre à jour les cours
    public void updateCourses(List<Course> newCourses) {
        this.courses.clear();
        if (newCourses != null) {
            this.courses.addAll(newCourses);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courses.get(position);

        holder.tvTitle.setText(course.getTitle());
        holder.tvDescription.setText(course.getDescription());

        // Utiliser getDurationMinutes()
        holder.tvDuration.setText(String.format(context.getString(R.string.duration_format), course.getDurationMinutes()));

        // Utiliser getImageUrl()
        if (course.getImageUrl() != null && !course.getImageUrl().isEmpty()) {
            Picasso.get().load(course.getImageUrl())
                    .placeholder(R.drawable.placeholder_course)
                    .error(R.drawable.error_course)
                    .into(holder.ivThumbnail);
        } else {
            holder.ivThumbnail.setImageResource(R.drawable.placeholder_course);
        }

        // Ajouter le gestionnaire de clic
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Si un listener est fourni, l'utiliser
                listener.onCourseClick(course);
            } else {
                // Sinon, utiliser le comportement par défaut
                try {
                    Intent intent = new Intent(context, CourseDetailActivity.class);
                    intent.putExtra("courseId", course.getCourseId());
                    context.startActivity(intent);
                } catch (Exception e) {
                    // Gérer l'erreur silencieusement
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvTitle, tvDescription, tvDuration;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.iv_course_image);
            tvTitle = itemView.findViewById(R.id.tv_course_title);
            tvDescription = itemView.findViewById(R.id.tv_course_description);
            tvDuration = itemView.findViewById(R.id.tv_course_duration);
        }
    }
}