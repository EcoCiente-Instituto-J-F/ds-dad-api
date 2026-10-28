package dad.core.domain;

import java.util.List;

public record Catalogos(
    List<Opcao> tiposUsuarios,
    List<Opcao> tiposCondominios,
    List<Categoria> categoriasResiduos,
    List<DiaSemana> diasSemana,
    List<Opcao> statusAgendamentos,
    List<Sindico> sindicos) {

  public record Opcao(
      Integer id,
      String nome) {
  }

  public record Categoria(
      Integer id,
      String nome,
      boolean permiteReciclagem,
      String cor,
      Integer pontosBase,
      Integer limitePontosDiario) {
  }

  public record DiaSemana(
      Integer id,
      String nome,
      Integer numero) {
  }

  public record Sindico(
      Integer id,
      Integer usuarioId,
      String nome) {
  }
}