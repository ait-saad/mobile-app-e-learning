
package com.projet.skilllearn.model;

import java.util.List;

/**
 * Représente le résultat d'un quiz complété par un utilisateur
 */
public class QuizResult {
    private String quizId;
    private String userId;
    private String courseId;
    private String sectionId;
    private List<QuizQuestion> questions;
    private List<Boolean> correctAnswers;
    private List<Integer> selectedAnswers;
    private int totalQuestions;
    private int correctCount;
    private double percentage;
    private long completedAt;
    private boolean passed;

    /**
     * Constructeur par défaut requis pour Firebase
     */
    public QuizResult() {
    }

    /**
     * Constructeur complet
     * @param quizId Identifiant du quiz
     * @param userId Identifiant de l'utilisateur
     * @param courseId Identifiant du cours
     * @param sectionId Identifiant de la section
     * @param questions Liste des questions du quiz
     * @param correctAnswers Liste des réponses correctes (true/false pour chaque question)
     * @param selectedAnswers Liste des indices des options sélectionnées par l'utilisateur
     */
    public QuizResult(String quizId, String userId, String courseId, String sectionId,
                      List<QuizQuestion> questions, List<Boolean> correctAnswers,
                      List<Integer> selectedAnswers) {
        this.quizId = quizId;
        this.userId = userId;
        this.courseId = courseId;
        this.sectionId = sectionId;
        this.questions = questions;
        this.correctAnswers = correctAnswers;
        this.selectedAnswers = selectedAnswers;
        this.totalQuestions = questions.size();
        this.completedAt = System.currentTimeMillis();

        // Calculer le nombre de réponses correctes et le pourcentage
        this.correctCount = 0;
        for (Boolean correct : correctAnswers) {
            if (correct) {
                this.correctCount++;
            }
        }
        this.percentage = (totalQuestions > 0) ?
                ((double) correctCount / totalQuestions) * 100 : 0;
    }

    /**
     * Vérifie si l'utilisateur a réussi le quiz
     * @param passingScore Score minimum pour réussir (en pourcentage)
     * @return true si l'utilisateur a réussi, false sinon
     */
    public boolean checkPassed(double passingScore) {
        this.passed = this.percentage >= passingScore;
        return this.passed;
    }

    // Getters and setters

    public String getQuizId() {
        return quizId;
    }

    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }

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

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public List<QuizQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuizQuestion> questions) {
        this.questions = questions;
        this.totalQuestions = questions.size();
    }

    public List<Boolean> getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(List<Boolean> correctAnswers) {
        this.correctAnswers = correctAnswers;

        // Recalculer le nombre de réponses correctes
        this.correctCount = 0;
        for (Boolean correct : correctAnswers) {
            if (correct) {
                this.correctCount++;
            }
        }

        // Recalculer le pourcentage
        this.percentage = (totalQuestions > 0) ?
                ((double) correctCount / totalQuestions) * 100 : 0;
    }

    public List<Integer> getSelectedAnswers() {
        return selectedAnswers;
    }

    public void setSelectedAnswers(List<Integer> selectedAnswers) {
        this.selectedAnswers = selectedAnswers;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;

        // Recalculer le pourcentage
        this.percentage = (totalQuestions > 0) ?
                ((double) correctCount / totalQuestions) * 100 : 0;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;

        // Recalculer le pourcentage
        this.percentage = (totalQuestions > 0) ?
                ((double) correctCount / totalQuestions) * 100 : 0;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    /**
     * Obtient une représentation textuelle du résultat du quiz
     * @return Chaîne de caractères décrivant le résultat
     */
    @Override
    public String toString() {
        String status = passed ? "Réussi" : "Échoué";
        return String.format("Quiz %s: %d/%d (%.1f%%) - %s",
                quizId, correctCount, totalQuestions, percentage, status);
    }
}