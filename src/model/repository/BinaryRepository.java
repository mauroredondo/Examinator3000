package model.repository;

import model.Question;
import java.io.*;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
            } catch (IOException | ClassNotFoundException | ClassCastException e) {
                // Si el fichero no se puede leer, NO lo dejamos en su sitio: el siguiente guardado
                // lo sobrescribiría con una lista vacía y se perderían los datos. Lo apartamos
                // con otro nombre para que se pueda recuperar a mano.
                this.questions = new ArrayList<>();
                File apartado = new File(FILE_PATH + ".corrupto-" + System.currentTimeMillis());
                if (file.renameTo(apartado)) {
                    System.err.println("No se pudo leer questions.bin (" + e.getMessage() + ").\n"
                            + "Se ha conservado una copia en " + apartado.getAbsolutePath()
                            + " y se empieza con un banco vacío.");
                } else {
                    System.err.println("No se pudo leer questions.bin (" + e.getMessage() + ") "
                            + "y tampoco apartarlo. Se empieza vacío.");
                }
            }
        }
    }

    // Escribe primero en un fichero temporal y luego lo mueve a su sitio, de modo que
    // un fallo a mitad de escritura nunca deja questions.bin a medias.
    @Override
    public void save() throws RepositoryException {
        Path destino = new File(FILE_PATH).toPath();
        Path temporal = new File(FILE_PATH + ".tmp").toPath();
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(temporal))) {
            oos.writeObject(questions);
        } catch (IOException e) {
            throw new RepositoryException("Error guardando el archivo binario: " + e.getMessage(), e);
        }
        try {
            try {
                Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RepositoryException("Error guardando el archivo binario: " + e.getMessage(), e);
        }
    }

    // Cada cambio se guarda al momento, para no perder datos si el programa se cierra mal
    @Override
    public void addQuestion(Question q) throws RepositoryException {
        if (q == null) throw new RepositoryException("La pregunta no puede ser nula.");
        questions.add(q);
        save();
    }

    @Override
    public void removeQuestion(Question q) throws RepositoryException {
        if (!questions.remove(q)) {
            throw new RepositoryException("La pregunta no existe en el repositorio.");
        }
        save();
    }

    @Override
    public void modifyQuestion(Question q) throws RepositoryException {
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).getId().equals(q.getId())) {
                questions.set(i, q);
                save();
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
