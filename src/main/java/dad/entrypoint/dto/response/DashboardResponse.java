package dad.entrypoint.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public record DashboardResponse(
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
    @io.swagger.v3.oas.annotations.media.Schema(
            description =
                "Ocorrências previstas para o dia em America/Sao_Paulo, incluindo recorrências. Considera todos os status.")
        long coletasHoje,
    List<MesResponse> evolucao,
    List<UsuarioRecenteResponse> usuariosRecentes) {

  public record MesResponse(
      String mes, long usuariosNovos, long cooperativasNovas, long postagens) {}

  public record UsuarioRecenteResponse(
      Integer id,
      String nome,
      String email,
      String tipo,
      boolean ativo,
      OffsetDateTime registroEm) {}
}
