package dad.entrypoint.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public record ColetaResponse(
    Integer id,
    Integer condominioId,
    Integer cooperativaId,
    Integer statusId,
    String status,
    OffsetDateTime dataInicio,
    OffsetDateTime dataFim,
    boolean recorrente,
    List<Integer> diasSemanaIds,
    List<VisitaResponse> visitas) {

  public record VisitaResponse(
      Integer id,
      OffsetDateTime data,
      boolean realizada,
      boolean confirmada,
      OffsetDateTime confirmadoEm,
      String observacao) {
  }
}