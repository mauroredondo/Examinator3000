# Examinator 3000

🇬🇧 English | 🇪🇸 [Español](README.es.md)

A console application in Java for building a bank of multiple-choice questions, taking practice exams from it and, optionally, generating new questions with Google Gemini.

It is organised as MVC with small interfaces for storage, backup and question generation, so each piece can be replaced without touching the rest.

## Features

- **Question bank (CRUD).** Create, list, edit and delete questions. Each question has an author, one or more topics, a statement and exactly four options, one of them correct, each with a short rationale.
- **Exam mode.** Choose a topic (or all of them) and a number of questions. Questions are drawn at random. After each answer you see whether it was right and why. Scoring: +1 for a correct answer, −1/3 for a wrong one, 0 if skipped. The final grade is out of 10 and never goes below 0.
- **JSON import and export.** Back up the bank or share it. Imported questions are validated: invalid ones are discarded and questions that already exist (same ID) are skipped. The program reports how many of each.
- **AI question generation (optional).** Asks Gemini for a question on a topic, using structured JSON output. The answer is checked (four options, exactly one correct) before you decide whether to keep it.
- **Automatic saving.** Every change is written to disk straight away, using a temporary file so an interrupted write cannot leave the data file half written.

## Requirements

- Java 17 or later (developed and tested with Java 21).
- The libraries in `lib/`:
  - `gson-2.10.1.jar`: JSON.
  - `biblioteca11.jar`: console input helpers (`com.coti.tools.Esdia`).
  - `genai-fatjar-1.0.0.jar`: a facade over the Google GenAI SDK (`es.usal.genai`).
- A Gemini API key, only if you want to use question generation.

## Build and run

From the project folder (on Windows, use `;` instead of `:` in the classpath):

```bash
mkdir -p bin
javac -cp "lib/*" -d bin $(find src -name '*.java')
java -cp "bin:lib/*" app.App
```

To enable question generation, pass a model ID and your API key:

```bash
java -cp "bin:lib/*" app.App -question-creator <model-id> <api-key>
```

Check Google's documentation for the current model IDs. When the AI is active, the main menu shows an extra option to generate questions. Keep in mind that a key passed on the command line stays in your shell history and is visible to other processes on the machine.

In VS Code, opening the folder with the Java extension pack is enough: `.vscode/settings.json` already points to `src` and `lib`.

## Where the data lives

| File | Purpose |
|---|---|
| `~/questions.bin` | The question bank (Java serialization). Created on first run. |
| `~/<name>.json` | Files created by *Export*. *Import* reads from the same folder. |

If `questions.bin` cannot be read (for example, after the `Question` class changes), the program does not overwrite it. It renames it to `questions.bin.corrupto-<timestamp>` and starts with an empty bank, so the old data can still be recovered by hand.

### JSON format

```json
[
  {
    "id": "6f1c0a52-2b0e-4a52-9a47-0b6f6a1d2f10",
    "author": "Mauro",
    "topics": ["POLYMORPHISM"],
    "statement": "What does dynamic dispatch decide?",
    "options": [
      { "text": "Which method runs, based on the object's real type", "rationale": "Resolved at run time.", "correct": true },
      { "text": "Which class is loaded first", "rationale": "Unrelated to dispatch.", "correct": false },
      { "text": "The memory layout of the object", "rationale": "Not what dispatch decides.", "correct": false },
      { "text": "The order of the constructors", "rationale": "Constructors follow inheritance order.", "correct": false }
    ]
  }
]
```

A question is accepted on import only if it has a statement, four options with text, exactly one correct option and at least one topic. Topics are converted to upper case.

## Structure

```
src/
  app/          App.java: reads the arguments and wires everything together
  controller/   Controller.java: connects the view with the model
  model/
    Model.java, Question.java, Option.java
    exam/       Exam.java: answers, scoring and timing
    repository/ IRepository + BinaryRepository
    backup/     QuestionBackupIO + JSONQuestionBackupIO
    automatic/  QuestionCreator + GeminiQuestionCreator
  view/         BaseView + InteractiveView (console menus)
lib/            third-party and course-provided jars
```

The model depends only on the three interfaces (`IRepository`, `QuestionBackupIO`, `QuestionCreator`), which `App` injects. Adding another storage format or another AI provider means writing a new implementation of one of them.

## Limitations

- There are no automated tests yet.
- The interface is text-only, and the texts of the program are in Spanish.
- Generated questions are not verified beyond their structure: the program does not check that the marked answer is actually correct, nor that the question is not a duplicate.
- The binary file depends on the `Question` class, so it is tied to a given version of the code. JSON export is the portable backup.
- Options are shown in the stored order, not shuffled.

## Author

**Mauro Redondo Beltrán** · BSc in Mathematics (University of Salamanca) · MSc in Artificial Intelligence (La Salle - URL)
