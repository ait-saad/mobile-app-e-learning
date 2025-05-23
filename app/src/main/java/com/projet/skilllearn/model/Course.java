package com.projet.skilllearn.model;

import androidx.annotation.NonNull;



import java.util.List;

public class Course {
    private String courseId;
    private String title;
    private String description;
    private String category;
    private String level;
    private int durationMinutes;
    private String imageUrl;
    private String author;
    private int enrolledCount;
    private List<String> tags;
    private long createdAt;
    private long updatedAt;

    // Champs pour le suivi de progression utilisateur
    private int userProgress; // 0-100 pourcentage
    private long lastStudiedTimestamp;
    private boolean isEnrolled;
    private List<CourseSection> sections;
    private String authorName;


    // Constructeur par défaut (requis pour Firebase)
    public Course() {
    }

    // Constructeur principal
    public Course(String courseId, String title, String description, String category,
                  String level, int durationMinutes, String imageUrl, String author) {
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.level = level;
        this.durationMinutes = durationMinutes;
        this.imageUrl = imageUrl;
        this.author = author;
        this.enrolledCount = 0;
    }

    // Getters et Setters
    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getEnrolledCount() {
        return enrolledCount;
    }

    public void setEnrolledCount(int enrolledCount) {
        this.enrolledCount = enrolledCount;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getUserProgress() {
        return userProgress;
    }

    public void setUserProgress(int userProgress) {
        this.userProgress = userProgress;
    }

    public long getLastStudiedTimestamp() {
        return lastStudiedTimestamp;
    }

    public void setLastStudiedTimestamp(long lastStudiedTimestamp) {
        this.lastStudiedTimestamp = lastStudiedTimestamp;
    }

    public boolean isEnrolled() {
        return isEnrolled;
    }

    public void setEnrolled(boolean enrolled) {
        isEnrolled = enrolled;
    }

    public List<CourseSection> getSections() {
        return sections;
    }

    public void setSections(List<CourseSection> sections) {
        this.sections = sections;
    }

}