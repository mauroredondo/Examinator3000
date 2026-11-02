package model.repository;

import model.Question;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class BinaryRepository implements IRepository {
    
    private ArrayList<Question> questions;
    private final String FILE_PATH;

    public BinaryRepository() {
        this.questions = new ArrayList<>();
        // Definimos la ruta en el home del usuario 
        this.FILE_PATH = System.getProperty("user.home") + File.separator + "questions.bin";
        load(); // Cargamos al iniciar
    }

    // Método privado para cargar el estado inicial
    @SuppressWarnings("unchecked")
    private void load() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                this.questions = (ArrayList<Question>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                // Si falla, iniciamos con lista vacía pero imprimimos error por consola estándar de error
                System.err.println("Error cargando questions.bin (se iniciará vacío): " + e.getMessage());
                this.questions = new ArrayList<>();
            }
        }
    }

    @Override
    public void save() throws RepositoryException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(questions);
        } catch (IOException e) {
            throw new RepositoryException("Error guardando el archivo binario: " + e.getMessage(), e);
        }
    }

    @Override
    public void addQuestion(Question q) throws RepositoryException {
        if (q == null) throw new RepositoryException("La pregunta no puede ser nula.");
        questions.add(q);
    }

    @Override
    public void removeQuestion(Question q) throws RepositoryException {
        if (!questions.remove(q)) {
            throw new RepositoryException("La pregunta no existe en el repositorio.");
        }
    }

    @Override
    public void modifyQuestion(Question q) throws RepositoryException {
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).getId().equals(q.getId())) {
                questions.set(i, q);
                return;
            }
        }
        throw new RepositoryException("No se encontró la pregunta para modificar.");
    }

    @Override
    public List<Question> getAllQuestions() throws RepositoryException {
        return new ArrayList<>(questions);
    }
}
