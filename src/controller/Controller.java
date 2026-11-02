package controller;

import model.Model;
import model.Option;
import model.Question;
import model.exam.Exam;
import model.backup.QuestionBackupIOException; // Import
import model.repository.RepositoryException;
import view.BaseView;
import model.automatic.QuestionCreator;
import model.automatic.QuestionCreatorException;

import java.util.List;
import java.util.Set;


public class Controller {
    
    private Model model;
    private BaseView view;

    public Controller(Model model, BaseView view) {
        this.model = model;
        this.view = view;
    }

    public void start() {
        view.init();
    }

    public void end() {
        try {
            model.saveChanges();
            view.showMessage("Datos guardados automáticamente.");
        } catch (RepositoryException e) {
            view.showErrorMessage("Error al guardar estado: " + e.getMessage());
        }
        view.end();
    }

    // -- Métodos CRUD --

    public List<Question> getQuestions() {
        try {
            return model.getAllQuestions();
        } catch (RepositoryException e) {
            view.showErrorMessage("Error al recuperar preguntas: " + e.getMessage());
            return List.of();
        }
    }

    public void createQuestion(String author, String statement, Set<String> topics, List<Option> options) {
        try {
            model.createQuestion(author, statement, topics, options);
        } catch (IllegalArgumentException | RepositoryException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void deleteQuestion(Question q) {
        try {
            model.removeQuestion(q);
        } catch (RepositoryException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void modifyQuestion(Question q) {
        try {
            model.updateQuestion(q);
        } catch (IllegalArgumentException | RepositoryException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // --- MÉTODOS IMPORT/EXPORT ---

    public void exportToJSON(String filename) {
        try {
            model.exportData(filename);
            view.showMessage("Exportación exitosa a " + filename);
        } catch (QuestionBackupIOException | RepositoryException e) {
            view.showErrorMessage("Error al exportar: " + e.getMessage());
        }
    }

    public void importFromJSON(String filename) {
        try {
            int count = model.importData(filename);
            view.showMessage("Importación finalizada. Se añadieron " + count + " preguntas nuevas.");
            if (count == 0) {
                view.showMessage("(Posiblemente todas las preguntas del fichero ya existían en el banco).");
            }
        } catch (QuestionBackupIOException | RepositoryException e) {
            view.showErrorMessage("Error al importar: " + e.getMessage());
        }
    }


// --- MÉTODOS MODO EXAMEN ---

    public Set<String> getAvailableTopics() {
        try {
            return model.getAvailableTopics();
        } catch (RepositoryException e) {
            view.showErrorMessage("Error obteniendo temas: " + e.getMessage());
            return Set.of();
        }
    }

    public int getMaxQuestions(String topic) {
        try {
            return model.getMaxQuestionsForTopic(topic);
        } catch (RepositoryException e) {
            return 0;
        }
    }

    public void startExam(String topic, int numQuestions) {
        try {
            model.startExam(topic, numQuestions);
        } catch (RepositoryException e) {
            throw new RuntimeException("Error iniciando examen: " + e.getMessage());
        }
    }

    public Exam getCurrentExam() {
        return model.getCurrentExam();
    }

    public void answerExamQuestion(int questionIndex, int optionIndex) {
        model.answerExamQuestion(questionIndex, optionIndex);
    }

    public Exam.ExamResult finishExam() {
        return model.finishExam();
    }

    // --- MÉTODOS IA ---

    public List<QuestionCreator> getQuestionCreators() {
        return model.getQuestionCreators();
    }

    public Question generateQuestion(int creatorIndex, String topic) {
        try {
            QuestionCreator creator = model.getQuestionCreators().get(creatorIndex);
            view.showMessage("Contactando con " + creator.getDescription() + " para generar pregunta sobre '" + topic + "'...");
            return creator.createQuestion(topic);
        } catch (QuestionCreatorException | IndexOutOfBoundsException e) {
            view.showErrorMessage("Fallo en la generación: " + e.getMessage());
            return null;
        }
    }

    public void saveGeneratedQuestion(Question q) {
        try {
            model.addGeneratedQuestion(q);
        } catch (RepositoryException e) {
            view.showErrorMessage("Error al guardar: " + e.getMessage());
        }
    }
}
