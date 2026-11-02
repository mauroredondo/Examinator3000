package model.backup;

import model.Question;
import java.util.List;


public interface QuestionBackupIO {
    void exportQuestions(List<Question> questions, String filename) throws QuestionBackupIOException;
    List<Question> importQuestions(String filename) throws QuestionBackupIOException;
}
