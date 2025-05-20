package com.projet.skilllearn.model;

/**
 * Représente une compétence possédée par un utilisateur avec son niveau
 */
public class UserSkill {
    private String name;
    private String category;
    private double level;  // de 0.0 (débutant) à 10.0 (expert)
    private long lastUpdated;
    private String description;

    /**
     * Constructeur
     * @param name Nom de la compétence
     * @param category Catégorie de la compétence
     */
    public UserSkill(String name, String category) {
        this.name = name;
        this.category = category;
        this.level = 1.0;  // Niveau débutant par défaut
        this.lastUpdated = System.currentTimeMillis();
    }

    /**
     * Constructeur complet
     * @param name Nom de la compétence
     * @param category Catégorie de la compétence
     * @param level Niveau actuel
     * @param description Description
     */
    public UserSkill(String name, String category, double level, String description) {
        this.name = name;
        this.category = category;
        this.level = level;
        this.description = description;
        this.lastUpdated = System.currentTimeMillis();
    }

    /**
     * Récupère le nom de la compétence
     * @return Nom de la compétence
     */
    public String getName() {
        return name;
    }

    /**
     * Définit le nom de la compétence
     * @param name Nouveau nom
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Récupère la catégorie de la compétence
     * @return Catégorie
     */
    public String getCategory() {
        return category;
    }

    /**
     * Définit la catégorie de la compétence
     * @param category Nouvelle catégorie
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Récupère le niveau actuel de la compétence
     * @return Niveau (0.0 à 10.0)
     */
    public double getLevel() {
        return level;
    }

    /**
     * Définit le niveau de la compétence
     * @param level Nouveau niveau
     */
    public void setLevel(double level) {
        this.level = Math.max(0.0, Math.min(10.0, level));
        this.lastUpdated = System.currentTimeMillis();
    }

    /**
     * Incrémente le niveau de la compétence
     * @param increment Valeur d'incrémentation
     */
    public void incrementLevel(double increment) {
        setLevel(this.level + increment);
    }

    /**
     * Récupère la date de dernière mise à jour
     * @return Timestamp de dernière mise à jour
     */
    public long getLastUpdated() {
        return lastUpdated;
    }

    /**
     * Récupère la description de la compétence
     * @return Description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Définit la description de la compétence
     * @param description Nouvelle description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Convertit le niveau numérique en libellé textuel
     * @return Libellé du niveau
     */
    public String getLevelLabel() {
        if (level < 2.0) {
            return "Débutant";
        } else if (level < 4.0) {
            return "Novice";
        } else if (level < 6.0) {
            return "Intermédiaire";
        } else if (level < 8.0) {
            return "Avancé";
        } else {
            return "Expert";
        }
    }
}