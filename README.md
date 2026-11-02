# Examinator 3000

Aplicación de consola en **Java 21** para crear y gestionar un banco de preguntas tipo test, hacer exámenes con ellas y **generar preguntas automáticamente con IA (Google Gemini)**. Proyecto realizado en la asignatura de Programación de la Universidad de Salamanca.

## Qué hace

- **Banco de preguntas (CRUD):** crear, listar, ver, modificar y borrar preguntas. Cada pregunta tiene autor, temas, enunciado y 4 opciones, cada una con su justificación y si es correcta.
- **Modo examen:** eliges un tema y un número de preguntas, respondes y obtienes la nota (sobre 10, con penalización de 1/3 por fallo).
- **Importar / exportar JSON:** copia de seguridad del banco de preguntas con Gson.
- **Generación con IA:** pide a Gemini una pregunta difícil sobre un tema. La respuesta llega como JSON estructurado (esquema generado a partir de una clase Java), se valida (exactamente 4 opciones y una sola correcta) y se puede guardar en el banco.
- **Persistencia:** el banco se guarda en un fichero binario mediante serialización de Java.

## Arquitectura

Patrón **MVC**, con interfaces para desacoplar las partes:

```
src/
├── app/         App.java (punto de entrada)
├── controller/  Controller.java
├── model/       Model, Question, Option, Exam
│   ├── repository/  IRepository, BinaryRepository
│   ├── backup/      QuestionBackupIO, JSONQuestionBackupIO
│   └── automatic/   QuestionCreator, GeminiQuestionCreator
└── view/        BaseView, InteractiveView (menú de consola)
```

`QuestionCreator`, `IRepository` y `QuestionBackupIO` son interfaces, de modo que se puede cambiar el modelo de IA, el almacenamiento o el formato de copia sin tocar el resto.

## Cómo ejecutarlo

```bash
# Sin IA
java -jar Examinator3000_RedondoMauro.jar

# Con generación de preguntas con Gemini
java -jar Examinator3000_RedondoMauro.jar -question-creator <modelo-gemini> <TU_API_KEY>
```

Necesitas una clave de API de Gemini (se puede obtener gratis en Google AI Studio).

## Tecnologías

Java 21 · POO · MVC · Serialización · JSON (Gson) · API de Gemini · Salida estructurada con JSON Schema

## Nota sobre la librería de IA

La conexión con Gemini se hace mediante una librería proporcionada por la asignatura (`es.usal.genai`). La parte desarrollada por mí es el prompt, el esquema de la pregunta, la validación de la respuesta y la integración en la arquitectura MVC.

## Qué aprendí

- Diseñar una aplicación por capas con interfaces para poder cambiar componentes.
- Integrar un LLM en una aplicación real: construir el prompt, forzar una respuesta en JSON con esquema y validar la salida antes de usarla.
- Gestionar la persistencia y la copia de seguridad de los datos.
