package com.projet.skilllearn.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Représente les performances d'un utilisateur dans une catégorie spécifique
 */
public class UserPerformance {
    private String userId;
    private String category;
    private double overallLevel;
    private Map<String, Double> skillLevels;
    private Map<String, Integer> revisionCounts;
    private Map<String, Long> lastRevisionTimes;
    private Map<String, Integer> quizAttempts;
    private Map<String, Double> quizAverageScores;
    private long totalLearningTimeMs;
    private int completedSections;

    /**
     * Constructeur
     * @param userId ID de l'utilisateur
     * @param category Catégorie d'apprentissage
     */
    public UserPerformance(String userId, String category) {
        this.userId = userId;
        this.category = category;
        this.overallLevel = 0.5; // Niveau initial
        this.skillLevels = new HashMap<>();
        this.revisionCounts = new HashMap<>();
        this.lastRevisionTimes = new HashMap<>();
        this.quizAttempts = new HashMap<>();
        this.quizAverageScores = new HashMap<>();
        this.totalLearningTimeMs = 0;
        this.completedSections = 0;
    }

    /**
     * Obtient le niveau de compétence pour une aptitude spécifique
     * @param skill Nom de la compétence
     * @return Niveau de compétence (0.0 à 1.0)
     */
    public double getSkillLevel(String skill) {
        if (!skillLevels.containsKey(skill)) {
            skillLevels.put(skill, 0.5); // Niveau initial pour une nouvelle compétence
        }
        return skillLevels.get(skill);
    }

    /**
     * Définit le niveau de compétence pour une aptitude
     * @param skill Nom de la compétence
     * @param level Nouveau niveau
     */
    public void setSkillLevel(String skill, double level) {
        skillLevels.put(skill, level);
    }

    /**
     * Récupère le nombre de révisions pour une compétence
     * @param skill Nom de la compétence
     * @return Nombre de révisions
     */
    public int getRevisionCount(String skill) {
        if (!revisionCounts.containsKey(skill)) {
            revisionCounts.put(skill, 0);
        }
        return revisionCounts.get(skill);
    }

    /**
     * Incrémente le nombre de révisions pour une compétence
     * @param skill Nom de la compétence
     */
    public void incrementRevisionCount(String skill) {
        int count = getRevisionCount(skill);
        revisionCounts.put(skill, count + 1);
        lastRevisionTimes.put(skill, System.currentTimeMillis());
    }

    /**
     * Récupère la date de dernière révision pour une compétence
     * @param skill Nom de la compétence
     * @return Timestamp de la dernière révision, ou 0 si jamais révisée
     */
    public long getLastRevisionTime(String skill) {
        if (!lastRevisionTimes.containsKey(skill)) {
            return 0;
        }
        return lastRevisionTimes.get(skill);
    }

    /**
     * Définit le niveau global pour la catégorie
     * @param level Nouveau niveau global
     */
    public void setOverallLevel(double level) {
        this.overallLevel = level;
    }

    /**
     * Récupère le niveau global pour la catégorie
     * @return Niveau global
     */
    public double getOverallLevel() {
        return overallLevel;
    }

    /**
     * Récupère tous les niveaux de compétence
     * @return Map des compétences et leurs niveaux
     */
    public Map<String, Double> getAllSkillLevels() {
        return new HashMap<>(skillLevels);
    }

    /**
     * Récupère l'ID de l'utilisateur
     * @return ID utilisateur
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Récupère la catégorie
     * @return Catégorie
     */
    public String getCategory() {
        return category;
    }

    /**
     * Enregistre une tentative de quiz
     * @param quizId ID du quiz
     * @param score Score obtenu (pourcentage)
     */
    public void recordQuizAttempt(String quizId, double score) {
        // Mettre à jour le nombre de tentatives
        int attempts = 1;
        if (quizAttempts.containsKey(quizId)) {
            attempts = quizAttempts.get(quizId) + 1;
        }
        quizAttempts.put(quizId, attempts);

        // Mettre à jour le score moyen
        double currentAverage = 0;
        if (quizAverageScores.containsKey(quizId)) {
            currentAverage = quizAverageScores.get(quizId);
        }

        // Calculer la nouvelle moyenne pondérée
        double newAverage;
        if (attempts == 1) {
            newAverage = score;
        } else {
            // Plus de poids aux tentatives récentes
            newAverage = (currentAverage * (attempts - 1) * 0.7 + score * 0.3 * attempts) / attempts;
        }

        quizAverageScores.put(quizId, newAverage);
    }

    /**
     * Obtient le nombre de tentatives pour un quiz donné
     * @param quizId ID du quiz
     * @return Nombre de tentatives
     */
    public int getQuizAttempts(String quizId) {
        if (!quizAttempts.containsKey(quizId)) {
            return 0;
        }
        return quizAttempts.get(quizId);
    }

    /**
     * Obtient le score moyen pour un quiz donné
     * @param quizId ID du quiz
     * @return Score moyen
     */
    public double getQuizAverageScore(String quizId) {
        if (!quizAverageScores.containsKey(quizId)) {
            return 0;
        }
        return quizAverageScores.get(quizId);
    }

    /**
     * Ajoute du temps d'apprentissage à la statistique globale
     * @param timeMs Temps en millisecondes
     */
    public void addLearningTime(long timeMs) {
        this.totalLearningTimeMs += timeMs;
    }

    /**
     * Récupère le temps total d'apprentissage
     * @return Temps total en millisecondes
     */
    public long getTotalLearningTime() {
        return totalLearningTimeMs;
    }

    /**
     * Incrémente le compteur de sections complétées
     */
    public void incrementCompletedSections() {
        this.completedSections++;
    }

    /**
     * Obtient le nombre de sections complétées
     * @return Nombre de sections
     */
    public int getCompletedSections() {
        return completedSections;
    }

    /**
     * Calcule la moyenne des scores de tous les quiz
     * @return Score moyen global
     */
    public double getAverageQuizScore() {
        if (quizAverageScores.isEmpty()) {
            return 0;
        }

        double sum = 0;
        for (Double score : quizAverageScores.values()) {
            sum += score;
        }

        return sum / quizAverageScores.size();
    }

    /**
     * Calcule le taux de succès aux quiz (pourcentage de quiz réussis avec un score >= 70%)
     * @return Taux de succès
     */
    public double getQuizSuccessRate() {
        if (quizAverageScores.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (Double score : quizAverageScores.values()) {
            if (score >= 70.0) {
                successCount++;
            }
        }

        return (double) successCount / quizAverageScores.size() * 100;
    }

    /**
     * Vérifie si l'utilisateur est assidu dans ses révisions
     * @param maxDaysBetweenRevisions Nombre maximum de jours entre les révisions
     * @return true si l'utilisateur est assidu
     */
    public boolean isConsistentLearner(int maxDaysBetweenRevisions) {
        if (lastRevisionTimes.isEmpty()) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        long maxInterval = maxDaysBetweenRevisions * 24 * 60 * 60 * 1000L;

        for (Long lastTime : lastRevisionTimes.values()) {
            if (currentTime - lastTime > maxInterval) {
                return false;
            }
        }

        return true;
    }

    /**
     * Identifie les compétences qui nécessitent une révision
     * @param daysThreshold Seuil en jours depuis la dernière révision
     * @return Liste des compétences à réviser
     */
    public Map<String, Long> getSkillsNeedingRevision(int daysThreshold) {
        Map<String, Long> skillsToRevise = new HashMap<>();
        long currentTime = System.currentTimeMillis();
        long threshold = daysThreshold * 24 * 60 * 60 * 1000L;

        for (Map.Entry<String, Long> entry : lastRevisionTimes.entrySet()) {
            String skill = entry.getKey();
            long lastTime = entry.getValue();

            if (currentTime - lastTime > threshold) {
                skillsToRevise.put(skill, (currentTime - lastTime) / (24 * 60 * 60 * 1000L));
            }
        }

        return skillsToRevise;
    }
}