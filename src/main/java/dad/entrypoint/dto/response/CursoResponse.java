package dad.entrypoint.dto.response;

import java.util.List;

public record CursoResponse(
    Integer id,
    String titulo,
    String descricao,
    boolean ativo,
    List<AulaResponse> aulas) {

  public record AulaResponse(
      Integer id,
      Integer cursoId,
      String titulo,
      String conteudo,
      Integer ordem) {
  }
}