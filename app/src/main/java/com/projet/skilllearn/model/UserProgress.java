package com.projet.skilllearn.model;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe représentant la progression d'un utilisateur dans un cours spécifique
 */
public class UserProgress {
    private String userId;
    private String courseId;
    private int percentage;
    private long enrolledAt;
    private long lastUpdated;
    private Map<String, SectionProgress> sections;
    private List<QuizAttempt> quizAttempts;
    private long totalTimeSpent; // en millisecondes
    private List<String> completedActivities;
    private Map<String, Integer> skillProgress;
    private boolean certificateEarned;
    private long certificateEarnedAt;
    private String userNotes;
    private int bookmarks; // nombre de signets dans le cours
    private List<Double> progressHistory; // historique des pourcentages de progression pour analyse
    private long lastAccessedTimestamp;
    /**
     * Constructeur par défaut (requis pour Firebase)
     */
    public UserProgress() {
        // Constructeur par défaut requis pour la désérialisation Firebase
    }

    /**
     * Constructeur principal
     * @param userId ID de l'utilisateur
     * @param courseId ID du cours
     */
    public UserProgress(String userId, String courseId) {
        this.userId = userId;
        this.courseId = courseId;
        this.percentage = 0;
        this.enrolledAt = System.currentTimeMillis();
        this.lastUpdated = System.currentTimeMillis();
        this.sections = new HashMap<>();
        this.quizAttempts = new ArrayList<>();
        this.totalTimeSpent = 0;
        this.completedActivities = new ArrayList<>();
        this.skillProgress = new HashMap<>();
        this.certificateEarned = false;
        this.userNotes = "";
        this.bookmarks = 0;
        this.progressHistory = new ArrayList<>();
        this.progressHistory.add(0.0); // Point de départ
    }

    /**
     * Classe imbriquée pour représenter la progression dans une section
     */
    public static class SectionProgress {
        private boolean completed;
        private long completedAt;
        private long timeSpent; // en millisecondes
        private double comprehensionScore; // score de compréhension entre 0-100
        private List<String> completedSubSections;
        private Map<String, Object> metadata; // données supplémentaires

        // Constructeur par défaut
        public SectionProgress() {
            this.completed = false;
            this.completedAt = 0;
            this.timeSpent = 0;
            this.comprehensionScore = 0;
            this.completedSubSections = new ArrayList<>();
            this.metadata = new HashMap<>();
        }

        // Getters et Setters
        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
            if (completed) {
                this.completedAt = System.currentTimeMillis();
            }
        }

        public long getCompletedAt() {
            return completedAt;
        }

        public void setCompletedAt(long completedAt) {
            this.completedAt = completedAt;
        }

        public long getTimeSpent() {
            return timeSpent;
        }

        public void addTimeSpent(long additionalTimeMs) {
            this.timeSpent += additionalTimeMs;
        }

        public double getComprehensionScore() {
            return comprehensionScore;
        }

        public void setComprehensionScore(double comprehensionScore) {
            this.comprehensionScore = comprehensionScore;
        }

        public List<String> getCompletedSubSections() {
            return completedSubSections;
        }

        public void addCompletedSubSection(String subSectionId) {
            if (!completedSubSections.contains(subSectionId)) {
                completedSubSections.add(subSectionId);
            }
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void addMetadata(String key, Object value) {
            metadata.put(key, value);
        }
    }

    /**
     * Classe imbriquée pour représenter une tentative de quiz
     */
    public static class QuizAttempt {
        private String quizId;
        private int score;
        private long completedAt;
        private int attemptNumber;
        private long timeSpent; // en millisecondes
        private List<Integer> answeredQuestions;
        private Map<String, String> responses; // questionId -> réponse

        // Constructeur par défaut
        public QuizAttempt() {
            // Requis pour Firebase
        }

        public QuizAttempt(String quizId, int score, int attemptNumber, long timeSpent) {
            this.quizId = quizId;
            this.score = score;
            this.completedAt = System.currentTimeMillis();
            this.attemptNumber = attemptNumber;
            this.timeSpent = timeSpent;
            this.answeredQuestions = new ArrayList<>();
            this.responses = new HashMap<>();
        }

        // Getters et Setters
        public String getQuizId() {
            return quizId;
        }

        public void setQuizId(String quizId) {
            this.quizId = quizId;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public long getCompletedAt() {
            return completedAt;
        }

        public void setCompletedAt(long completedAt) {
            this.completedAt = completedAt;
        }

        public int getAttemptNumber() {
            return attemptNumber;
        }

        public void setAttemptNumber(int attemptNumber) {
            this.attemptNumber = attemptNumber;
        }

        public long getTimeSpent() {
            return timeSpent;
        }

        public void setTimeSpent(long timeSpent) {
            this.timeSpent = timeSpent;
        }

        public List<Integer> getAnsweredQuestions() {
            return answeredQuestions;
        }

        public void addAnsweredQuestion(int questionIndex) {
            this.answeredQuestions.add(questionIndex);
        }

        public Map<String, String> getResponses() {
            return responses;
        }

        public void addResponse(String questionId, String response) {
            this.responses.put(questionId, response);
        }
    }

    // Méthodes pour gérer les sections

    /**
     * Marque une section comme complétée
     * @param sectionId Identifiant de la section
     * @param comprehensionScore Score de compréhension (facultatif)
     */
    public void completeSectionWithScore(String sectionId, double comprehensionScore) {
        SectionProgress sectionProgress = getOrCreateSectionProgress(sectionId);
        sectionProgress.setCompleted(true);
        sectionProgress.setComprehensionScore(comprehensionScore);
        updateOverallProgress();
        this.lastUpdated = System.currentTimeMillis();

        // Ajouter à l'historique de progression
        updateProgressHistory();
    }

    /**
     * Marque une section comme complétée
     * @param sectionId Identifiant de la section
     */
    public void completeSection(String sectionId) {
        completeSectionWithScore(sectionId, 100.0);
    }

    /**
     * Obtient ou crée une progression de section
     * @param sectionId Identifiant de la section
     * @return Objet SectionProgress
     */
    public SectionProgress getOrCreateSectionProgress(String sectionId) {
        if (!sections.containsKey(sectionId)) {
            sections.put(sectionId, new SectionProgress());
        }
        return sections.get(sectionId);
    }

    /**
     * Ajoute du temps à une section
     * @param sectionId Identifiant de la section
     * @param timeMs Temps passé en millisecondes
     */
    public void addTimeToSection(String sectionId, long timeMs) {
        SectionProgress sectionProgress = getOrCreateSectionProgress(sectionId);
        sectionProgress.addTimeSpent(timeMs);
        this.totalTimeSpent += timeMs;
        this.lastUpdated = System.currentTimeMillis();
    }

    /**
     * Vérifie si une section est complétée
     * @param sectionId Identifiant de la section
     * @return true si la section est complétée
     */
    public boolean isSectionCompleted(String sectionId) {
        if (!sections.containsKey(sectionId)) {
            return false;
        }
        return sections.get(sectionId).isCompleted();
    }

    // Méthodes pour gérer les tentatives de quiz

    /**
     * Ajoute une tentative de quiz
     * @param quizId Identifiant du quiz
     * @param score Score obtenu
     * @param timeSpent Temps passé en millisecondes
     */
    public void addQuizAttempt(String quizId, int score, long timeSpent) {
        int attemptNumber = 1;
        for (QuizAttempt attempt : quizAttempts) {
            if (attempt.getQuizId().equals(quizId)) {
                attemptNumber++;
            }
        }

        QuizAttempt attempt = new QuizAttempt(quizId, score, attemptNumber, timeSpent);
        quizAttempts.add(attempt);

        // Ajouter le quiz aux activités complétées s'il est réussi (score > 70%)
        if (score >= 70) {
            addCompletedActivity("quiz_" + quizId);
        }

        this.totalTimeSpent += timeSpent;
        this.lastUpdated = System.currentTimeMillis();

        // Mettre à jour le progrès global
        updateOverallProgress();
    }

    /**
     * Obtient la meilleure tentative de quiz
     * @param quizId Identifiant du quiz
     * @return Meilleure tentative ou null si aucune
     */
    public QuizAttempt getBestQuizAttempt(String quizId) {
        QuizAttempt bestAttempt = null;
        int bestScore = -1;

        for (QuizAttempt attempt : quizAttempts) {
            if (attempt.getQuizId().equals(quizId) && attempt.getScore() > bestScore) {
                bestScore = attempt.getScore();
                bestAttempt = attempt;
            }
        }

        return bestAttempt;
    }

    /**
     * Obtient la dernière tentative de quiz
     * @param quizId Identifiant du quiz
     * @return Dernière tentative ou null si aucune
     */
    public QuizAttempt getLastQuizAttempt(String quizId) {
        QuizAttempt lastAttempt = null;
        long lastTime = 0;

        for (QuizAttempt attempt : quizAttempts) {
            if (attempt.getQuizId().equals(quizId) && attempt.getCompletedAt() > lastTime) {
                lastTime = attempt.getCompletedAt();
                lastAttempt = attempt;
            }
        }

        return lastAttempt;
    }

    /**
     * Obtient toutes les tentatives pour un quiz spécifique
     * @param quizId Identifiant du quiz
     * @return Liste des tentatives
     */
    public List<QuizAttempt> getQuizAttempts(String quizId) {
        List<QuizAttempt> attempts = new ArrayList<>();

        for (QuizAttempt attempt : quizAttempts) {
            if (attempt.getQuizId().equals(quizId)) {
                attempts.add(attempt);
            }
        }

        return attempts;
    }

    // Méthodes pour la gestion des activités et compétences

    /**
     * Ajoute une activité complétée
     * @param activityId Identifiant de l'activité
     */
    public void addCompletedActivity(String activityId) {
        if (!completedActivities.contains(activityId)) {
            completedActivities.add(activityId);
            updateOverallProgress();
        }
    }

    /**
     * Met à jour une compétence
     * @param skillId Identifiant de la compétence
     * @param level Niveau de compétence (0-100)
     */
    public void updateSkill(String skillId, int level) {
        skillProgress.put(skillId, level);
    }

    /**
     * Obtient le niveau d'une compétence
     * @param skillId Identifiant de la compétence
     * @return Niveau de compétence ou 0 si non défini
     */
    public int getSkillLevel(String skillId) {
        return skillProgress.getOrDefault(skillId, 0);
    }

    /**
     * Met à jour le pourcentage global de progression
     */
    private void updateOverallProgress() {
        // Cette logique dépend de comment vous calculez la progression globale
        // Exemple simple: basé sur le nombre de sections complétées
        int completedCount = 0;
        for (SectionProgress progress : sections.values()) {
            if (progress.isCompleted()) {
                completedCount++;
            }
        }

        // Si aucune section n'est définie, garder à 0%
        if (sections.isEmpty()) {
            this.percentage = 0;
        } else {
            this.percentage = (int) ((double) completedCount / sections.size() * 100);
        }

        // Vérifier si le certificat est gagné (100% de progression)
        if (this.percentage == 100 && !this.certificateEarned) {
            this.certificateEarned = true;
            this.certificateEarnedAt = System.currentTimeMillis();
        }

        // Mise à jour de l'historique de progression
        updateProgressHistory();
    }

    /**
     * Met à jour l'historique de progression
     */
    private void updateProgressHistory() {
        // Ajouter le pourcentage actuel à l'historique
        this.progressHistory.add((double) this.percentage);
    }

    /**
     * Ajoute un signet dans le cours
     */
    public void addBookmark() {
        this.bookmarks++;
    }

    /**
     * Enlève un signet du cours
     */
    public void removeBookmark() {
        if (this.bookmarks > 0) {
            this.bookmarks--;
        }
    }

    /**
     * Ajoute ou met à jour une note utilisateur
     * @param note Texte de la note
     */
    public void updateUserNote(String note) {
        this.userNotes = note;
        this.lastUpdated = System.currentTimeMillis();
    }

    /**
     * Calcule le taux de progression par jour
     * @return Pourcentage de progression par jour depuis l'inscription
     */
    public double getDailyProgressRate() {
        long daysEnrolled = getDaysEnrolled();
        if (daysEnrolled == 0) {
            return 0.0;
        }
        return (double) percentage / daysEnrolled;
    }

    /**
     * Calcule le nombre de jours depuis l'inscription
     * @return Nombre de jours
     */
    public long getDaysEnrolled() {
        long millisPerDay = 24 * 60 * 60 * 1000;
        return (System.currentTimeMillis() - enrolledAt) / millisPerDay;
    }

    /**
     * Calcule le temps moyen par session
     * @return Temps moyen en millisecondes
     */
    public long getAverageSessionTime() {
        if (completedActivities.size() == 0) {
            return 0;
        }
        return totalTimeSpent / completedActivities.size();
    }

    /**
     * Prédit la date d'achèvement du cours en se basant sur le taux de progression actuel
     * @return Timestamp de la date prévue d'achèvement, ou 0 si impossible à prédire
     */
    public long predictCompletionDate() {
        double dailyRate = getDailyProgressRate();
        if (dailyRate <= 0) {
            return 0; // Impossible de prédire
        }

        double daysRemaining = (100 - percentage) / dailyRate;
        return System.currentTimeMillis() + (long)(daysRemaining * 24 * 60 * 60 * 1000);
    }

    /**
     * Calcule le score moyen des quiz
     * @return Score moyen ou 0 si aucun quiz
     */
    public double getAverageQuizScore() {
        if (quizAttempts.isEmpty()) {
            return 0;
        }

        int totalScore = 0;
        for (QuizAttempt attempt : quizAttempts) {
            totalScore += attempt.getScore();
        }

        return (double) totalScore / quizAttempts.size();
    }

    /**
     * Détermine si l'utilisateur est actif récemment
     * @param daysThreshold Seuil en jours
     * @return true si l'utilisateur a été actif dans la période
     */
    public boolean isActiveRecently(int daysThreshold) {
        long threshold = System.currentTimeMillis() - (daysThreshold * 24 * 60 * 60 * 1000L);
        return lastUpdated > threshold;
    }

    /**
     * Analyse la tendance de progression (positive, négative ou stable)
     * @return 1 pour positive, 0 pour stable, -1 pour négative
     */
    public int getProgressTrend() {
        if (progressHistory.size() < 3) {
            return 0; // Pas assez de données
        }

        int size = progressHistory.size();
        double recent = 0;
        double older = 0;

        // Moyenne des 3 dernières entrées
        for (int i = 0; i < 3; i++) {
            recent += progressHistory.get(size - 1 - i);
        }
        recent /= 3;

        // Moyenne des 3 entrées précédentes
        for (int i = 3; i < 6 && size - 1 - i >= 0; i++) {
            older += progressHistory.get(size - 1 - i);
        }
        older /= 3;

        double difference = recent - older;
        if (Math.abs(difference) < 5.0) {
            return 0; // Stable (moins de 5% de différence)
        } else if (difference > 0) {
            return 1; // Positive
        } else {
            return -1; // Négative
        }
    }

    // Getters et Setters standards

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
        updateProgressHistory();
    }

    public long getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(long enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Map<String, SectionProgress> getSections() {
        return sections;
    }

    public void setSections(Map<String, SectionProgress> sections) {
        this.sections = sections;
    }

    public List<QuizAttempt> getQuizAttempts() {
        return quizAttempts;
    }

    public void setQuizAttempts(List<QuizAttempt> quizAttempts) {
        this.quizAttempts = quizAttempts;
    }

    public long getTotalTimeSpent() {
        return totalTimeSpent;
    }

    public void setTotalTimeSpent(long totalTimeSpent) {
        this.totalTimeSpent = totalTimeSpent;
    }

    public List<String> getCompletedActivities() {
        return completedActivities;
    }

    public void setCompletedActivities(List<String> completedActivities) {
        this.completedActivities = completedActivities;
    }

    public Map<String, Integer> getSkillProgress() {
        return skillProgress;
    }

    public void setSkillProgress(Map<String, Integer> skillProgress) {
        this.skillProgress = skillProgress;
    }

    public boolean isCertificateEarned() {
        return certificateEarned;
    }

    public void setCertificateEarned(boolean certificateEarned) {
        this.certificateEarned = certificateEarned;
        if (certificateEarned && certificateEarnedAt == 0) {
            this.certificateEarnedAt = System.currentTimeMillis();
        }
    }

    public long getCertificateEarnedAt() {
        return certificateEarnedAt;
    }

    public void setCertificateEarnedAt(long certificateEarnedAt) {
        this.certificateEarnedAt = certificateEarnedAt;
    }

    public String getUserNotes() {
        return userNotes;
    }

    public void setUserNotes(String userNotes) {
        this.userNotes = userNotes;
    }

    public int getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(int bookmarks) {
        this.bookmarks = bookmarks;
    }

    public List<Double> getProgressHistory() {
        return progressHistory;
    }

    public void setProgressHistory(List<Double> progressHistory) {
        this.progressHistory = progressHistory;
    }
    public void setOverallProgress(int percentage) {
        // Any other logic needed
    }
    public long getLastAccessedTimestamp() {
        return lastAccessedTimestamp;
    }
    public void setLastAccessedTimestamp(long timestamp) {
        this.lastAccessedTimestamp = timestamp;
    }

    // This is a convenience method to update the timestamp to the current time
    public void updateLastAccessed() {
        this.lastAccessedTimestamp = System.currentTimeMillis();
    }
}