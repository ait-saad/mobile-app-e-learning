package com.projet.skilllearn.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.projet.skilllearn.R;
import com.projet.skilllearn.model.CourseSection;

import java.util.List;

public class CourseSectionAdapter extends RecyclerView.Adapter<CourseSectionAdapter.ViewHolder> {

    private final Context context;
    private final List<CourseSection> sections;
    private final OnSectionClickListener listener;
    private int selectedPosition = 0;

    public interface OnSectionClickListener {
        void onSectionClick(int position);
    }

    public CourseSectionAdapter(Context context, List<CourseSection> sections, OnSectionClickListener listener) {
        this.context = context;
        this.sections = sections;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_course_section, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CourseSection section = sections.get(position);
        holder.bind(section, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    public void setSelectedPosition(int position) {
        int oldSelectedPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(oldSelectedPosition);
        notifyItemChanged(selectedPosition);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvDuration;
        private final ImageView ivIcon;
        private final View itemRoot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_section_title);
            tvDuration = itemView.findViewById(R.id.tv_section_duration);
            ivIcon = itemView.findViewById(R.id.iv_section_icon);
            itemRoot = itemView.findViewById(R.id.item_root);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    setSelectedPosition(position);
                    listener.onSectionClick(position);
                }
            });
        }

        public void bind(CourseSection section, boolean isSelected) {
            tvTitle.setText(section.getTitle());

            if (section.getDurationMinutes() > 0) {
                tvDuration.setText(String.format("%d min", section.getDurationMinutes()));
                tvDuration.setVisibility(View.VISIBLE);
            } else {
                tvDuration.setVisibility(View.GONE);
            }

            // Définir l'icône appropriée selon le type de contenu
            if (section.getVideoUrl() != null && !section.getVideoUrl().isEmpty()) {
                ivIcon.setImageResource(R.drawable.ic_video);
            } else if (section.getQuiz() != null) {
                ivIcon.setImageResource(R.drawable.ic_quiz);
            } else {
                ivIcon.setImageResource(R.drawable.ic_text);
            }

            // Mettre en évidence la section sélectionnée
            if (isSelected) {
                itemRoot.setBackgroundResource(R.drawable.selected_section_background);
            } else {
                itemRoot.setBackgroundResource(android.R.color.transparent);
            }
        }
    }
}