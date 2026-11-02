package model.backup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.Question;

import java.io.*;
import java.lang.reflect.Type;
import java.util.List;


public class JSONQuestionBackupIO implements QuestionBackupIO {

    private final Gson gson;
    private final String BASE_PATH;

    public JSONQuestionBackupIO() {
        // Configuramos Gson para que el JSON sea legible
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.BASE_PATH = System.getProperty("user.home") + File.separator;
    }

    @Override
    public void exportQuestions(List<Question> questions, String filename) throws QuestionBackupIOException {
        // Aseguramos que termine en .json
        if (!filename.endsWith(".json")) filename += ".json";
        File file = new File(BASE_PATH + filename);

        try (Writer writer = new FileWriter(file)) {
            gson.toJson(questions, writer);
        } catch (IOException e) {
            throw new QuestionBackupIOException("Error al exportar JSON a " + file.getAbsolutePath(), e);
        }
    }

    @Override
    public List<Question> importQuestions(String filename) throws QuestionBackupIOException {
        if (!filename.endsWith(".json")) filename += ".json";
        File file = new File(BASE_PATH + filename);

        if (!file.exists()) {
            throw new QuestionBackupIOException("El fichero " + file.getAbsolutePath() + " no existe.");
        }

        try (Reader reader = new FileReader(file)) {
            // TypeToken es necesario para que Gson entienda que queremos una List<Question>
            Type listType = new TypeToken<List<Question>>(){}.getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            throw new QuestionBackupIOException("Error al importar JSON desde " + file.getAbsolutePath(), e);
        } catch (Exception e) { // Captura errores de parseo JSON malformado
            throw new QuestionBackupIOException("El fichero no tiene un formato JSON de preguntas válido.", e);
        }
    }
}
