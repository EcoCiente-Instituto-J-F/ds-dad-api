package dad.core.domain;

import java.util.List;

public record PerguntaQuiz(
    Integer id,
    Integer quizId,
    String enunciado,
    Integer ordem,
    List<AlternativaQuiz> alternativas
) {
}