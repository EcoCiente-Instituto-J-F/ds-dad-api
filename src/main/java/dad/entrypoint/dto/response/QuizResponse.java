package dad.entrypoint.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record QuizResponse(
    Integer id,
    Integer cursoId,
    Integer aulaId,
    String titulo,
    BigDecimal notaMinima,
    Integer pontos,
    boolean ativo,
    OffsetDateTime criadoEm,
    List<PerguntaResponse> perguntas,
    @io.swagger.v3.oas.annotations.media.Schema(
            description = "Indica histórico de tentativas e bloqueio da estrutura.")
        boolean possuiTentativas,
    @io.swagger.v3.oas.annotations.media.Schema(
            description = "Quantidade de tentativas, inclusive não finalizadas.")
        long totalTentativas) {

  public record PerguntaResponse(
      Integer id,
      Integer quizId,
      String enunciado,
      Integer ordem,
      List<AlternativaResponse> alternativas) {}

  public record AlternativaResponse(
      Integer id, Integer perguntaId, String texto, boolean correta) {}
}
