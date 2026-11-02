package model.exam;

import model.Question;
import model.Option;
import java.util.List;


public class Exam {
    private List<Question> questions;
    private int[] answers; // Almacena el índice de la opción elegida (-1 si no contestada)
    private long startTime;
    private long endTime;

    public Exam(List<Question> questions) {
        this.questions = questions;
        this.answers = new int[questions.size()];
        // Inicializamos respuestas a -1 
        for (int i = 0; i < answers.length; i++) {
            answers[i] = -1;
        }
    }

    public void start() {
        this.startTime = System.currentTimeMillis();
    }

    public void finish() {
        this.endTime = System.currentTimeMillis();
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void answerQuestion(int questionIndex, int optionIndex) {
        if (questionIndex >= 0 && questionIndex < answers.length) {
            answers[questionIndex] = optionIndex;
        }
    }

    // Devuelve el objeto Option seleccionado para una pregunta dada
    public Option getSelectedOption(int questionIndex) {
        int optIdx = answers[questionIndex];
        if (optIdx == -1) return null;
        return questions.get(questionIndex).getOptions().get(optIdx);
    }

    // Cálculo de estadísticas y nota
    public ExamResult calculateResult() {
        int correct = 0;
        int incorrect = 0;
        int unanswered = 0;
        double rawScore = 0.0;

        for (int i = 0; i < questions.size(); i++) {
            int answerIdx = answers[i];
            if (answerIdx == -1) {
                unanswered++;
            } else {
                Option selected = questions.get(i).getOptions().get(answerIdx);
                if (selected.isCorrect()) {
                    correct++;
                    rawScore += 1.0;
                } else {
                    incorrect++;
                    rawScore -= (1.0 / 3.0); // Penalización de 1/3 
                }
            }
        }

        // Evitar notas negativas
        if (rawScore < 0) rawScore = 0;

        // Regla de tres para nota sobre 10
        double maxPossibleScore = questions.size();
        double finalGrade = (maxPossibleScore == 0) ? 0 : (rawScore / maxPossibleScore) * 10.0;

        long durationSeconds = (endTime - startTime) / 1000;

        return new ExamResult(finalGrade, correct, incorrect, unanswered, durationSeconds);
    }

    // Clase interna (record o clase simple) para pasar los resultados
    public static class ExamResult {
        public final double grade;
        public final int correct;
        public final int incorrect;
        public final int unanswered;
        public final long durationSeconds;

        public ExamResult(double grade, int correct, int incorrect, int unanswered, long durationSeconds) {
            this.grade = grade;
            this.correct = correct;
            this.incorrect = incorrect;
            this.unanswered = unanswered;
            this.durationSeconds = durationSeconds;
        }
    }
}
