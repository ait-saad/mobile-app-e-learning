package com.projet.skilllearn.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente les préférences d'apprentissage d'un utilisateur
 */
public class LearningPreference {
    private String userId;
    private List<String> preferredCategories;
    private List<String> preferredTags;
    private String preferredLearningStyle; // visuel, auditif, pratique, etc.
    private int preferredSessionDuration; // durée préférée en minutes
    private String preferredDifficulty; // débutant, intermédiaire, avancé
    private boolean preferNotifications;
    private List<Integer> preferredLearningDays; // jours de la semaine (1-7)
    private List<TimeRange> preferredLearningTimes; // plages horaires préférées

    /**
     * Constructeur
     * @param userId ID de l'utilisateur
     */
    public LearningPreference(String userId) {
        this.userId = userId;
        this.preferredCategories = new ArrayList<>();
        this.preferredTags = new ArrayList<>();
        this.preferredLearningStyle = "visuel";
        this.preferredSessionDuration = 30;
        this.preferredDifficulty = "intermédiaire";
        this.preferNotifications = true;
        this.preferredLearningDays = new ArrayList<>();
        this.preferredLearningTimes = new ArrayList<>();

        // Valeurs par défaut
        for (int i = 1; i <= 7; i++) {
            preferredLearningDays.add(i);
        }
    }

    // Getters & Setters

    public String getUserId() {
        return userId;
    }

    public List<String> getPreferredCategories() {
        return preferredCategories;
    }

    public void setPreferredCategories(List<String> preferredCategories) {
        this.preferredCategories = preferredCategories;
    }

    public void addPreferredCategory(String category) {
        if (!preferredCategories.contains(category)) {
            preferredCategories.add(category);
        }
    }

    public List<String> getPreferredTags() {
        return preferredTags;
    }

    public void setPreferredTags(List<String> preferredTags) {
        this.preferredTags = preferredTags;
    }

    public void addPreferredTag(String tag) {
        if (!preferredTags.contains(tag)) {
            preferredTags.add(tag);
        }
    }

    public String getPreferredLearningStyle() {
        return preferredLearningStyle;
    }

    public void setPreferredLearningStyle(String preferredLearningStyle) {
        this.preferredLearningStyle = preferredLearningStyle;
    }

    public int getPreferredSessionDuration() {
        return preferredSessionDuration;
    }

    public void setPreferredSessionDuration(int preferredSessionDuration) {
        this.preferredSessionDuration = preferredSessionDuration;
    }

    public String getPreferredDifficulty() {
        return preferredDifficulty;
    }

    public void setPreferredDifficulty(String preferredDifficulty) {
        this.preferredDifficulty = preferredDifficulty;
    }

    public boolean isPreferNotifications() {
        return preferNotifications;
    }

    public void setPreferNotifications(boolean preferNotifications) {
        this.preferNotifications = preferNotifications;
    }

    public List<Integer> getPreferredLearningDays() {
        return preferredLearningDays;
    }

    public void setPreferredLearningDays(List<Integer> preferredLearningDays) {
        this.preferredLearningDays = preferredLearningDays;
    }

    public List<TimeRange> getPreferredLearningTimes() {
        return preferredLearningTimes;
    }

    public void setPreferredLearningTimes(List<TimeRange> preferredLearningTimes) {
        this.preferredLearningTimes = preferredLearningTimes;
    }

    public void addPreferredLearningTime(TimeRange timeRange) {
        this.preferredLearningTimes.add(timeRange);
    }

    /**
     * Classe imbriquée représentant une plage horaire
     */
    public static class TimeRange {
        private int startHour;
        private int startMinute;
        private int endHour;
        private int endMinute;

        public TimeRange(int startHour, int startMinute, int endHour, int endMinute) {
            this.startHour = startHour;
            this.startMinute = startMinute;
            this.endHour = endHour;
            this.endMinute = endMinute;
        }

        public int getStartHour() {
            return startHour;
        }

        public int getStartMinute() {
            return startMinute;
        }

        public int getEndHour() {
            return endHour;
        }

        public int getEndMinute() {
            return endMinute;
        }

        @Override
        public String toString() {
            return String.format("%02d:%02d - %02d:%02d", startHour, startMinute, endHour, endMinute);
        }
    }
}