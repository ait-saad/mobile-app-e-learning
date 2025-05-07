package com.projet.skilllearn.model;

import java.util.List;

public class Quiz {
    private String quizId;
    private String title;
    private List<QuizQuestion> questions;
    private int passingScore;
    private boolean required;

    // Constructeur par défaut pour Firebase
    public Quiz() {
    }

    public Quiz(String quizId, String title, List<QuizQuestion> questions, int passingScore, boolean required) {
        this.quizId = quizId;
        this.title = title;
        this.questions = questions;
        this.passingScore = passingScore;
        this.required = required;
    }

    // Getters et setters
    public String getQuizId() {
        return quizId;
    }

    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<QuizQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuizQuestion> questions) {
        this.questions = questions;
    }

    public int getPassingScore() {
        return passingScore;
    }

    public void setPassingScore(int passingScore) {
        this.passingScore = passingScore;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}