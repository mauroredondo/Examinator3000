package view;

import com.coti.tools.Esdia;
import controller.Controller;
import model.Option;
import model.Question;
import model.exam.Exam;
import model.automatic.QuestionCreator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class InteractiveView extends BaseView {

    @Override
    public void init() {
        System.out.println("--- EXAMINATOR 3000 ---");
        boolean exit = false;
        
        while (!exit) {
            boolean hasAI = !controller.getQuestionCreators().isEmpty();
            System.out.println("\nMENU PRINCIPAL");
            System.out.println("1. Gestionar Banco de Preguntas (CRUD)");
            System.out.println("2. Modo Examen");
            System.out.println("3. Importar/Exportar JSON"); // Opción habilitada
            if (hasAI) System.out.println("4. Generar pregunta automática (IA)");
            System.out.println((hasAI ? "5" : "4") + ". Salir");

            int maxOpt = hasAI ? 5 : 4;
            int option = Esdia.readInt("Seleccione: ", 1, maxOpt);

            if (option == 1) menuCrud();
            else if (option == 2) menuExam();
            else if (option == 3) menuImportExport();
            else if (hasAI && option == 4) menuAI(); // Submenú IA
            else exit = true;
            }
        
        controller.end();
}

    // --- SUBMENÚ CRUD ---
    private void menuCrud() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- GESTIÓN DE PREGUNTAS ---");
            System.out.println("1. Listar todas las preguntas");
            System.out.println("2. Crear nueva pregunta");
            System.out.println("3. Ver detalle / Modificar / Borrar");
            System.out.println("4. Volver al menú principal");

            int option = Esdia.readInt("Opción: ", 1, 4);

            switch (option) {
                case 1 -> showQuestionList(controller.getQuestions());
                case 2 -> createQuestionForm();
                case 3 -> manageQuestionDetails();
                case 4 -> back = true;
            }
        }
    }

    private void showQuestionList(List<Question> questions) {
        if (questions.isEmpty()) {
            System.out.println("No hay preguntas registradas.");
            return;
        }
        System.out.println("\nLISTADO DE PREGUNTAS:");
        System.out.printf("%-3s %-20s %-50s%n", "#", "Tema", "Enunciado (extracto)");
        System.out.println("----------------------------------------------------------------");
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            String shortStatement = q.getStatement().length() > 45 ?
                                    q.getStatement().substring(0, 45) + "..." : q.getStatement();
            System.out.printf("%-3d %-20s %-50s%n", (i + 1), q.getTopics(), shortStatement);
        }
    }

    private void createQuestionForm() {
        System.out.println("\n--- NUEVA PREGUNTA ---");
        try {
            String author = Esdia.readString("Autor: ");
            String statement = Esdia.readString("Enunciado: ");
            String topic = Esdia.readString("Tema principal: ");
            
            Set<String> topics = new HashSet<>();
            topics.add(topic);

            List<Option> options = new ArrayList<>();
            System.out.println("Introduzca 4 opciones:");
            
            // Validar que solo haya una correcta se hará visualmente aquí o en el modelo
            // Simplificación: pedimos datos secuenciales
            for (int i = 1; i <= 4; i++) {
                System.out.println("Opción " + i + ":");
                String text = Esdia.readString("  Texto: ");
                String rationale = Esdia.readString("  Justificación: ");
                boolean correct = Esdia.yesOrNo("  ¿Es correcta? ");
                options.add(new Option(text, rationale, correct));
            }

            controller.createQuestion(author, statement, topics, options);
            showMessage("Pregunta creada con éxito.");

        } catch (Exception e) {
            showErrorMessage("Error creando pregunta: " + e.getMessage());
        }
    }

    private void manageQuestionDetails() {
        List<Question> questions = controller.getQuestions();
        if (questions.isEmpty()) {
            System.out.println("No hay preguntas para seleccionar.");
            return;
        }
        
        showQuestionList(questions);
        int index = Esdia.readInt("Seleccione el número de la pregunta (0 para cancelar): ", 0, questions.size());
        
        if (index == 0) return;
        
        Question q = questions.get(index - 1);
        showQuestionFullDetails(q);
        
        System.out.println("\nACCIONES:");
        System.out.println("1. Modificar");
        System.out.println("2. Eliminar");
        System.out.println("3. Volver");
        
        int action = Esdia.readInt("Elija acción: ", 1, 3);
        
        try {
            if (action == 1) {
                modifyQuestionForm(q);
            } else if (action == 2) {
                if (Esdia.yesOrNo("¿Seguro que desea eliminar esta pregunta?")) {
                    controller.deleteQuestion(q);
                    showMessage("Pregunta eliminada.");
                }
            }
        } catch (Exception e) {
            showErrorMessage("Error en la operación: " + e.getMessage());
        }
    }

    private void showQuestionFullDetails(Question q) {
        System.out.println("\n--- DETALLE DE PREGUNTA ---");
        System.out.println("ID: " + q.getId());
        System.out.println("Autor: " + q.getAuthor());
        System.out.println("Temas: " + q.getTopics());
        System.out.println("Enunciado: " + q.getStatement());
        System.out.println("Opciones:");
        int i = 1;
        for (Option opt : q.getOptions()) {
            System.out.printf(" %d) %s [%s] -> %s%n", 
                i++, opt.getText(), opt.isCorrect() ? "CORRECTA" : "INCORRECTA", opt.getRationale());
        }
    }

    private void modifyQuestionForm(Question original) {
        System.out.println("Modificando pregunta (deje en blanco para mantener valor actual).");

        // Se edita una copia: la pregunta original no cambia hasta que el modelo
        // valida y guarda los cambios
        Question q = original.copy();

        // readString admite vacío (readString_ne no), que es lo que hace falta para "mantener"
        String newStatement = Esdia.readString("Nuevo enunciado [" + q.getStatement() + "]: ");
        if (!newStatement.trim().isEmpty()) q.setStatement(newStatement);

        // Para simplificar, si se quieren modificar opciones se piden todas de nuevo
        if (Esdia.yesOrNo("¿Desea modificar las opciones?")) {
            List<Option> newOptions = new ArrayList<>();
            for (int i = 1; i <= 4; i++) {
                System.out.println("Nueva Opción " + i + ":");
                String text = Esdia.readString("Texto: ");
                String rationale = Esdia.readString("Justificación: ");
                boolean correct = Esdia.yesOrNo("¿Es correcta? ");
                newOptions.add(new Option(text, rationale, correct));
            }
            q.setOptions(newOptions);
        }
        
        try {
            controller.modifyQuestion(q);
            showMessage("Pregunta modificada correctamente.");
        } catch (Exception e) {
            showErrorMessage("Error al guardar modificación: " + e.getMessage()
                    + " (la pregunta original no se ha modificado)");
        }
    }

    //--- SUBMENÚ IMPORT/EXPORT ---
    private void menuImportExport() {
        System.out.println("\n--- IMPORTAR / EXPORTAR ---");
        System.out.println("1. Exportar banco de preguntas a JSON");
        System.out.println("2. Importar preguntas desde JSON");
        System.out.println("3. Volver");
        
        int opt = Esdia.readInt("Opción: ", 1, 3);
        
        if (opt == 1) {
            String filename = Esdia.readString("Nombre del fichero (se guardará en su carpeta de usuario): ");
            controller.exportToJSON(filename);
        } else if (opt == 2) {
            String filename = Esdia.readString("Nombre del fichero a leer: ");
            controller.importFromJSON(filename);
        }
    }

    // --- MODO EXAMEN ---

    private void menuExam() {
        System.out.println("\n--- MODO EXAMEN ---");
        
        //Obtener Temas
        Set<String> topics = controller.getAvailableTopics();
        if (topics.isEmpty()) {
            System.out.println("No hay preguntas suficientes para generar un examen.");
            return;
        }

        System.out.println("Temas disponibles:");
        List<String> topicList = new ArrayList<>(topics);
        for (int i = 0; i < topicList.size(); i++) {
            System.out.println((i + 1) + ". " + topicList.get(i));
        }
        System.out.println((topicList.size() + 1) + ". TODOS");

        int topicOpt = Esdia.readInt("Elija tema: ", 1, topicList.size() + 1);
        String selectedTopic = (topicOpt <= topicList.size()) ? topicList.get(topicOpt - 1) : "TODOS";

        //Elegir número de preguntas
        int max = controller.getMaxQuestions(selectedTopic);
        if (max == 0) {
            System.out.println("No hay preguntas de ese tema.");
            return;
        }
        int numQuestions = Esdia.readInt("Número de preguntas (1-" + max + "): ", 1, max);

        //Iniciar Examen
        controller.startExam(selectedTopic, numQuestions);
        Exam exam = controller.getCurrentExam();
        List<Question> questions = exam.getQuestions();

        System.out.println("\n¡Examen iniciado! (" + numQuestions + " preguntas)");
        
        //Bucle de preguntas
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            System.out.println("\nPregunta " + (i + 1) + "/" + numQuestions + ":");
            System.out.println(q.getStatement());
            
            List<Option> options = q.getOptions();
            for (int j = 0; j < options.size(); j++) {
                System.out.println("   " + (j + 1) + ") " + options.get(j).getText());
            }
            System.out.println("   5) Saltar pregunta (No contestar)");

            int answer = Esdia.readInt("Su respuesta: ", 1, 5);

            if (answer != 5) {
                // Registrar respuesta (índice 0-3)
                int optionIndex = answer - 1;
                controller.answerExamQuestion(i, optionIndex);
                
                // Feedback inmediato
                Option selected = options.get(optionIndex);
                if (selected.isCorrect()) {
                    System.out.println(">> ¡CORRECTO! " + selected.getRationale());
                } else {
                    System.out.println(">> INCORRECTO. " + selected.getRationale());
                }
            } else {
                System.out.println(">> Pregunta saltada.");
            }
        }

        //Resultados
        Exam.ExamResult result = controller.finishExam();
        System.out.println("\n--- RESULTADOS DEL EXAMEN ---");
        System.out.printf("Nota final: %.2f / 10.0%n", result.grade);
        System.out.println("Aciertos: " + result.correct);
        System.out.println("Fallos: " + result.incorrect + " (Penalización aplicada)");
        System.out.println("No contestadas: " + result.unanswered);
        System.out.println("Tiempo empleado: " + result.durationSeconds + " segundos");
        
        Esdia.readString("Pulse Enter para volver al menú...");
    }

    //--- SUBMENÚ IA ---

    private void menuAI() {
        System.out.println("\n--- GENERADOR IA (Gemini) ---");
        List<QuestionCreator> creators = controller.getQuestionCreators();
        
        // Listar modelos disponibles
        for (int i = 0; i < creators.size(); i++) {
            System.out.println((i + 1) + ". " + creators.get(i).getDescription());
        }
        
        int idx = Esdia.readInt("Elija modelo: ", 1, creators.size()) - 1;
        String topic = Esdia.readString("¿Sobre qué tema? (ej. Polimorfismo en Java): ");
        
        // Llamada síncrona (bloqueante)
        Question q = controller.generateQuestion(idx, topic);
        
        if (q != null) {
            System.out.println("\n--- PREGUNTA GENERADA ---");
            System.out.println(q.getStatement());
            for (Option opt : q.getOptions()) {
                System.out.println(" - " + opt.getText() + (opt.isCorrect() ? " [CORRECTA]" : ""));
                System.out.println("   (" + opt.getRationale() + ")");
            }
            
            if (Esdia.yesOrNo("¿Guardar en el banco?")) {
                controller.saveGeneratedQuestion(q);
                showMessage("Guardada.");
            } else {
                showMessage("Descartada.");
            }
        }
    }

    //// Overrides de BaseView

    @Override
    public void showMessage(String msg) {
        System.out.println(">> " + msg);
    }

    @Override
    public void showErrorMessage(String errorMsg) {
        System.err.println("!! " + errorMsg);
    }

    @Override
    public void end() {
        System.out.println("Saliendo de Examinator 3000...");
    }
}

