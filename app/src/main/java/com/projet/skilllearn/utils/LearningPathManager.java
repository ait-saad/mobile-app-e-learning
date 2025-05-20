package com.projet.skilllearn.utils;

import com.projet.skilllearn.model.Course;
import com.projet.skilllearn.model.User;
import com.projet.skilllearn.model.UserProgress;
import com.projet.skilllearn.model.LearningPreference;
import com.projet.skilllearn.model.UserSkill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestionnaire de parcours d'apprentissage intelligent
 * Utilise l'IA pour personnaliser les recommandations et adapter les cours aux besoins de l'utilisateur
 */
public class LearningPathManager {
    private static LearningPathManager instance;

    // Constantes pour les poids de facteurs d'apprentissage
    private static final double WEIGHT_COMPLETION_RATE = 0.3;
    private static final double WEIGHT_QUIZ_SCORE = 0.25;
    private static final double WEIGHT_PROGRESS_SPEED = 0.2;
    private static final double WEIGHT_LEARNING_STYLE = 0.15;
    private static final double WEIGHT_PREVIOUS_EXPERIENCE = 0.1;

    // Singleton
    public static synchronized LearningPathManager getInstance() {
        if (instance == null) {
            instance = new LearningPathManager();
        }
        return instance;
    }

    private LearningPathManager() {
        // Constructeur privé
    }

    /**
     * Génère un parcours d'apprentissage personnalisé basé sur les préférences et la progression
     * @param user L'utilisateur pour lequel générer le parcours
     * @param preferences Les préférences d'apprentissage de l'utilisateur
     * @param userSkills Les compétences actuelles de l'utilisateur
     * @param availableCourses La liste de tous les cours disponibles
     * @return Une liste de cours ordonnée pour le parcours d'apprentissage optimal
     */
    public List<Course> generatePersonalizedLearningPath(User user,
                                                         LearningPreference preferences,
                                                         List<UserSkill> userSkills,
                                                         List<Course> availableCourses) {
        List<Course> recommendedPath = new ArrayList<>();

        // Collecter les données d'apprentissage de l'utilisateur
        Map<String, Double> courseScores = calculateCourseRelevanceScores(
                user, preferences, userSkills, availableCourses);

        // Trier les cours par score de pertinence
        List<Map.Entry<String, Double>> sortedCourses = new ArrayList<>(courseScores.entrySet());
        sortedCourses.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        // Construire le parcours d'apprentissage optimal
        for (Map.Entry<String, Double> entry : sortedCourses) {
            String courseId = entry.getKey();
            for (Course course : availableCourses) {
                if (course.getCourseId().equals(courseId)) {
                    recommendedPath.add(course);
                    break;
                }
            }
        }

        return recommendedPath;
    }

    /**
     * Calcule les scores de pertinence pour chaque cours basés sur les besoins de l'utilisateur
     */
    private Map<String, Double> calculateCourseRelevanceScores(User user,
                                                               LearningPreference preferences,
                                                               List<UserSkill> userSkills,
                                                               List<Course> availableCourses) {
        Map<String, Double> courseScores = new HashMap<>();

        for (Course course : availableCourses) {
            double relevanceScore = 0.0;

            // Facteur 1: Correspondance avec les objectifs d'apprentissage
            double goalMatchScore = calculateGoalMatchScore(preferences, course);

            // Facteur 2: Niveau de compétence actuel vs niveau du cours
            double skillLevelScore = calculateSkillLevelMatch(userSkills, course);

            // Facteur 3: Historique d'apprentissage et taux de complétion des cours similaires
            double historyScore = calculateLearningHistoryScore(user, course);

            // Facteur 4: Style d'apprentissage préféré (vidéo, texte, quiz, etc.)
            double styleScore = calculateLearningStyleMatch(preferences, course);

            // Combiner les scores avec leur poids respectif
            relevanceScore = (goalMatchScore * 0.4) +
                    (skillLevelScore * 0.3) +
                    (historyScore * 0.2) +
                    (styleScore * 0.1);

            courseScores.put(course.getCourseId(), relevanceScore);
        }

        return courseScores;
    }

    /**
     * Calcule à quel point un cours correspond aux objectifs d'apprentissage
     */
    private double calculateGoalMatchScore(LearningPreference preferences, Course course) {
        // Logique d'analyse des tags, catégories et objectifs du cours
        double score = 0.0;

        // Correspondance des catégories
        if (preferences.getPreferredCategories().contains(course.getCategory())) {
            score += 0.5;
        }

        // Correspondance des tags
        List<String> userTags = preferences.getPreferredTags();
        List<String> courseTags = course.getTags();

        if (userTags != null && courseTags != null) {
            int matchingTags = 0;
            for (String tag : courseTags) {
                if (userTags.contains(tag)) {
                    matchingTags++;
                }
            }

            if (courseTags.size() > 0) {
                score += 0.5 * ((double) matchingTags / courseTags.size());
            }
        }

        return score;
    }

    /**
     * Calcule la correspondance entre le niveau de compétence de l'utilisateur et le niveau du cours
     */
    private double calculateSkillLevelMatch(List<UserSkill> userSkills, Course course) {
        // Convertir les niveaux en valeurs numériques
        int courseLevelValue = getLevelValue(course.getLevel());

        // Chercher si l'utilisateur a des compétences dans la catégorie du cours
        double userLevelValue = 0.0;
        boolean skillFound = false;

        for (UserSkill skill : userSkills) {
            if (skill.getCategory().equals(course.getCategory())) {
                userLevelValue = skill.getLevel();
                skillFound = true;
                break;
            }
        }

        if (!skillFound) {
            // Si l'utilisateur n'a jamais pratiqué cette catégorie, un cours débutant est idéal
            return courseLevelValue == 1 ? 1.0 : 0.5;
        }

        // Calcul de l'écart entre le niveau de l'utilisateur et celui du cours
        double levelDifference = Math.abs(userLevelValue - courseLevelValue);

        // Idéalement, le cours devrait être légèrement au-dessus du niveau actuel
        if (courseLevelValue > userLevelValue && levelDifference <= 1) {
            return 1.0; // Parfait pour progresser
        } else if (levelDifference <= 0.5) {
            return 0.8; // Bon match, même niveau
        } else if (levelDifference <= 1) {
            return 0.6; // Match acceptable
        } else {
            return 0.3; // Match faible
        }
    }

    /**
     * Convertit le niveau textuel en valeur numérique
     */
    private int getLevelValue(String level) {
        switch (level.toLowerCase()) {
            case "débutant":
                return 1;
            case "intermédiaire":
                return 2;
            case "avancé":
            case "expert":
                return 3;
            default:
                return 1;
        }
    }

    /**
     * Calcule un score basé sur l'historique d'apprentissage de l'utilisateur
     */
    private double calculateLearningHistoryScore(User user, Course course) {
        // À implémenter: analyse de l'historique des cours suivis
        // et taux de complétion des cours similaires

        return 0.7; // Score par défaut pour le moment
    }

    /**
     * Calcule la correspondance entre le style d'apprentissage préféré et le format du cours
     */
    private double calculateLearningStyleMatch(LearningPreference preferences, Course course) {
        // À implémenter: analyse du format du cours (vidéo, texte, etc.)
        // par rapport aux préférences de l'utilisateur

        return 0.8; // Score par défaut pour le moment
    }

    /**
     * Analyse les performances de l'utilisateur pour adapter la difficulté du contenu
     */
    public void adaptContentDifficulty(String userId, String courseId, int quizScore, int completionSpeed) {
        // Logique d'adaptation de la difficulté basée sur les performances
        // Par exemple, suggérer des défis plus complexes ou des ressources supplémentaires
    }

    /**
     * Identifie les lacunes dans les compétences de l'utilisateur
     */
    public List<String> identifySkillGaps(List<UserSkill> userSkills, List<String> targetSkills) {
        List<String> skillGaps = new ArrayList<>();

        // Identifier les compétences manquantes ou à améliorer
        for (String targetSkill : targetSkills) {
            boolean found = false;
            for (UserSkill userSkill : userSkills) {
                if (userSkill.getName().equals(targetSkill) && userSkill.getLevel() >= 2.0) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                skillGaps.add(targetSkill);
            }
        }

        return skillGaps;
    }

    /**
     * Fournit des rappels d'apprentissage intelligents basés sur le comportement de l'utilisateur
     */
    public void scheduleLearningReminders(User user, List<Course> enrolledCourses) {
        // Logique pour déterminer les moments optimaux de rappels
        // basés sur les habitudes d'apprentissage
    }
}