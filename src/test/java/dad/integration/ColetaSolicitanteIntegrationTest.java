package dad.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

class ColetaSolicitanteIntegrationTest extends AbstractIntegrationTest {
  private static final String ROTA = "/api/admin/v1/coletas";

  @Test
  void gravaUsuarioAutenticadoComoSolicitante() throws Exception {
    var coleta = criar(ROTA, coleta());
    assertEquals(
        adminId,
        jdbc.queryForObject(
            "SELECT solicitado_por_usuario_id FROM tb_agendamentos_coletas WHERE id_agendamento_coleta = ?",
            Integer.class,
            coleta.get("id").asInt()));
  }

  @Test
  void outroAdministradorEditaSemTrocarSolicitante() throws Exception {
    var coleta = criar(ROTA, coleta());
    Integer outro = inserirUsuario("Outro administrador", "outro.admin@teste.com", tipoAdmin);
    var dados = coleta();
    dados.put("statusId", statusCancelado);
    chamar(
            HttpMethod.PATCH,
            ROTA + "/" + coleta.get("id").asInt(),
            dados,
            token(outro, "outro.admin@teste.com", "ADMIN"))
        .andExpect(status().isOk());
    assertEquals(
        adminId,
        jdbc.queryForObject(
            "SELECT solicitado_por_usuario_id FROM tb_agendamentos_coletas WHERE id_agendamento_coleta = ?",
            Integer.class,
            coleta.get("id").asInt()));
  }

  @Test
  void naoPermiteSolicitanteNoJson() throws Exception {
    long antes = contar("SELECT COUNT(*) FROM tb_agendamentos_coletas");
    var dados = coleta();
    dados.put("solicitadoPorUsuarioId", comumId);
    chamar(HttpMethod.POST, ROTA, dados).andExpect(status().isBadRequest());
    assertEquals(antes, contar("SELECT COUNT(*) FROM tb_agendamentos_coletas"));
  }

  @Test
  void usuarioDoTokenPrecisaExistirNoBanco() throws Exception {
    long antes = contar("SELECT COUNT(*) FROM tb_agendamentos_coletas");
    chamar(HttpMethod.POST, ROTA, coleta(), token(999999, "ausente@teste.com", "ADMIN"))
        .andExpect(status().isUnauthorized());
    chamar(HttpMethod.POST, ROTA, coleta(), null).andExpect(status().isUnauthorized());
    assertEquals(antes, contar("SELECT COUNT(*) FROM tb_agendamentos_coletas"));
  }
}
