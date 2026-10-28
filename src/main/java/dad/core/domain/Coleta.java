package dad.core.domain;

import java.time.OffsetDateTime;
import java.util.List;

public record Coleta(
    Integer id,
    Integer condominioId,
    Integer cooperativaId,
    Integer statusId,
    String status,
    OffsetDateTime dataInicio,
    OffsetDateTime dataFim,
    boolean recorrente,
    List<Integer> diasSemanaIds,
    List<Visita> visitas) {

  public record Dados(
      Integer condominioId,
      Integer cooperativaId,
      Integer statusId,
      OffsetDateTime dataInicio,
      OffsetDateTime dataFim,
      boolean recorrente,
      List<Integer> diasSemanaIds) {
  }

  public record RegistroVisita(
      OffsetDateTime data,
      boolean realizada,
      boolean confirmada,
      String observacao) {
  }

  public record Visita(
      Integer id,
      OffsetDateTime data,
      boolean realizada,
      boolean confirmada,
      OffsetDateTime confirmadoEm,
      String observacao) {
  }
}