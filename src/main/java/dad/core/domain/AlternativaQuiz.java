package dad.core.domain;

public record AlternativaQuiz(
    Integer id,
    Integer perguntaId,
    String texto,
    boolean correta
) {
}