package dad.core.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record Quiz(
    Integer id,
    Integer cursoId,
    Integer aulaId,
    String titulo,
    BigDecimal notaMinima,
    Integer pontos,
    boolean ativo,
    OffsetDateTime criadoEm,
    List<PerguntaQuiz> perguntas
) {
}