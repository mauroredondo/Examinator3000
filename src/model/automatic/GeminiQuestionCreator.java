package model.automatic;

import es.usal.genai.GenAiConfig;
import es.usal.genai.GenAiFacade;
import es.usal.genai.SimpleSchemas;
import com.google.genai.types.Schema;
import model.Option;
import model.Question;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class GeminiQuestionCreator implements QuestionCreator {

    private final String modelId;
    private final String apiKey;

    public GeminiQuestionCreator(String modelId, String apiKey) {
        this.modelId = modelId;
        this.apiKey = apiKey;
    }

    @Override
    public String getDescription() {
        return "Google Gemini (" + modelId + ")";
    }

    @Override
    public Question createQuestion(String topic) throws QuestionCreatorException {
        // Configuramos la conexión con Gemini usando la librería proporcionada
        GenAiConfig config = GenAiConfig.forGemini(modelId, apiKey);
        
        // Usamos try-with-resources para asegurar que se cierra la conexión
        try (GenAiFacade genai = new GenAiFacade(config)) {
            
            //Definimos el esquema esperado usando las clases DTO
            Schema schema = SimpleSchemas.from(QuestionDTO.class);

            //Creamos el prompt específico
            String prompt = String.format(
                "Genera una pregunta de tipo test difícil sobre el tema '%s'. " +
                "Debe tener un enunciado claro y exactamente 4 opciones. " +
                "Solo una opción puede ser correcta (isCorrect=true). " +
                "Proporciona una justificación (rationale) breve para cada opción.", 
                topic
            );

            //Llamada a la IA solicitando un objeto estructurado (JSON)
            QuestionDTO dto = genai.generateJson(prompt, schema, QuestionDTO.class);

            //Convertir el DTO a nuestro Modelo de Dominio
            if (dto.options == null || dto.options.size() != 4) {
                throw new QuestionCreatorException("La IA no generó 4 opciones válidas.");
            }

            List<Option> domainOptions = new ArrayList<>();
            for (OptionDTO optDto : dto.options) {
                domainOptions.add(new Option(optDto.text, optDto.rationale, optDto.isCorrect));
            }
            
            // Validar que haya exactamente 1 correcta
            long correctCount = domainOptions.stream().filter(Option::isCorrect).count();
            if (correctCount != 1) {
                throw new QuestionCreatorException("La IA generó " + correctCount + " respuestas correctas, se requiere 1.");
            }

            Set<String> topics = new HashSet<>();
            topics.add(topic.toUpperCase());

            return new Question("Gemini AI", dto.statement, domainOptions, topics);

        } catch (Exception e) {
            throw new QuestionCreatorException("Error generando pregunta con Gemini: " + e.getMessage(), e);
        }
    }

    // --- DTOs ---
    // Tienen que ser public static para que la librería pueda instanciarlos
    
    public static class QuestionDTO {
        public String statement;
        public List<OptionDTO> options;
    }

    public static class OptionDTO {
        public String text;
        public String rationale;
        public boolean isCorrect;
    }
}
