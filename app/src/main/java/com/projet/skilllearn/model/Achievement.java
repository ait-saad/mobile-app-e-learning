package com.projet.skilllearn.model;

/**
 * Modèle pour les succès (badges)
 */
public class Achievement {
    private String id;
    private String title;
    private String description;
    private String type;
    private long earnedAt;
    private String iconUrl;

    /**
     * Constructeur par défaut (requis pour Firebase)
     */
    public Achievement() {
    }

    /**
     * Constructeur avec paramètres
     * @param id ID du succès
     * @param title Titre du succès
     * @param description Description du succès
     * @param type Type de succès (course_completion, milestone, etc.)
     * @param earnedAt Timestamp de l'obtention
     */
    public Achievement(String id, String title, String description, String type, long earnedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.earnedAt = earnedAt;
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getEarnedAt() {
        return earnedAt;
    }

    public void setEarnedAt(long earnedAt) {
        this.earnedAt = earnedAt;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}