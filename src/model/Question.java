package model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.Set;


public class Question implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;                // Identificador único 
    private String author;          // Autor 
    private HashSet<String> topics; // Temas (Set para evitar duplicados) 
    private String statement;       // Enunciado 
    private List<Option> options;   // Lista de opciones 

    public Question(String author, String statement, List<Option> options, Set<String> rawTopics) {
        this.id = UUID.randomUUID(); // Generado automáticamente 
        this.author = author;
        this.statement = statement;
        this.options = options;
        setTopics(rawTopics);
    }

    // Getters y Setters
    public UUID getId() { return id; }
    
    // El ID no suele modificarse, pero mantenemos setter por si la deserialización JSON lo requiere más tarde
    public void setId(UUID id) { this.id = id; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public HashSet<String> getTopics() { return topics; }
    
    // Normalización de temas a mayúsculas 
    public void setTopics(Set<String> rawTopics) {
        this.topics = new HashSet<>();
        if (rawTopics != null) {
            for (String t : rawTopics) {
                this.topics.add(t.toUpperCase().trim());
            }
        }
    }

    public String getStatement() { return statement; }
    public void setStatement(String statement) { this.statement = statement; }

    public List<Option> getOptions() { return options; }
    public void setOptions(List<Option> options) { this.options = options; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Question question = (Question) o;
        return id.equals(question.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
