package model.automatic;

import model.Question;


public interface QuestionCreator {
    Question createQuestion(String topic) throws QuestionCreatorException;
    String getDescription();
}
