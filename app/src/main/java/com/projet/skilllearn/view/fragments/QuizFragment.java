package com.projet.skilllearn.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.projet.skilllearn.R;
import com.projet.skilllearn.model.Achievement;
import com.projet.skilllearn.model.Quiz;
import com.projet.skilllearn.model.QuizQuestion;
import com.projet.skilllearn.utils.UserProgressManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizFragment extends Fragment {
    private static QuizFragment instance;
    private TextView tvQuestion;
    private RadioGroup rgOptions;
    private Button btnSubmit;
    private Button btnNext;
    private TextView tvFeedback;
    private TextView tvScore;

    private Quiz quiz;
    private List<QuizQuestion> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private boolean answered = false;

    public static QuizFragment getInstance() {
        if (instance == null) {
            instance = new QuizFragment();
        }
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvQuestion = view.findViewById(R.id.tv_question);
        rgOptions = view.findViewById(R.id.rg_options);
        btnSubmit = view.findViewById(R.id.btn_submit);
        btnNext = view.findViewById(R.id.btn_next);
        tvFeedback = view.findViewById(R.id.tv_feedback);
        tvScore = view.findViewById(R.id.tv_score);

        btnSubmit.setOnClickListener(v -> checkAnswer());
        btnNext.setOnClickListener(v -> showNextQuestion());

        // Afficher le quiz s'il est disponible
        if (quiz != null) {
            updateQuiz(quiz);
        }
    }

    public void updateQuiz(Quiz quiz) {
        this.quiz = quiz;
        this.questions = quiz.getQuestions();
        this.currentQuestionIndex = 0;
        this.score = 0;
        this.answered = false;

        updateScoreDisplay();
        if (questions != null && !questions.isEmpty()) {
            showQuestion(currentQuestionIndex);
        }
    }

    private void showQuestion(int index) {
        if (questions == null || index >= questions.size()) {
            tvQuestion.setText("Pas de questions disponibles");
            rgOptions.removeAllViews();
            btnSubmit.setEnabled(false);
            btnNext.setEnabled(false);
            return;
        }

        QuizQuestion question = questions.get(index);
        tvQuestion.setText(question.getQuestion());

        // Réinitialiser les options
        rgOptions.removeAllViews();

        // Ajouter les options
        List<String> options = question.getOptions();
        for (int i = 0; i < options.size(); i++) {
            RadioButton rb = new RadioButton(requireContext());
            rb.setId(View.generateViewId());
            rb.setText(options.get(i));
            rgOptions.addView(rb);
        }

        // Réinitialiser l'état
        rgOptions.clearCheck();
        btnSubmit.setEnabled(true);
        btnNext.setEnabled(false);
        tvFeedback.setText("");
        tvFeedback.setVisibility(View.GONE);

        answered = false;
    }

    private void checkAnswer() {
        if (answered || questions == null || currentQuestionIndex >= questions.size()) {
            return;
        }

        int selectedId = rgOptions.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(requireContext(), "Veuillez sélectionner une réponse", Toast.LENGTH_SHORT).show();
            return;
        }

        answered = true;

        // Trouver l'index de l'option sélectionnée
        int selectedIndex = -1;
        for (int i = 0; i < rgOptions.getChildCount(); i++) {
            RadioButton rb = (RadioButton) rgOptions.getChildAt(i);
            if (rb.getId() == selectedId) {
                selectedIndex = i;
                break;
            }
        }

        QuizQuestion question = questions.get(currentQuestionIndex);
        int correctIndex = question.getCorrectOptionIndex();

        if (selectedIndex == correctIndex) {
            // Réponse correcte
            score++;
            tvFeedback.setText("Correct ! " + question.getExplanation());
            tvFeedback.setTextColor(getResources().getColor(R.color.colorCorrect, null));
        } else {
            // Réponse incorrecte
            tvFeedback.setText("Incorrect. La bonne réponse est : " +
                    question.getOptions().get(correctIndex) + "\n" + question.getExplanation());
            tvFeedback.setTextColor(getResources().getColor(R.color.colorIncorrect, null));
        }

        tvFeedback.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);
        btnNext.setEnabled(true);

        updateScoreDisplay();
    }

    private void updateScoreDisplay() {
        if (questions != null && !questions.isEmpty()) {
            tvScore.setText(String.format("Score: %d/%d", score, questions.size()));
        } else {
            tvScore.setText("Score: 0/0");
        }
    }

    private void showNextQuestion() {
        currentQuestionIndex++;
        updateScoreDisplay();

        if (currentQuestionIndex < questions.size()) {
            showQuestion(currentQuestionIndex);
        } else {
            // Quiz terminé
            showQuizResult();
        }
    }

    private void showQuizResult() {
        double percentage = (double) score / questions.size() * 100;

        // Cacher les éléments du quiz
        tvQuestion.setVisibility(View.GONE);
        rgOptions.setVisibility(View.GONE);
        btnSubmit.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);

        // Afficher le résultat
        tvFeedback.setText(String.format("Quiz terminé !\nVotre score : %d/%d (%.1f%%)",
                score, questions.size(), percentage));
        tvFeedback.setVisibility(View.VISIBLE);

        // Sauvegarder le résultat si l'utilisateur est connecté
        if (FirebaseAuth.getInstance().getCurrentUser() != null && getActivity() != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String courseId = getActivity().getIntent().getStringExtra("courseId");

            if (courseId != null && quiz != null) {
                // Enregistrer le résultat du quiz
                saveQuizResult(userId, courseId, quiz.getQuizId(), percentage);

                // Vérifier si l'utilisateur mérite un badge
                if (percentage >= 80) {
                    UserProgressManager.getInstance().addAchievement(
                            new Achievement(
                                    "quiz_master_" + quiz.getQuizId(),
                                    "Quiz Master",
                                    "Vous avez obtenu plus de 80% au quiz",
                                    "quiz_master",
                                    System.currentTimeMillis()
                            )
                    );
                }
            }
        }
    }

    private void saveQuizResult(String userId, String courseId, String quizId, double percentage) {
        DatabaseReference resultRef = FirebaseDatabase.getInstance().getReference("quiz_results")
                .child(userId).child(courseId).child(quizId);

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("score", score);
        resultData.put("totalQuestions", questions.size());
        resultData.put("percentage", percentage);
        resultData.put("completedAt", System.currentTimeMillis());

        resultRef.setValue(resultData);
    }
}