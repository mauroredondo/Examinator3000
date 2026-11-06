package model;

import model.backup.QuestionBackupIO;
import model.backup.QuestionBackupIOException;
import model.exam.Exam;
import model.repository.IRepository;
import model.repository.RepositoryException;
import model.automatic.QuestionCreator;

import java.util.*;
import java.util.stream.Collectors;

public class Model {
    
    private IRepository repository;
    private QuestionBackupIO backupIO;
    private List<QuestionCreator> questionCreators; // Lista de IAs disponibles
    private Exam currentExam; // Examen activo

    // Constructor con inyección de dependencias
    public Model(IRepository repository, QuestionBackupIO backupIO, List<QuestionCreator> questionCreators) {
        this.repository = repository;
        this.backupIO = backupIO;
        this.questionCreators = questionCreators;
    }

    // --- MÉTODOS DE PERSISTENCIA ---

    public void saveChanges() throws RepositoryException {
        repository.save();
    }

    // --- MÉTODOS CRUD ---

    public List<Question> getAllQuestions() throws RepositoryException {
        return repository.getAllQuestions();
    }

    public void createQuestion(String author, String statement, Set<String> topics, List<Option> options) 
            throws RepositoryException {
        
        validateQuestion(statement, options);

        Question q = new Question(author, statement, options, topics);
        repository.addQuestion(q);
    }

    public void removeQuestion(Question q) throws RepositoryException {
        repository.removeQuestion(q);
    }

    public void updateQuestion(Question q) throws RepositoryException {
        validateQuestion(q.getStatement(), q.getOptions());
        repository.modifyQuestion(q);
    }

    // --- IMPORTACIÓN / EXPORTACIÓN ---

    public void exportData(String filename) throws QuestionBackupIOException, RepositoryException {
        List<Question> questions = repository.getAllQuestions();
        backupIO.exportQuestions(questions, filename);
    }

    // Resultado de una importación: cuántas preguntas se añadieron, cuántas ya existían
    // (mismo ID) y cuántas se descartaron por no ser válidas.
    public record ImportResult(int added, int duplicates, int invalid) {}

    public ImportResult importData(String filename) throws QuestionBackupIOException, RepositoryException {
        List<Question> importedQuestions = backupIO.importQuestions(filename);
        List<Question> currentQuestions = repository.getAllQuestions();

        // Obtenemos los IDs existentes para comparar rápidamente
        Set<UUID> existingIds = currentQuestions.stream()
                                    .map(Question::getId)
                                    .collect(Collectors.toSet());

        int added = 0, duplicates = 0, invalid = 0;
        for (Question q : importedQuestions) {
            // Gson rellena los campos sin pasar por el constructor, así que una pregunta
            // importada hay que validarla y normalizarla antes de aceptarla
            if (!normalizeImported(q)) {
                invalid++;
            } else if (existingIds.contains(q.getId())) {
                // Solo añadimos si el ID no existe ya en el banco
                duplicates++;
            } else {
                repository.addQuestion(q);
                existingIds.add(q.getId());
                added++;
            }
        }
        return new ImportResult(added, duplicates, invalid);
    }

    // Devuelve false si la pregunta importada no es válida; si lo es, la deja normalizada
    private boolean normalizeImported(Question q) {
        if (q == null) return false;
        try {
            validateQuestion(q.getStatement(), q.getOptions());
        } catch (IllegalArgumentException e) {
            return false;
        }
        q.setTopics(q.getTopics());          // mayúsculas y sin espacios sobrantes
        if (q.getTopics().isEmpty()) return false;
        if (q.getId() == null) q.setId(UUID.randomUUID());
        for (Option o : q.getOptions()) {
            if (o.getRationale() == null) o.setRationale("");
        }
        return true;
    }

    // --- MODO EXAMEN ---

    public Set<String> getAvailableTopics() throws RepositoryException {
        List<Question> all = repository.getAllQuestions();
        Set<String> topics = new HashSet<>();
        for (Question q : all) {
            if (q.getTopics() != null) {
                topics.addAll(q.getTopics());
            }
        }
        return topics;
    }

    public int getMaxQuestionsForTopic(String topic) throws RepositoryException {
        return getQuestionsByTopic(topic).size();
    }

    // Método auxiliar privado para filtrar
    private List<Question> getQuestionsByTopic(String topic) throws RepositoryException {
        List<Question> all = repository.getAllQuestions();
        if (topic == null || topic.equalsIgnoreCase("TODOS")) {
            return all;
        }
        return all.stream()
                .filter(q -> q.getTopics().contains(topic.toUpperCase()))
                .collect(Collectors.toList());
    }

    public void startExam(String topic, int numQuestions) throws RepositoryException {
        List<Question> candidates = getQuestionsByTopic(topic);
        
        // Barajar para que sea aleatorio
        Collections.shuffle(candidates);
        
        // Seleccionar las N primeras
        List<Question> selected = candidates.stream()
                                            .limit(numQuestions)
                                            .collect(Collectors.toList());
        
        this.currentExam = new Exam(selected);
        this.currentExam.start();
    }

    public Exam getCurrentExam() {
        return currentExam;
    }

    public void answerExamQuestion(int questionIndex, int optionIndex) {
        if (currentExam != null) {
            currentExam.answerQuestion(questionIndex, optionIndex);
        }
    }

    public Exam.ExamResult finishExam() {
        if (currentExam != null) {
            currentExam.finish();
            return currentExam.calculateResult();
        }
        return null;
    }

    // --- VALIDACIONES INTERNAS ---

    private void validateQuestion(String statement, List<Option> options) {
        if (statement == null || statement.trim().isEmpty()) {
            throw new IllegalArgumentException("El enunciado no puede estar vacío.");
        }
        validateOptions(options);
    }

    private void validateOptions(List<Option> options) {
        if (options == null || options.size() != 4) {
            throw new IllegalArgumentException("La pregunta debe tener exactamente 4 opciones.");
        }
        for (Option o : options) {
            if (o == null || o.getText() == null || o.getText().trim().isEmpty()) {
                throw new IllegalArgumentException("Todas las opciones deben tener texto.");
            }
        }
        long correctCount = options.stream().filter(Option::isCorrect).count();
        if (correctCount != 1) {
            throw new IllegalArgumentException("Debe haber exactamente una opción correcta.");
        }
    }

    // --- MÉTODOS IA ---
    public List<QuestionCreator> getQuestionCreators() {
        return questionCreators;
    }

    public void addGeneratedQuestion(Question q) throws RepositoryException {
        // Reutilizamos el método del repositorio
        repository.addQuestion(q);
    }

    
}