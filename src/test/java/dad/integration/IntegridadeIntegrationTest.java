package dad.integration;

import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class IntegridadeIntegrationTest
    extends AbstractIntegrationTest {

  @Test
  void deveBloquearAulaDeOutroCursoNoQuiz()
      throws Exception {

    var primeiroCurso = criarCurso();
    var segundoCurso = criarCurso();

    var corpo = quiz(
        primeiroCurso.get("id").asInt(),
        segundoCurso.get("aulas")
            .get(0).get("id").asInt());

    chamar(
        HttpMethod.POST,
        "/api/admin/v1/quizzes",
        corpo).andExpect(status().isConflict());

    assertEquals(
        0,
        contar("SELECT COUNT(*) FROM tb_quizzes"));

    assertEquals(
        0,
        contar("SELECT COUNT(*) FROM tb_perguntas_quiz"));
  }

  @Test
  void deveBloquearPerguntaComDuasAlternativasCorretas()
      throws Exception {

    var curso = criarCurso();

    var corpo = quiz(
        curso.get("id").asInt(),
        curso.get("aulas").get(0).get("id").asInt());

    corpo.put(
        "perguntas",
        List.of(
            dados(
                "enunciado", "Qual alternativa está correta?",
                "alternativas",
                List.of(
                    dados("texto", "Primeira", "correta", true),
                    dados("texto", "Segunda", "correta", true)))));

    chamar(
        HttpMethod.POST,
        "/api/admin/v1/quizzes",
        corpo).andExpect(status().isConflict());

    assertEquals(
        0,
        contar("SELECT COUNT(*) FROM tb_quizzes"));
  }

  @Test
  void deveBloquearQuizComPontosZero()
      throws Exception {

    var curso = criarCurso();

    var corpo = quiz(
        curso.get("id").asInt(),
        curso.get("aulas").get(0).get("id").asInt());

    corpo.put("pontos", 0);

    chamar(
        HttpMethod.POST,
        "/api/admin/v1/quizzes",
        corpo).andExpect(status().isBadRequest());

    assertEquals(
        0,
        contar("SELECT COUNT(*) FROM tb_quizzes"));
  }

  @Test
  void deveBloquearNotaMinimaForaDoIntervalo()
      throws Exception {

    var curso = criarCurso();

    var corpo = quiz(
        curso.get("id").asInt(),
        curso.get("aulas").get(0).get("id").asInt());

    for (int nota : List.of(-1, 101)) {
      corpo.put("notaMinima", nota);

      chamar(
          HttpMethod.POST,
          "/api/admin/v1/quizzes",
          corpo).andExpect(status().isBadRequest());
    }
  }

  @Test
  void deveBloquearAulaDeOutroCursoNaEdicao()
      throws Exception {

    var primeiro = criarCurso();
    var segundo = criarCurso();

    int cursoId = primeiro.get("id").asInt();
    int aulaAlheia = segundo.get("aulas")
        .get(0).get("id").asInt();

    var corpo = curso();

    corpo.put("titulo", "Alteração indevida");
    corpo.put(
        "aulas",
        List.of(
            dados(
                "id", aulaAlheia,
                "titulo", "Aula alheia",
                "conteudo", "Conteúdo indevido")));

    chamar(
        HttpMethod.PATCH,
        "/api/admin/v1/cursos/" + cursoId,
        corpo).andExpect(status().isConflict());

    assertEquals(
        "Curso Integração",
        jdbc.queryForObject("""
            SELECT titulo_curso
            FROM tb_cursos
            WHERE id_curso = ?
            """,
            String.class,
            cursoId));

    assertEquals(
        2,
        contar("""
            SELECT COUNT(*)
            FROM tb_aulas
            WHERE curso_id = ?
            """,
            cursoId));
  }

  @Test
  void deveBloquearRemocaoDeAulaComQuiz()
      throws Exception {

    var curso = criarCurso();

    criarQuizDoCurso(curso);

    var corpo = curso();
    corpo.put("aulas", List.of());

    int cursoId = curso.get("id").asInt();

    chamar(
        HttpMethod.PATCH,
        "/api/admin/v1/cursos/" + cursoId,
        corpo).andExpect(status().isConflict());

    assertEquals(
        2,
        contar("""
            SELECT COUNT(*)
            FROM tb_aulas
            WHERE curso_id = ?
            """,
            cursoId));

    assertEquals(
        1,
        contar("SELECT COUNT(*) FROM tb_quizzes"));
  }

  @Test
  void deveBloquearRemocaoDeTorreComHistorico()
      throws Exception {

    var condominio = criar(
        "/api/admin/v1/condominios",
        condominio());

    var curso = criarCurso();
    var quiz = criarQuizDoCurso(curso);

    int condominioNovoId = condominio.get("id").asInt();
    int torreId = condominio.get("torres")
        .get(0).get("id").asInt();

    jdbc.update("""
        INSERT INTO tb_tentativas_quiz (
            usuario_id,
            quiz_id,
            condominio_id,
            torre_id
        )
        VALUES (?, ?, ?, ?)
        """,
        comumId,
        quiz.get("id").asInt(),
        condominioNovoId,
        torreId);

    var corpo = condominio();
    corpo.put("torres", List.of());

    chamar(
        HttpMethod.PATCH,
        "/api/admin/v1/condominios/" + condominioNovoId,
        corpo).andExpect(status().isConflict());

    assertEquals(
        2,
        contar("""
            SELECT COUNT(*)
            FROM tb_torres
            WHERE condominio_id = ?
            """,
            condominioNovoId));
  }

  @Test
  void deveBloquearNomesDeTorresRepetidos()
      throws Exception {

    var corpo = condominio();

    corpo.put(
        "torres",
        List.of(
            dados("id", null, "nome", "Torre A"),
            dados("id", null, "nome", "torre a")));

    long antes = contar(
        "SELECT COUNT(*) FROM tb_condominios");

    chamar(
        HttpMethod.POST,
        "/api/admin/v1/condominios",
        corpo).andExpect(status().isConflict());

    assertEquals(
        antes,
        contar("SELECT COUNT(*) FROM tb_condominios"));
  }

  @Test
  void deveBloquearTipoDeCondominioInexistente()
      throws Exception {

    var corpo = condominio();

    corpo.put("tipoId", Integer.MAX_VALUE);

    chamar(
        HttpMethod.POST,
        "/api/admin/v1/condominios",
        corpo).andExpect(status().isConflict());
  }

  @Test
  void editarEnderecoDoCondominioNaoDeveAlterarUsuarioQueCompartilhaEndereco()
      throws Exception {

    var criado = criar(
        "/api/admin/v1/condominios",
        condominio());

    int id = criado.get("id").asInt();

    Integer enderecoAnterior = jdbc.queryForObject("""
        SELECT endereco_id
        FROM tb_condominios
        WHERE id_condominio = ?
        """,
        Integer.class,
        id);

    jdbc.update("""
        UPDATE tb_usuarios
        SET endereco_id = ?
        WHERE id_usuario = ?
        """,
        enderecoAnterior,
        comumId);

    var novoEndereco = endereco();
    novoEndereco.put("logradouro", "Rua Nova");

    var editar = condominio();

    editar.put("endereco", novoEndereco);
    editar.put(
        "torres",
        List.of(
            dados(
                "id",
                criado.get("torres").get(0).get("id").asInt(),
                "nome",
                "A"),
            dados(
                "id",
                criado.get("torres").get(1).get("id").asInt(),
                "nome",
                "B")));

    chamar(
        HttpMethod.PATCH,
        "/api/admin/v1/condominios/" + id,
        editar)
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.endereco.logradouro").value("Rua Nova"));

    assertEquals(
        "Rua dos Testes",
        jdbc.queryForObject("""
            SELECT e.logradouro
            FROM tb_usuarios u
            JOIN tb_enderecos e
                ON e.id_endereco = u.endereco_id
            WHERE u.id_usuario = ?
            """,
            String.class,
            comumId));

    assertEquals(
        enderecoAnterior,
        jdbc.queryForObject("""
            SELECT endereco_id
            FROM tb_usuarios
            WHERE id_usuario = ?
            """,
            Integer.class,
            comumId));
  }

  @Test
  void editarEnderecoDoUsuarioNaoDeveAlterarOutroUsuario()
      throws Exception {

    Integer enderecoAdmin = jdbc.queryForObject("""
        SELECT endereco_id
        FROM tb_usuarios
        WHERE id_usuario = ?
        """,
        Integer.class,
        adminId);

    jdbc.update("""
        UPDATE tb_usuarios
        SET endereco_id = ?
        WHERE id_usuario = ?
        """,
        enderecoAdmin,
        comumId);

    var novoEndereco = endereco();
    novoEndereco.put("logradouro", "Rua Exclusiva");

    chamar(
        HttpMethod.PATCH,
        "/api/admin/v1/usuarios/" + comumId,
        dados(
            "nome", "Usuário atualizado",
            "email", "comum@teste.com",
            "dataNascimento", null,
            "avatar", null,
            "endereco", novoEndereco))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.endereco.logradouro")
                .value("Rua Exclusiva"));

    assertEquals(
        "Rua dos Testes",
        jdbc.queryForObject("""
            SELECT e.logradouro
            FROM tb_usuarios u
            JOIN tb_enderecos e
                ON e.id_endereco = u.endereco_id
            WHERE u.id_usuario = ?
            """,
            String.class,
            adminId));
  }

  @Test
  void deveUsarIdDoSindicoEnaoIdDoUsuario()
      throws Exception {

    var corpoUsuario = usuario();
    corpoUsuario.put("tipoUsuarioId", tipoSindico);

    var usuario = criar(
        "/api/admin/v1/usuarios",
        corpoUsuario);

    int usuarioId = usuario.get("id").asInt();

    Integer sindicoId = jdbc.queryForObject("""
        SELECT id_sindico
        FROM tb_sindicos
        WHERE usuario_id = ?
        """,
        Integer.class,
        usuarioId);

    var catalogos = json(
        chamar(
            HttpMethod.GET,
            "/api/admin/v1/catalogos",
            null).andExpect(status().isOk()));

    assertEquals(
        sindicoId.intValue(),
        catalogos.get("sindicos")
            .get(0).get("id").asInt());

    assertEquals(
        usuarioId,
        catalogos.get("sindicos")
            .get(0).get("usuarioId").asInt());

    var corpo = condominio();
    corpo.put("sindicoId", sindicoId);

    var condominio = criar(
        "/api/admin/v1/condominios",
        corpo);

    assertEquals(
        sindicoId.intValue(),
        condominio.get("sindicoId").asInt());
  }

  @Test
  void deveBloquearPontoDeOutraCooperativa()
      throws Exception {

    var primeira = criar(
        "/api/admin/v1/cooperativas",
        cooperativa());

    int pontoAlheio = primeira.get("pontos")
        .get(0).get("id").asInt();

    var segundoCorpo = cooperativa();

    segundoCorpo.put("nome", "Segunda Cooperativa");
    segundoCorpo.put("cnpj", "12345678000195");
    segundoCorpo.put("email", "segunda@teste.com");

    var segunda = criar(
        "/api/admin/v1/cooperativas",
        segundoCorpo);

    int segundaId = segunda.get("id").asInt();

    segundoCorpo.put("nome", "Alteração indevida");

    segundoCorpo.put(
        "pontos",
        List.of(
            dados(
                "id", pontoAlheio,
                "nome", "Ponto alheio",
                "abertura", "08:00:00",
                "fechamento", "17:00:00",
                "ativo", true,
                "endereco", endereco(),
                "categoriaIds", List.of(categoria1))));

    chamar(
        HttpMethod.PATCH,
        "/api/admin/v1/cooperativas/" + segundaId,
        segundoCorpo).andExpect(status().isConflict());

    assertEquals(
        "Segunda Cooperativa",
        jdbc.queryForObject("""
            SELECT nome_cooperativa
            FROM tb_cooperativas
            WHERE id_cooperativa = ?
            """,
            String.class,
            segundaId));

    assertEquals(
        1,
        contar("""
            SELECT COUNT(*)
            FROM tb_pontos_coletas
            WHERE cooperativa_id = ?
            """,
            segundaId));
  }

  @Test
  void deveBloquearCategoriasRepetidas()
      throws Exception {

    var corpo = cooperativa();

    corpo.put(
        "categoriaIds",
        List.of(categoria1, categoria1));

    long antes = contar(
        "SELECT COUNT(*) FROM tb_cooperativas");

    chamar(
        HttpMethod.POST,
        "/api/admin/v1/cooperativas",
        corpo).andExpect(status().isConflict());

    assertEquals(
        antes,
        contar("SELECT COUNT(*) FROM tb_cooperativas"));
  }

  @Test
  void usuarioNaoPodePertencerADuasCooperativas()
      throws Exception {

    int usuarioId = inserirUsuario(
        "Responsável",
        "responsavel@teste.com",
        tipoCooperativa);

    var primeira = cooperativa();
    primeira.put("usuarioId", usuarioId);

    criar(
        "/api/admin/v1/cooperativas",
        primeira);

    var segunda = cooperativa();

    segunda.put("usuarioId", usuarioId);
    segunda.put("cnpj", "12345678000195");
    segunda.put("email", "outra@teste.com");

    chamar(
        HttpMethod.POST,
        "/api/admin/v1/cooperativas",
        segunda).andExpect(status().isConflict());

    assertEquals(
        1,
        contar("""
            SELECT COUNT(*)
            FROM tb_cooperativas
            WHERE usuario_id = ?
            """,
            usuarioId));
  }

  @Test
  void deveBloquearPeriodoInvertidoDaColeta()
      throws Exception {

    var corpo = coleta();

    corpo.put("dataInicio", dataVisita());
    corpo.put(
        "dataFim",
        dataVisita().minusDays(1));

    chamar(
        HttpMethod.POST,
        "/api/admin/v1/coletas",
        corpo).andExpect(status().isConflict());

    assertEquals(
        0,
        contar("SELECT COUNT(*) FROM tb_agendamentos_coletas"));
  }

  @Test
  void deveBloquearRecorrenciaSemDias()
      throws Exception {

    var corpo = coleta();

    corpo.put("recorrente", true);
    corpo.put("diasSemanaIds", List.of());

    chamar(
        HttpMethod.POST,
        "/api/admin/v1/coletas",
        corpo).andExpect(status().isConflict());
  }

  @Test
  void deveBloquearDiasRepetidosNaRecorrencia()
      throws Exception {

    var corpo = coleta();

    corpo.put("recorrente", true);
    corpo.put(
        "diasSemanaIds",
        List.of(diasIds[1], diasIds[1]));

    chamar(
        HttpMethod.POST,
        "/api/admin/v1/coletas",
        corpo).andExpect(status().isConflict());

    assertEquals(
        0,
        contar("""
            SELECT COUNT(*)
            FROM tb_rel_recorrencias_agendamentos
            """));
  }

  @Test
  void deveBloquearDiaInexistenteNaRecorrencia()
      throws Exception {

    var corpo = coleta();

    corpo.put("recorrente", true);
    corpo.put(
        "diasSemanaIds",
        List.of(Integer.MAX_VALUE));

    chamar(
        HttpMethod.POST,
        "/api/admin/v1/coletas",
        corpo).andExpect(status().isConflict());

    assertEquals(
        0,
        contar("SELECT COUNT(*) FROM tb_agendamentos_coletas"));
  }

  @Test
  void deveBloquearColetaComCooperativaInexistente()
      throws Exception {

    var corpo = coleta();

    corpo.put(
        "cooperativaId",
        Integer.MAX_VALUE);

    chamar(
        HttpMethod.POST,
        "/api/admin/v1/coletas",
        corpo).andExpect(status().isNotFound());
  }

  @Test
  void deveBloquearColetaComCondominioInativo()
      throws Exception {

    jdbc.update("""
        UPDATE tb_condominios
        SET ativo = FALSE
        WHERE id_condominio = ?
        """,
        condominioId);

    chamar(
        HttpMethod.POST,
        "/api/admin/v1/coletas",
        coleta()).andExpect(status().isConflict());
  }

  @Test
  void deveBloquearResultadoDeVisitaFutura()
      throws Exception {

    var coleta = criar(
        "/api/admin/v1/coletas",
        coleta());

    chamar(
        HttpMethod.POST,
        caminhoVisitas(coleta),
        dados(
            "data", dataVisita().plusDays(3),
            "realizada", true,
            "confirmada", false,
            "observacao", null))
        .andExpect(status().isConflict());

    assertEquals(
        0,
        contar("SELECT COUNT(*) FROM tb_visitas_coletas"));
  }

  @Test
  void deveBloquearVisitaForaDoPeriodo()
      throws Exception {

    var coleta = criar(
        "/api/admin/v1/coletas",
        coleta());

    chamar(
        HttpMethod.POST,
        caminhoVisitas(coleta),
        dados(
            "data", dataVisita().minusDays(10),
            "realizada", true,
            "confirmada", false,
            "observacao", null))
        .andExpect(status().isConflict());
  }

  @Test
  void deveBloquearVisitaEmDiaDiferenteDaRecorrencia()
      throws Exception {

    var corpo = coleta();

    corpo.put("recorrente", true);
    corpo.put(
        "diasSemanaIds",
        List.of(
            idDia(
                dataVisita().atZoneSameInstant(ZONA)
                    .getDayOfWeek())));

    var coleta = criar(
        "/api/admin/v1/coletas",
        corpo);

    chamar(
        HttpMethod.POST,
        caminhoVisitas(coleta),
        dados(
            "data", dataVisita().minusDays(1),
            "realizada", true,
            "confirmada", false,
            "observacao", null))
        .andExpect(status().isConflict());
  }

  @Test
  void deveBloquearConfirmacaoDeVisitaNaoRealizada()
      throws Exception {

    var coleta = criar(
        "/api/admin/v1/coletas",
        coleta());

    chamar(
        HttpMethod.POST,
        caminhoVisitas(coleta),
        dados(
            "data", dataVisita(),
            "realizada", false,
            "confirmada", true,
            "observacao", "Não ocorreu"))
        .andExpect(status().isConflict());
  }

  @Test
  void devePermitirAlterarSomenteStatusAposVisita()
      throws Exception {

    var corpo = coleta();

    var coleta = criar(
        "/api/admin/v1/coletas",
        corpo);

    chamar(
        HttpMethod.POST,
        caminhoVisitas(coleta),
        dados(
            "data", dataVisita(),
            "realizada", true,
            "confirmada", false,
            "observacao", null))
        .andExpect(status().isOk());

    corpo.put("statusId", statusRealizado);

    chamar(
        HttpMethod.PATCH,
        "/api/admin/v1/coletas/"
            + coleta.get("id").asInt(),
        corpo)
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.statusId").value(statusRealizado))
        .andExpect(
            jsonPath("$.visitas.length()").value(1));
  }

  @Test
  void deveBloquearCancelamentoDeColetaConcluida()
      throws Exception {

    var corpo = coleta();
    corpo.put("statusId", statusRealizado);

    var coleta = criar(
        "/api/admin/v1/coletas",
        corpo);

    chamar(
        HttpMethod.POST,
        "/api/admin/v1/coletas/"
            + coleta.get("id").asInt()
            + "/cancelamento",
        null).andExpect(status().isConflict());

    assertEquals(
        statusRealizado,
        jdbc.queryForObject("""
            SELECT status_agendamento_id
            FROM tb_agendamentos_coletas
            WHERE id_agendamento_coleta = ?
            """,
            Integer.class,
            coleta.get("id").asInt()));
  }

  @Test
  void cancelarNovamenteNaoDeveDuplicarAgendamento()
      throws Exception {

    var coleta = criar(
        "/api/admin/v1/coletas",
        coleta());

    String caminho = "/api/admin/v1/coletas/"
        + coleta.get("id").asInt()
        + "/cancelamento";

    for (int tentativa = 0; tentativa < 2; tentativa++) {
      chamar(
          HttpMethod.POST,
          caminho,
          null)
          .andExpect(status().isOk())
          .andExpect(
              jsonPath("$.statusId").value(statusCancelado));
    }

    assertEquals(
        1,
        contar("SELECT COUNT(*) FROM tb_agendamentos_coletas"));
  }

  @Test
  void deveContarCadastroNoMesCorretoDoDashboard()
      throws Exception {

    var hoje = java.time.LocalDate.now(ZONA);

    var inicioHoje = hoje
        .atStartOfDay(ZONA)
        .toOffsetDateTime();

    YearMonth mesAntigo = YearMonth.from(hoje)
        .minusMonths(3);

    var dataAntiga = mesAntigo.atDay(10)
        .atTime(12, 0)
        .atZone(ZONA)
        .toOffsetDateTime();

    assertEquals(
        1,
        jdbc.update("""
            UPDATE tb_usuarios
            SET registro_em = ?
            WHERE id_usuario = ?
            """,
            inicioHoje,
            adminId),
        "O administrador deve existir na preparação");

    assertEquals(
        1,
        jdbc.update("""
            UPDATE tb_usuarios
            SET registro_em = ?
            WHERE id_usuario = ?
            """,
            dataAntiga,
            comumId),
        "O usuário comum deve existir na preparação");

    var dashboard = json(
        chamar(
            HttpMethod.GET,
            "/api/admin/v1/dashboard?meses=6",
            null).andExpect(status().isOk()));

    String resposta = dashboard.toPrettyString();

    assertEquals(
        1L,
        dashboard.get("usuariosCadastradosHoje").asLong(),
        "Contagem de cadastros de hoje incorreta:\n" + resposta);

    JsonNode mesEncontrado = null;

    for (var mes : dashboard.get("evolucao")) {
      if (mesAntigo.toString().equals(
          mes.get("mes").asText())) {
        mesEncontrado = mes;
        break;
      }
    }

    assertNotNull(
        mesEncontrado,
        "Mês esperado não encontrado:\n" + resposta);

    assertEquals(
        1L,
        mesEncontrado.get("usuariosNovos").asLong(),
        "Contagem do mês antigo incorreta:\n" + resposta);
  }

  private JsonNode criarQuizDoCurso(
      JsonNode curso) throws Exception {
    return criar(
        "/api/admin/v1/quizzes",
        quiz(
            curso.get("id").asInt(),
            curso.get("aulas")
                .get(0).get("id").asInt()));
  }

  private String caminhoVisitas(JsonNode coleta) {
    return "/api/admin/v1/coletas/"
        + coleta.get("id").asInt()
        + "/visitas";
  }
}