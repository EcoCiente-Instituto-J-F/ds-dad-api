package dad.core.domain;

import java.time.OffsetDateTime;
import java.util.List;

public record Dashboard(
    OffsetDateTime geradoEm,
    long usuarios,
    long usuariosAtivos,
    long condominios,
    long condominiosAtivos,
    long cooperativas,
    long cursos,
    long quizzes,
    long agendamentos,
    long usuariosCadastradosHoje,
    long postagensHoje,
    List<Mes> evolucao,
    List<UsuarioRecente> usuariosRecentes) {

  public record Mes(
      String mes,
      long usuariosNovos,
      long cooperativasNovas,
      long postagens) {
  }

  public record UsuarioRecente(
      Integer id,
      String nome,
      String email,
      String tipo,
      boolean ativo,
      OffsetDateTime registroEm) {
  }
}