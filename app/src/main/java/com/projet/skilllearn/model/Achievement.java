package com.projet.skilllearn.model;

public class Achievement {
    private String id;
    private String title;
    private String iconUrl;
    private String description;
    private String type;
    private long earnedAt;
    private boolean unlocked;

    // Constructeur par défaut (requis pour Firebase)
    public Achievement() {
    }

    // Constructeur avec timestamp
    public Achievement(String id, String title, String description, String type, long earnedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.earnedAt = earnedAt;
        this.unlocked = true; // Si on fournit un timestamp, considérer comme débloqué
    }

    // Constructeur avec unlocked
    public Achievement(String id, String title, String description, String type, boolean unlocked) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.unlocked = unlocked;
        this.earnedAt = unlocked ? System.currentTimeMillis() : 0;
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

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
        if (unlocked && earnedAt == 0) {
            this.earnedAt = System.currentTimeMillis();
        }
    }
    public String getIconUrl() {
        return iconUrl;
    }

    // Ajoutez ce setter pour iconUrl
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}