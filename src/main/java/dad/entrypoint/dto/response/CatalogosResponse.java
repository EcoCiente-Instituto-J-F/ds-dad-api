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
      String nome,
      @io.swagger.v3.oas.annotations.media.Schema(
              description =
                  "IDs existentes equivalentes a esta opção. Permite exibir usuários legados na opção Administrador.")
          List<Integer> idsEquivalentes) {
    public OpcaoResponse(Integer id, String nome) {
      this(id, nome, List.of(id));
    }
  }

  public record CategoriaResponse(
      Integer id,
      String nome,
      boolean permiteReciclagem,
      String cor,
      Integer pontosBase,
      Integer limitePontosDiario) {}

  public record DiaSemanaResponse(Integer id, String nome, Integer numero) {}

  public record SindicoResponse(Integer id, Integer usuarioId, String nome) {}
}
