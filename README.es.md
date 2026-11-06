# Examinator 3000

🇬🇧 [English](README.md) | 🇪🇸 Español

Aplicación de consola en Java para crear un banco de preguntas tipo test, hacer exámenes de práctica con él y, de forma opcional, generar preguntas nuevas con Google Gemini.

Está organizada en MVC, con interfaces pequeñas para el almacenamiento, la copia de seguridad y la generación de preguntas, de modo que cada pieza se puede cambiar sin tocar el resto.

## Qué hace

- **Banco de preguntas (CRUD).** Crear, listar, editar y borrar preguntas. Cada pregunta tiene autor, uno o más temas, un enunciado y exactamente cuatro opciones, una de ellas correcta, cada una con una justificación breve.
- **Modo examen.** Eliges un tema (o todos) y un número de preguntas. Se sacan al azar. Después de cada respuesta ves si era correcta y por qué. Puntuación: +1 por acierto, −1/3 por fallo y 0 si se salta. La nota final es sobre 10 y nunca baja de 0.
- **Importar y exportar JSON.** Sirve para hacer copias de seguridad o compartir el banco. Las preguntas importadas se validan: las inválidas se descartan y las que ya existen (mismo ID) se omiten. El programa indica cuántas hay de cada tipo.
- **Generación con IA (opcional).** Pide a Gemini una pregunta sobre un tema, con salida JSON estructurada. Se comprueba la respuesta (cuatro opciones, exactamente una correcta) antes de que decidas si la guardas.
- **Guardado automático.** Cada cambio se escribe en disco al momento, usando un fichero temporal para que una escritura interrumpida no deje el archivo de datos a medias.

## Requisitos

- Java 17 o superior (desarrollado y probado con Java 21).
- Las librerías de `lib/`:
  - `gson-2.10.1.jar`: JSON.
  - `biblioteca11.jar`: utilidades de entrada por consola (`com.coti.tools.Esdia`).
  - `genai-fatjar-1.0.0.jar`: una fachada sobre el SDK de Google GenAI (`es.usal.genai`).
- Una clave de API de Gemini, solo si quieres usar la generación de preguntas.

## Compilar y ejecutar

Desde la carpeta del proyecto (en Windows, usa `;` en lugar de `:` en el classpath):

```bash
mkdir -p bin
javac -cp "lib/*" -d bin $(find src -name '*.java')
java -cp "bin:lib/*" app.App
```

Para activar la generación de preguntas, pasa un identificador de modelo y tu clave de API:

```bash
java -cp "bin:lib/*" app.App -question-creator <id-del-modelo> <clave-api>
```

Consulta la documentación de Google para ver los identificadores de modelo vigentes. Con la IA activa, el menú principal muestra una opción más para generar preguntas. Ten en cuenta que una clave pasada por línea de comandos queda en el historial de la shell y es visible para otros procesos de la máquina.

En VS Code basta con abrir la carpeta con el paquete de extensiones de Java: `.vscode/settings.json` ya apunta a `src` y `lib`.

## Dónde se guardan los datos

| Archivo | Para qué sirve |
|---|---|
| `~/questions.bin` | El banco de preguntas (serialización de Java). Se crea la primera vez que se ejecuta. |
| `~/<nombre>.json` | Archivos creados con *Exportar*. *Importar* lee de la misma carpeta. |

Si `questions.bin` no se puede leer (por ejemplo, tras cambiar la clase `Question`), el programa no lo sobrescribe. Lo renombra a `questions.bin.corrupto-<fecha>` y empieza con un banco vacío, así que los datos antiguos se pueden recuperar a mano.

### Formato JSON

```json
[
  {
    "id": "6f1c0a52-2b0e-4a52-9a47-0b6f6a1d2f10",
    "author": "Mauro",
    "topics": ["POLIMORFISMO"],
    "statement": "¿Qué decide el enlace dinámico?",
    "options": [
      { "text": "Qué método se ejecuta, según el tipo real del objeto", "rationale": "Se resuelve en tiempo de ejecución.", "correct": true },
      { "text": "Qué clase se carga primero", "rationale": "No tiene relación con el enlace.", "correct": false },
      { "text": "La disposición del objeto en memoria", "rationale": "No es lo que decide el enlace.", "correct": false },
      { "text": "El orden de los constructores", "rationale": "Los constructores siguen el orden de herencia.", "correct": false }
    ]
  }
]
```

Al importar, una pregunta solo se acepta si tiene enunciado, cuatro opciones con texto, exactamente una correcta y al menos un tema. Los temas se pasan a mayúsculas.

## Estructura

```
src/
  app/          App.java: lee los argumentos y conecta todo
  controller/   Controller.java: une la vista con el modelo
  model/
    Model.java, Question.java, Option.java
    exam/       Exam.java: respuestas, puntuación y tiempo
    repository/ IRepository + BinaryRepository
    backup/     QuestionBackupIO + JSONQuestionBackupIO
    automatic/  QuestionCreator + GeminiQuestionCreator
  view/         BaseView + InteractiveView (menús de consola)
lib/            librerías de terceros y las facilitadas en la asignatura
```

El modelo solo depende de las tres interfaces (`IRepository`, `QuestionBackupIO`, `QuestionCreator`), que `App` inyecta. Añadir otro formato de almacenamiento u otro proveedor de IA consiste en escribir una nueva implementación de una de ellas.

## Limitaciones

- Todavía no hay tests automáticos.
- La interfaz es solo de texto.
- Las preguntas generadas solo se verifican en su estructura: el programa no comprueba que la respuesta marcada sea realmente la correcta ni que la pregunta no esté repetida.
- El archivo binario depende de la clase `Question`, así que va ligado a una versión del código. La exportación a JSON es la copia de seguridad portable.
- Las opciones se muestran en el orden guardado, sin barajar.

## Autor

**Mauro Redondo Beltrán** · Grado en Matemáticas (Universidad de Salamanca) · Máster en Inteligencia Artificial (La Salle - URL)
