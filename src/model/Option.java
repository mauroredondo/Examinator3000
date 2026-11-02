package model;

import java.io.Serializable;


public class Option implements Serializable {
    private static final long serialVersionUID = 1L;

    private String text;       
    private String rationale; 
    private boolean correct;  

    public Option(String text, String rationale, boolean correct) {
        this.text = text;
        this.rationale = rationale;
        this.correct = correct;
    }


    // Getters y Setters

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public String getRationale() {
        return rationale;
    }
    public void setRationale(String rationale) {
        this.rationale = rationale;
    }

    public boolean isCorrect() {
        return correct;
    }
    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    @Override
    public String toString() {
        return text;
    }
}
