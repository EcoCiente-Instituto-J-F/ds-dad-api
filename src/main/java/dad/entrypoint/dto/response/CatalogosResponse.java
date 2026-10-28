package dad.entrypoint.dto.response;

import java.util.List;

public record CatalogosResponse(
    List<OpcaoResponse> tiposUsuarios,
    List<OpcaoResponse> tiposCondominios,
    List<CategoriaResponse> categoriasResiduos,
    List<DiaSemanaResponse> diasSemana,
    List<OpcaoResponse> statusAgendamentos,
    List<SindicoResponse> sindicos) {

  public record OpcaoResponse(
      Integer id,
      String nome) {
  }

  public record CategoriaResponse(
      Integer id,
      String nome,
      boolean permiteReciclagem,
      String cor,
      Integer pontosBase,
      Integer limitePontosDiario) {
  }

  public record DiaSemanaResponse(
      Integer id,
      String nome,
      Integer numero) {
  }

  public record SindicoResponse(
      Integer id,
      Integer usuarioId,
      String nome) {
  }
}