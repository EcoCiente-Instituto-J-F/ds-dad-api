package dad.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

class ContratosCrudIntegrationTest extends AbstractIntegrationTest {
  @org.springframework.test.context.bean.override.mockito.MockitoSpyBean
  private dad.core.gateway.UsuarioGateway usuarioGateway;

  @Test
  void revertePrimeiroCadastroQuandoSegundoFalhaDuranteGravacao() throws Exception {
    long antesUsuarios = contar("SELECT COUNT(*) FROM tb_usuarios");
    long antesEnderecos = contar("SELECT COUNT(*) FROM tb_enderecos");
    org.mockito.Mockito.doThrow(
            new dad.core.exception.RegraNegocioException("Falha de gravação simulada"))
        .when(usuarioGateway)
        .criar(
            org.mockito.ArgumentMatchers.argThat(
                u -> u != null && u.email().equals("falha@teste.com")));
    chamar(
            HttpMethod.POST,
            IMPORTACAO,
            dados(
                "usuarios",
                List.of(novoUsuario("primeiro@teste.com"), novoUsuario("falha@teste.com"))))
        .andExpect(status().isConflict());
    assertEquals(antesUsuarios, contar("SELECT COUNT(*) FROM tb_usuarios"));
    assertEquals(antesEnderecos, contar("SELECT COUNT(*) FROM tb_enderecos"));
  }

  @Test
  void dashboardContaRecorrenciaIniciadaAntesDeHoje() throws Exception {
    var hoje = java.time.LocalDate.now(ZONA);
    var inicio = hoje.minusWeeks(4).atTime(9, 0).atZone(ZONA).toOffsetDateTime();
    programar(inicio.toString(), null, true, List.of(idDia(hoje.getDayOfWeek())));
    var resposta =
        json(chamar(HttpMethod.GET, "/api/admin/v1/dashboard", null).andExpect(status().isOk()));
    assertEquals(1, resposta.get("coletasHoje").asLong());
  }

  private static final String COLETAS = "/api/admin/v1/coletas";
  private static final String IMPORTACAO = "/api/admin/v1/usuarios/importacao";

  private JsonNode programar(String inicio, String fim, boolean recorrente, List<Integer> dias)
      throws Exception {
    var dados = coleta();
    dados.put("dataInicio", inicio);
    dados.put("dataFim", fim);
    dados.put("recorrente", recorrente);
    dados.put("diasSemanaIds", dias);
    return criar(COLETAS, dados);
  }

  private JsonNode calendario(String parametros) throws Exception {
    return json(
        chamar(HttpMethod.GET, COLETAS + "/calendario?" + parametros, null)
            .andExpect(status().isOk()));
  }

  @Test
  void incluiRecorrenciaAnteriorAoMesERespeitaTermino() throws Exception {
    var coleta =
        programar(
            "2026-10-05T12:00:00Z", "2026-11-16T12:00:00Z", true, List.of(idDia(DayOfWeek.MONDAY)));
    var resposta = calendario("inicio=2026-11-01T00:00:00Z&fim=2026-12-01T00:00:00Z");
    assertEquals(3, resposta.get("totalItens").asLong());
    assertEquals(50, resposta.get("tamanho").asInt());
    assertEquals(coleta.get("id").asInt(), resposta.get("itens").get(2).get("id").asInt());
    assertEquals(
        OffsetDateTime.parse("2026-11-16T12:00:00Z").toInstant(),
        OffsetDateTime.parse(resposta.get("itens").get(2).get("ocorrenciaEm").asText())
            .toInstant());
  }

  @Test
  void filtraAntesDeContarEPaginaOcorrenciasComOrdemEstavel() throws Exception {
    var c1 = programar("2026-10-05T12:00:00Z", null, true, List.of(idDia(DayOfWeek.MONDAY)));
    var c2 = programar("2026-11-02T12:00:00Z", null, false, List.of());
    var outro = coleta();
    outro.put("dataInicio", "2026-11-02T12:00:00Z");
    outro.put("dataFim", null);
    outro.put("statusId", statusCancelado);
    criar(COLETAS, outro);
    String filtros =
        "inicio=2026-11-01T00:00:00Z&fim=2026-11-10T00:00:00Z&condominioId="
            + condominioId
            + "&cooperativaId="
            + cooperativaId
            + "&statusId="
            + statusAgendado
            + "&tamanho=1";
    var primeira = calendario(filtros);
    var segunda = calendario(filtros + "&pagina=1");
    var terceira = calendario(filtros + "&pagina=2");
    assertEquals(3, primeira.get("totalItens").asLong());
    assertEquals(3, primeira.get("totalPaginas").asInt());
    assertEquals(c1.get("id").asInt(), primeira.get("itens").get(0).get("id").asInt());
    assertEquals(c2.get("id").asInt(), segunda.get("itens").get(0).get("id").asInt());
    assertEquals(c1.get("id").asInt(), terceira.get("itens").get(0).get("id").asInt());
    assertEquals(0, calendario(filtros + "&pagina=99").get("itens").size());
  }

  @Test
  void calculaDiaDaSemanaNoFusoDeNegocio() throws Exception {
    programar("2026-10-05T02:30:00Z", null, true, List.of(idDia(DayOfWeek.SUNDAY)));
    var resposta = calendario("inicio=2026-11-02T00:00:00Z&fim=2026-11-02T03:00:00Z");
    assertEquals(1, resposta.get("totalItens").asLong());
    assertEquals(
        OffsetDateTime.parse("2026-11-02T02:30:00Z").toInstant(),
        OffsetDateTime.parse(resposta.get("itens").get(0).get("ocorrenciaEm").asText())
            .toInstant());
  }

  @Test
  void fimExclusivoNaoDuplicaOcorrenciaEntrePeriodos() throws Exception {
    programar("2026-11-02T12:00:00Z", null, false, List.of());
    assertEquals(
        0,
        calendario("inicio=2026-11-01T00:00:00Z&fim=2026-11-02T12:00:00Z")
            .get("totalItens")
            .asLong());
    assertEquals(
        1,
        calendario("inicio=2026-11-02T12:00:00Z&fim=2026-11-03T00:00:00Z")
            .get("totalItens")
            .asLong());
  }

  @Test
  void naoGeraOcorrenciaAposHorarioFinalDaRecorrencia() throws Exception {
    programar(
        "2026-10-05T12:00:00Z", "2026-11-09T11:00:00Z", true, List.of(idDia(DayOfWeek.MONDAY)));
    assertEquals(
        1,
        calendario("inicio=2026-11-01T00:00:00Z&fim=2026-11-10T00:00:00Z")
            .get("totalItens")
            .asLong());
  }

  @Test
  void validaParametrosDoCalendario() throws Exception {
    chamar(
            HttpMethod.GET,
            COLETAS + "/calendario?inicio=2026-11-02T12:00:00Z&fim=2026-11-01T12:00:00Z",
            null)
        .andExpect(status().isBadRequest());
    chamar(HttpMethod.GET, COLETAS + "/calendario?inicio=2026-11-02&fim=2026-11-03", null)
        .andExpect(status().isBadRequest());
    chamar(HttpMethod.GET, COLETAS + "/calendario", null).andExpect(status().isBadRequest());
  }

  private Map<String, Object> novoUsuario(String email) {
    var u = usuario();
    u.put("email", email);
    u.put("cpf", null);
    return u;
  }

  @Test
  void importaLoteComSenhaCriptografada() throws Exception {
    var resposta =
        json(
            chamar(
                    HttpMethod.POST,
                    IMPORTACAO,
                    dados(
                        "usuarios",
                        List.of(novoUsuario("lote1@teste.com"), novoUsuario("lote2@teste.com"))))
                .andExpect(status().isOk()));
    assertEquals(2, resposta.get("importados").asInt());
    var hash =
        jdbc.queryForObject(
            "SELECT senha_hash FROM tb_usuarios WHERE email_usuario = ?",
            String.class,
            "lote1@teste.com");
    assertTrue(
        new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
            .matches(SENHA, hash));
    assertFalse(resposta.toString().contains("senha"));
  }

  @Test
  void rejeitaDuplicidadesDoLoteComIndices() throws Exception {
    var resposta =
        json(
            chamar(
                    HttpMethod.POST,
                    IMPORTACAO,
                    dados(
                        "usuarios",
                        List.of(
                            novoUsuario("repetido@teste.com"), novoUsuario("REPETIDO@teste.com"))))
                .andExpect(status().isBadRequest()));
    assertTrue(resposta.get("campos").has("usuarios.0.email"));
    assertTrue(resposta.get("campos").has("usuarios.1.email"));
    assertEquals(
        0, contar("SELECT COUNT(*) FROM tb_usuarios WHERE email_usuario = 'repetido@teste.com'"));
  }

  @Test
  void agregaEmailDoBancoETipoInvalidoSemCadastrarNinguem() throws Exception {
    long antes = contar("SELECT COUNT(*) FROM tb_usuarios");
    var invalido = novoUsuario("invalido@teste.com");
    invalido.put("tipoUsuarioId", 999999);
    var resposta =
        json(
            chamar(
                    HttpMethod.POST,
                    IMPORTACAO,
                    dados(
                        "usuarios",
                        List.of(
                            novoUsuario("valido@teste.com"), novoUsuario(ADMIN_EMAIL), invalido)))
                .andExpect(status().isBadRequest()));
    assertTrue(resposta.get("campos").has("usuarios.1.email"));
    assertTrue(resposta.get("campos").has("usuarios.2.tipoUsuarioId"));
    assertEquals(antes, contar("SELECT COUNT(*) FROM tb_usuarios"));
  }

  @Test
  void validaItensComMesmoContratoIndividualEIndices() throws Exception {
    var invalido = novoUsuario("invalido");
    var resposta =
        json(
            chamar(HttpMethod.POST, IMPORTACAO, dados("usuarios", List.of(invalido)))
                .andExpect(status().isBadRequest()));
    assertTrue(resposta.get("campos").has("usuarios.0.email"));
  }

  @Test
  void validaLimitesDoLote() throws Exception {
    chamar(HttpMethod.POST, IMPORTACAO, dados("usuarios", List.of()))
        .andExpect(status().isBadRequest());
    chamar(
            HttpMethod.POST,
            IMPORTACAO,
            dados("usuarios", java.util.Collections.nCopies(101, novoUsuario("teste@teste.com"))))
        .andExpect(status().isBadRequest());
  }

  @Test
  void importaMoradorComVinculoNaMesmaTransacao() throws Exception {
    var morador = novoUsuario("morador.lote@teste.com");
    morador.put("tipoUsuarioId", tipoMorador);
    morador.put("condominioId", condominioId);
    chamar(HttpMethod.POST, IMPORTACAO, dados("usuarios", List.of(morador)))
        .andExpect(status().isOk());
    assertEquals(
        1,
        contar(
            "SELECT COUNT(*) FROM tb_moradores m JOIN tb_usuarios u ON u.id_usuario = m.usuario_id WHERE u.email_usuario = 'morador.lote@teste.com' AND m.condominio_id = ?",
            condominioId));
  }

  @Test
  void apenasAdministradoresPodemImportarEConsultarCalendario() throws Exception {
    String comum = token(comumId, "comum@teste.com", "ADMIN");
    chamar(
            HttpMethod.POST,
            IMPORTACAO,
            dados("usuarios", List.of(novoUsuario("lote@teste.com"))),
            comum)
        .andExpect(status().isForbidden());
    chamar(
            HttpMethod.GET,
            COLETAS + "/calendario?inicio=2026-11-01T00:00:00Z&fim=2026-12-01T00:00:00Z",
            null,
            comum)
        .andExpect(status().isForbidden());
    chamar(
            HttpMethod.POST,
            IMPORTACAO,
            dados("usuarios", List.of(novoUsuario("lote@teste.com"))),
            null)
        .andExpect(status().isUnauthorized());
  }

  @Test
  void quizRetornaTentativasEPreservaBloqueiosDoHistorico() throws Exception {
    var curso = criarCurso();
    int cursoId = curso.get("id").asInt();
    int aulaId = curso.get("aulas").get(0).get("id").asInt();
    var quiz = criar("/api/admin/v1/quizzes", quiz(cursoId, aulaId));
    int quizId = quiz.get("id").asInt();
    assertFalse(quiz.get("possuiTentativas").asBoolean());
    assertEquals(0, quiz.get("totalTentativas").asLong());
    jdbc.update(
        "INSERT INTO tb_tentativas_quiz (usuario_id, quiz_id) VALUES (?, ?)", comumId, quizId);
    var detalhe =
        json(
            chamar(HttpMethod.GET, "/api/admin/v1/quizzes/" + quizId, null)
                .andExpect(status().isOk()));
    assertTrue(detalhe.get("possuiTentativas").asBoolean());
    assertEquals(1, detalhe.get("totalTentativas").asLong());
    var alteracao = quiz(cursoId, aulaId);
    alteracao.put("titulo", "Título permitido");
    alteracao.put("ativo", false);
    chamar(HttpMethod.PUT, "/api/admin/v1/quizzes/" + quizId, alteracao).andExpect(status().isOk());
    alteracao.put("pontos", 99);
    chamar(HttpMethod.PUT, "/api/admin/v1/quizzes/" + quizId, alteracao)
        .andExpect(status().isConflict());
    chamar(HttpMethod.DELETE, "/api/admin/v1/quizzes/" + quizId, null)
        .andExpect(status().isConflict());
  }

  @Test
  void agrupaTiposAdminNoCatalogoEFiltraTodosOsIdsEquivalentes() throws Exception {
    int legado = inserirTipo("Admin");
    int feminina = inserirTipo("Administradora");
    inserirUsuario("Admin legado", "legado@teste.com", legado);
    inserirUsuario("Admin feminina", "feminina@teste.com", feminina);
    var catalogos =
        json(chamar(HttpMethod.GET, "/api/admin/v1/catalogos", null).andExpect(status().isOk()));
    int quantidade = 0;
    for (var tipo : catalogos.get("tiposUsuarios")) {
      if (tipo.get("nome").asText().equals("Administrador")) {
        quantidade++;
        assertEquals(3, tipo.get("idsEquivalentes").size());
      }
      assertNotEquals("Admin", tipo.get("nome").asText());
    }
    assertEquals(1, quantidade);
    for (int tipo : List.of(tipoAdmin, legado, feminina)) {
      var lista =
          json(
              chamar(HttpMethod.GET, "/api/admin/v1/usuarios?tipoUsuarioId=" + tipo, null)
                  .andExpect(status().isOk()));
      assertEquals(3, lista.get("totalItens").asLong());
    }
    var legadoRequest = novoUsuario("novo.legado@teste.com");
    legadoRequest.put("tipoUsuarioId", legado);
    var criado = criar("/api/admin/v1/usuarios", legadoRequest);
    assertEquals(legado, criado.get("tipoUsuarioId").asInt());
    assertEquals("Administrador", criado.get("tipoUsuario").asText());
    String tokenLegado = token(criado.get("id").asInt(), "novo.legado@teste.com", "ADMIN");
    chamar(HttpMethod.GET, "/api/admin/v1/catalogos", null, tokenLegado).andExpect(status().isOk());
  }
}
