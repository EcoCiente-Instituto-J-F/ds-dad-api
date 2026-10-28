package dad.integration;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ApiAdministrativaIntegrationTest
    extends AbstractIntegrationTest {

  @Test
  void deveExigirToken() throws Exception {
    chamar(
        HttpMethod.GET,
        "/api/admin/v1/dashboard",
        null,
        null).andExpect(status().isUnauthorized());
  }

  @Test
  void deveRecusarTokenInvalido() throws Exception {
    chamar(
        HttpMethod.GET,
        "/api/admin/v1/dashboard",
        null,
        "token-invalido").andExpect(status().isUnauthorized());
  }

  @Test
  void deveRecusarTokenExpirado() throws Exception {
    String expirado = token(
        adminId,
        ADMIN_EMAIL,
        "ADMIN",
        Instant.now().minusSeconds(60));

    chamar(
        HttpMethod.GET,
        "/api/admin/v1/dashboard",
        null,
        expirado).andExpect(status().isUnauthorized());
  }

  @Test
  void deveRecusarUsuarioComum() throws Exception {
    String token = token(
        comumId,
        "comum@teste.com",
        "USUARIO_COMUM");

    chamar(
        HttpMethod.GET,
        "/api/admin/v1/dashboard",
        null,
        token).andExpect(status().isForbidden());
  }

  @Test
  void perfilAdminNoTokenNaoDeveSubstituirTipoDoBanco()
      throws Exception {

    String token = token(
        comumId,
        "comum@teste.com",
        "ADMIN");

    chamar(
        HttpMethod.GET,
        "/api/admin/v1/dashboard",
        null,
        token).andExpect(status().isForbidden());
  }

  @Test
  void administradorPrecisaDePerfilAdminNoToken()
      throws Exception {

    String token = token(
        adminId,
        ADMIN_EMAIL,
        "USUARIO_COMUM");

    chamar(
        HttpMethod.GET,
        "/api/admin/v1/dashboard",
        null,
        token).andExpect(status().isForbidden());
  }

  @Test
  void deveRecusarAdministradorDesativado()
      throws Exception {

    jdbc.update("""
        UPDATE tb_usuarios
        SET ativo = FALSE
        WHERE id_usuario = ?
        """,
        adminId);

    chamar(
        HttpMethod.GET,
        "/api/admin/v1/dashboard",
        null).andExpect(status().isUnauthorized());
  }

  @Test
  void deveConsultarPerfilAtualDoBanco()
      throws Exception {

    jdbc.update("""
        UPDATE tb_usuarios
        SET tipo_usuario_id = ?
        WHERE id_usuario = ?
        """,
        tipoComum,
        adminId);

    chamar(
        HttpMethod.GET,
        "/api/admin/v1/dashboard",
        null).andExpect(status().isForbidden());
  }

  @Test
  void deveRecusarTokenComEmailDiferente()
      throws Exception {

    String token = token(
        adminId,
        "outro@teste.com",
        "ADMIN");

    chamar(
        HttpMethod.GET,
        "/api/admin/v1/dashboard",
        null,
        token).andExpect(status().isUnauthorized());
  }

  @Test
  void devePermitirPreflightDoFrontend()
      throws Exception {

    mvc.perform(
        options("/api/admin/v1/usuarios")
            .header(
                "Origin",
                "http://localhost:5173")
            .header(
                "Access-Control-Request-Method",
                "GET")
            .header(
                "Access-Control-Request-Headers",
                "Authorization"))
        .andExpect(status().isOk())
        .andExpect(header().string(
            "Access-Control-Allow-Origin",
            "http://localhost:5173"));
  }

  @Test
  void deveCadastrarConsultarEditarEDesativarUsuario()
      throws Exception {

    var criado = criar(
        "/api/admin/v1/usuarios",
        usuario());

    int usuarioId = criado.get("id").asInt();

    assertEquals(
        "Pessoa Teste",
        criado.get("nome").asText());

    assertEquals(
        "***.***.***-25",
        criado.get("cpfMascarado").asText());

    assertFalse(criado.has("senha"));
    assertFalse(criado.has("senhaHash"));
    assertFalse(criado.has("cpf"));

    String hash = jdbc.queryForObject("""
        SELECT senha_hash
        FROM tb_usuarios
        WHERE id_usuario = ?
        """,
        String.class,
        usuarioId);

    assertNotEquals(SENHA, hash);
    assertTrue(
        new BCryptPasswordEncoder()
            .matches(SENHA, hash));

    chamar(
        HttpMethod.GET,
        "/api/admin/v1/usuarios/" + usuarioId,
        null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(usuarioId));

    chamar(
        HttpMethod.GET,
        "/api/admin/v1/usuarios?busca=Pessoa"
            + "&pagina=0&tamanho=1",
        null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalItens").value(1))
        .andExpect(jsonPath("$.itens[0].id").value(usuarioId));

    var editar = dados(
        "nome", "Pessoa Atualizada",
        "email", "atualizada@teste.com",
        "dataNascimento", "2000-01-10",
        "avatar", null,
        "endereco", endereco());

    chamar(
        HttpMethod.PATCH,
        "/api/admin/v1/usuarios/" + usuarioId,
        editar)
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.nome").value("Pessoa Atualizada"));

    assertEquals(
        "52998224725",
        jdbc.queryForObject("""
            SELECT cpf
            FROM tb_usuarios
            WHERE id_usuario = ?
            """,
            String.class,
            usuarioId));

    chamar(
        HttpMethod.PATCH,
        "/api/admin/v1/usuarios/"
            + usuarioId + "/status",
        dados("ativo", false))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ativo").value(false));

    chamar(
        HttpMethod.GET,
        "/api/admin/v1/usuarios?ativo=false",
        null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalItens").value(1));
  }

  @Test
  void deveBloquearEmailDuplicadoSemGravarUsuario()
      throws Exception {

    var corpo = usuario();

    corpo.put("email", ADMIN_EMAIL);

    long antes = contar(
        "SELECT COUNT(*) FROM tb_usuarios");

    chamar(
        HttpMethod.POST,
        "/api/admin/v1/usuarios",
        corpo).andExpect(status().isConflict());

    assertEquals(
        antes,
        contar("SELECT COUNT(*) FROM tb_usuarios"));
  }

  @Test
  void deveBloquearCpfDuplicado()
      throws Exception {

    criar(
        "/api/admin/v1/usuarios",
        usuario());

    var outro = usuario();
    outro.put("email", "outra@teste.com");

    chamar(
        HttpMethod.POST,
        "/api/admin/v1/usuarios",
        outro).andExpect(status().isConflict());
  }

  @Test
  void deveCadastrarMoradorComVinculoPendente()
      throws Exception {

    var corpo = usuario();

    corpo.put("tipoUsuarioId", tipoMorador);
    corpo.put("condominioId", condominioId);

    var criado = criar(
        "/api/admin/v1/usuarios",
        corpo);

    int usuarioId = criado.get("id").asInt();

    assertEquals(
        condominioId.intValue(),
        criado.get("condominioId").asInt());

    assertEquals(
        1,
        contar("""
            SELECT COUNT(*)
            FROM tb_moradores
            WHERE usuario_id = ?
              AND condominio_id = ?
            """,
            usuarioId,
            condominioId));

    assertEquals(
        1,
        contar("""
            SELECT COUNT(*)
            FROM tb_rel_usuarios_condominios
            WHERE usuario_id = ?
              AND aprovado = FALSE
              AND trust_score = 0
            """,
            usuarioId));
  }

  @Test
  void deveBloquearMoradorSemCondominio()
      throws Exception {

    var corpo = usuario();

    corpo.put("tipoUsuarioId", tipoMorador);

    long antes = contar(
        "SELECT COUNT(*) FROM tb_usuarios");

    chamar(
        HttpMethod.POST,
        "/api/admin/v1/usuarios",
        corpo).andExpect(status().isConflict());

    assertEquals(
        antes,
        contar("SELECT COUNT(*) FROM tb_usuarios"));
  }

  @Test
  void deveCadastrarExtensaoDoSindico()
      throws Exception {

    var corpo = usuario();
    corpo.put("tipoUsuarioId", tipoSindico);

    var criado = criar(
        "/api/admin/v1/usuarios",
        corpo);

    assertEquals(
        1,
        contar("""
            SELECT COUNT(*)
            FROM tb_sindicos
            WHERE usuario_id = ?
            """,
            criado.get("id").asInt()));
  }

  @Test
  void deveValidarCamposDoCadastro()
      throws Exception {

    var corpo = usuario();

    corpo.put("nome", "");
    corpo.put("senha", "123");

    chamar(
        HttpMethod.POST,
        "/api/admin/v1/usuarios",
        corpo)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.campos.nome").exists())
        .andExpect(jsonPath("$.campos.senha").exists());
  }

  @Test
  void deveCadastrarConsultarEReordenarCurso()
      throws Exception {

    var criado = criarCurso();

    int cursoId = criado.get("id").asInt();
    int primeiraId = criado.get("aulas")
        .get(0).get("id").asInt();
    int segundaId = criado.get("aulas")
        .get(1).get("id").asInt();

    chamar(
        HttpMethod.GET,
        "/api/admin/v1/cursos/" + cursoId,
        null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.aulas.length()").value(2));

    chamar(
        HttpMethod.GET,
        "/api/admin/v1/cursos?busca=Curso"
            + "&pagina=0&tamanho=1",
        null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalItens").value(1));

    var editar = curso();

    editar.put(
        "aulas",
        List.of(
            dados(
                "id", segundaId,
                "titulo", "Segunda aula",
                "conteudo", "Conteúdo da segunda aula"),
            dados(
                "id", primeiraId,
                "titulo", "Primeira aula",
                "conteudo", "Conteúdo da primeira aula")));

    var atualizado = json(
        chamar(
            HttpMethod.PATCH,
            "/api/admin/v1/cursos/" + cursoId,
            editar).andExpect(status().isOk()));

    assertEquals(
        segundaId,
        atualizado.get("aulas")
            .get(0).get("id").asInt());

    assertEquals(
        1,
        atualizado.get("aulas")
            .get(0).get("ordem").asInt());

    assertEquals(
        primeiraId,
        atualizado.get("aulas")
            .get(1).get("id").asInt());
  }

  @Test
  void devePreservarAulaComProgressoERollbackDoCurso()
      throws Exception {

    var criado = criarCurso();

    int cursoId = criado.get("id").asInt();
    int aulaId = criado.get("aulas")
        .get(0).get("id").asInt();

    jdbc.update("""
        INSERT INTO tb_rel_usuarios_cursos (
            usuario_id,
            aula_id
        )
        VALUES (?, ?)
        """,
        comumId,
        aulaId);

    var editar = curso();

    editar.put("titulo", "Título que não deve persistir");
    editar.put("aulas", List.of());

    chamar(
        HttpMethod.PATCH,
        "/api/admin/v1/cursos/" + cursoId,
        editar).andExpect(status().isConflict());

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
  void deveCadastrarConsultarEditarEExcluirQuiz()
      throws Exception {

    var curso = criarCurso();

    int cursoId = curso.get("id").asInt();
    int aulaId = curso.get("aulas")
        .get(0).get("id").asInt();

    var corpo = quiz(cursoId, aulaId);

    var criado = criar(
        "/api/admin/v1/quizzes",
        corpo);

    int quizId = criado.get("id").asInt();

    chamar(
        HttpMethod.GET,
        "/api/admin/v1/quizzes/" + quizId,
        null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.perguntas.length()").value(1));

    chamar(
        HttpMethod.GET,
        "/api/admin/v1/quizzes?busca=Quiz",
        null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalItens").value(1));

    corpo.put("titulo", "Quiz Atualizado");
    corpo.put("pontos", 30);

    chamar(
        HttpMethod.PUT,
        "/api/admin/v1/quizzes/" + quizId,
        corpo)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pontos").value(30));

    assertEquals(
        1,
        contar("""
            SELECT COUNT(*)
            FROM tb_perguntas_quiz
            WHERE quiz_id = ?
            """,
            quizId));

    chamar(
        HttpMethod.DELETE,
        "/api/admin/v1/quizzes/" + quizId,
        null).andExpect(status().isNoContent());

    chamar(
        HttpMethod.GET,
        "/api/admin/v1/quizzes/" + quizId,
        null).andExpect(status().isNotFound());

    assertEquals(
        0,
        contar("SELECT COUNT(*) FROM tb_alternativas_quiz"));
  }

  @Test
  void deveBloquearSegundoQuizNaMesmaAula()
      throws Exception {

    var curso = criarCurso();

    var corpo = quiz(
        curso.get("id").asInt(),
        curso.get("aulas").get(0).get("id").asInt());

    criar("/api/admin/v1/quizzes", corpo);

    chamar(
        HttpMethod.POST,
        "/api/admin/v1/quizzes",
        corpo).andExpect(status().isConflict());
  }

  @Test
  void deveExigirUmaAlternativaCorreta()
      throws Exception {

    var curso = criarCurso();

    var corpo = quiz(
        curso.get("id").asInt(),
        curso.get("aulas").get(0).get("id").asInt());

    corpo.put(
        "perguntas",
        List.of(
            dados(
                "enunciado", "Pergunta sem resposta correta",
                "alternativas",
                List.of(
                    dados("texto", "A", "correta", false),
                    dados("texto", "B", "correta", false)))));

    chamar(
        HttpMethod.POST,
        "/api/admin/v1/quizzes",
        corpo).andExpect(status().isConflict());

    assertEquals(
        0,
        contar("SELECT COUNT(*) FROM tb_quizzes"));
  }

  @Test
  void devePreservarEstruturaDoQuizComTentativa()
      throws Exception {

    var curso = criarCurso();

    var corpo = quiz(
        curso.get("id").asInt(),
        curso.get("aulas").get(0).get("id").asInt());

    var criado = criar(
        "/api/admin/v1/quizzes",
        corpo);

    int quizId = criado.get("id").asInt();
    int perguntaId = criado.get("perguntas")
        .get(0).get("id").asInt();

    jdbc.update("""
        INSERT INTO tb_tentativas_quiz (
            usuario_id,
            quiz_id
        )
        VALUES (?, ?)
        """,
        comumId,
        quizId);

    corpo.put("titulo", "Título permitido");

    var atualizado = json(
        chamar(
            HttpMethod.PUT,
            "/api/admin/v1/quizzes/" + quizId,
            corpo).andExpect(status().isOk()));

    assertEquals(
        perguntaId,
        atualizado.get("perguntas")
            .get(0).get("id").asInt());

    corpo.put("pontos", 99);

    chamar(
        HttpMethod.PUT,
        "/api/admin/v1/quizzes/" + quizId,
        corpo).andExpect(status().isConflict());

    chamar(
        HttpMethod.DELETE,
        "/api/admin/v1/quizzes/" + quizId,
        null).andExpect(status().isConflict());

    assertEquals(
        20,
        jdbc.queryForObject("""
            SELECT pontos_recompensa
            FROM tb_quizzes
            WHERE id_quiz = ?
            """,
            Integer.class,
            quizId).intValue());
  }

  @Test
  void deveCadastrarConsultarEditarEDesativarCondominio()
      throws Exception {

    var criado = criar(
        "/api/admin/v1/condominios",
        condominio());

    int id = criado.get("id").asInt();
    int torreA = criado.get("torres")
        .get(0).get("id").asInt();
    int torreB = criado.get("torres")
        .get(1).get("id").asInt();

    chamar(
        HttpMethod.GET,
        "/api/admin/v1/condominios/" + id,
        null).andExpect(status().isOk());

    chamar(
        HttpMethod.GET,
        "/api/admin/v1/condominios?busca=Novo",
        null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalItens").value(1));

    var editar = condominio();

    editar.put(
        "torres",
        List.of(
            dados("id", torreA, "nome", "B"),
            dados("id", torreB, "nome", "A")));

    var atualizado = json(
        chamar(
            HttpMethod.PATCH,
            "/api/admin/v1/condominios/" + id,
            editar).andExpect(status().isOk()));

    assertEquals(
        torreB,
        atualizado.get("torres")
            .get(0).get("id").asInt());

    chamar(
        HttpMethod.PATCH,
        "/api/admin/v1/condominios/"
            + id + "/status",
        dados("ativo", false))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ativo").value(false));
  }

  @Test
  void deveBloquearTorreDeOutroCondominio()
      throws Exception {

    var primeiro = criar(
        "/api/admin/v1/condominios",
        condominio());

    int torreId = primeiro.get("torres")
        .get(0).get("id").asInt();

    var segundoCorpo = condominio();

    segundoCorpo.put("cnpj", "12345678000195");
    segundoCorpo.put("codigoAcesso", "NOVO-02");

    var segundo = criar(
        "/api/admin/v1/condominios",
        segundoCorpo);

    segundoCorpo.put(
        "torres",
        List.of(
            dados(
                "id", torreId,
                "nome", "Torre de outro condomínio")));

    chamar(
        HttpMethod.PATCH,
        "/api/admin/v1/condominios/"
            + segundo.get("id").asInt(),
        segundoCorpo).andExpect(status().isConflict());
  }

  @Test
  void deveCadastrarConsultarEditarCooperativaEPontos()
      throws Exception {

    var criado = criar(
        "/api/admin/v1/cooperativas",
        cooperativa());

    int id = criado.get("id").asInt();
    int pontoId = criado.get("pontos")
        .get(0).get("id").asInt();

    chamar(
        HttpMethod.GET,
        "/api/admin/v1/cooperativas/" + id,
        null).andExpect(status().isOk());

    chamar(
        HttpMethod.GET,
        "/api/admin/v1/cooperativas?busca=Nova",
        null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalItens").value(1));

    var editar = cooperativa();

    editar.put(
        "categoriaIds",
        List.of(categoria2));

    editar.put(
        "pontos",
        List.of(
            dados(
                "id", pontoId,
                "nome", "Ponto Atualizado",
                "abertura", "09:00:00",
                "fechamento", "18:00:00",
                "ativo", false,
                "endereco", endereco(),
                "categoriaIds", List.of(categoria2))));

    chamar(
        HttpMethod.PATCH,
        "/api/admin/v1/cooperativas/" + id,
        editar)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pontos[0].ativo").value(false));

    assertEquals(
        1,
        contar("""
            SELECT COUNT(*)
            FROM tb_rel_cooperativas_categorias_materiais
            WHERE cooperativa_id = ?
              AND categoria_residuo_id = ?
            """,
            id,
            categoria2));

    assertEquals(
        1,
        contar("""
            SELECT COUNT(*)
            FROM tb_rel_pontos_coletas_categorias
            WHERE ponto_coleta_id = ?
              AND categoria_residuo_id = ?
            """,
            pontoId,
            categoria2));

    editar.put("pontos", List.of());

    chamar(
        HttpMethod.PATCH,
        "/api/admin/v1/cooperativas/" + id,
        editar)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pontos.length()").value(0));

    assertEquals(
        0,
        contar("""
            SELECT COUNT(*)
            FROM tb_rel_pontos_coletas_categorias
            WHERE ponto_coleta_id = ?
            """,
            pontoId));
  }

  @Test
  void deveBloquearCategoriaInexistenteSemGravarCooperativa()
      throws Exception {

    var corpo = cooperativa();

    corpo.put(
        "categoriaIds",
        List.of(Integer.MAX_VALUE));

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
  void deveBloquearVinculoDeCooperativaComUsuarioComum()
      throws Exception {

    var corpo = cooperativa();
    corpo.put("usuarioId", comumId);

    chamar(
        HttpMethod.POST,
        "/api/admin/v1/cooperativas",
        corpo).andExpect(status().isConflict());
  }

  @Test
  void deveCriarConsultarEditarRegistrarVisitaECancelarColeta()
      throws Exception {

    var corpo = coleta();

    var criado = criar(
        "/api/admin/v1/coletas",
        corpo);

    int id = criado.get("id").asInt();

    chamar(
        HttpMethod.GET,
        "/api/admin/v1/coletas/" + id,
        null).andExpect(status().isOk());

    chamar(
        HttpMethod.GET,
        "/api/admin/v1/coletas?busca=Cooperativa"
            + "&statusId=" + statusAgendado,
        null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalItens").value(1));

    corpo.put("dataFim", dataVisita().withHour(23));

    chamar(
        HttpMethod.PATCH,
        "/api/admin/v1/coletas/" + id,
        corpo).andExpect(status().isOk());

    chamar(
        HttpMethod.POST,
        "/api/admin/v1/coletas/" + id + "/visitas",
        dados(
            "data", dataVisita(),
            "realizada", true,
            "confirmada", true,
            "observacao", "Coleta realizada"))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.visitas[0].realizada").value(true))
        .andExpect(
            jsonPath("$.visitas[0].confirmadoEm").isNotEmpty());

    chamar(
        HttpMethod.POST,
        "/api/admin/v1/coletas/"
            + id + "/cancelamento",
        null)
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.statusId").value(statusCancelado));

    chamar(
        HttpMethod.POST,
        "/api/admin/v1/coletas/" + id + "/visitas",
        dados(
            "data", dataVisita(),
            "realizada", true,
            "confirmada", false,
            "observacao", null))
        .andExpect(status().isConflict());
  }

  @Test
  void deveFiltrarAgendamentosPorPeriodo()
      throws Exception {

    criar(
        "/api/admin/v1/coletas",
        coleta());

    mvc.perform(
        get("/api/admin/v1/coletas")
            .header(
                "Authorization",
                "Bearer " + tokenAdmin)
            .param(
                "inicio",
                dataVisita().minusDays(5).toString())
            .param(
                "fim",
                dataVisita().plusDays(1).toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalItens").value(1));

    mvc.perform(
        get("/api/admin/v1/coletas")
            .header(
                "Authorization",
                "Bearer " + tokenAdmin)
            .param(
                "inicio",
                dataVisita().plusDays(10).toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalItens").value(0));
  }

  @Test
  void deveRespeitarRecorrenciaEBloquearVisitaDuplicada()
      throws Exception {

    var corpo = coleta();

    corpo.put("recorrente", true);
    corpo.put(
        "diasSemanaIds",
        List.of(
            idDia(
                dataVisita().atZoneSameInstant(ZONA)
                    .getDayOfWeek())));

    var criado = criar(
        "/api/admin/v1/coletas",
        corpo);

    int id = criado.get("id").asInt();

    var visita = dados(
        "data", dataVisita(),
        "realizada", true,
        "confirmada", false,
        "observacao", null);

    chamar(
        HttpMethod.POST,
        "/api/admin/v1/coletas/" + id + "/visitas",
        visita).andExpect(status().isOk());

    chamar(
        HttpMethod.POST,
        "/api/admin/v1/coletas/" + id + "/visitas",
        visita).andExpect(status().isConflict());

    corpo.put(
        "dataInicio",
        dataVisita().minusDays(10));

    chamar(
        HttpMethod.PATCH,
        "/api/admin/v1/coletas/" + id,
        corpo).andExpect(status().isConflict());

    assertEquals(
        1,
        contar("""
            SELECT COUNT(*)
            FROM tb_visitas_coletas
            WHERE agendamento_coleta_id = ?
            """,
            id));
  }

  @Test
  void deveRegistrarResultadoNaVisitaPreviamentePlanejada()
      throws Exception {

    var criado = criar(
        "/api/admin/v1/coletas",
        coleta());

    int coletaId = criado.get("id").asInt();

    int visitaId = id("""
        INSERT INTO tb_visitas_coletas (
            agendamento_coleta_id,
            data_visita
        )
        VALUES (?, ?)
        RETURNING id_visita_coleta
        """,
        coletaId,
        dataVisita());

    chamar(
        HttpMethod.POST,
        "/api/admin/v1/coletas/"
            + coletaId + "/visitas",
        dados(
            "data", dataVisita(),
            "realizada", false,
            "confirmada", false,
            "observacao", "Cooperativa não compareceu"))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.visitas[0].id").value(visitaId));

    assertEquals(
        1,
        contar("""
            SELECT COUNT(*)
            FROM tb_visitas_coletas
            WHERE agendamento_coleta_id = ?
            """,
            coletaId));
  }

  @Test
  void deveExigirObservacaoQuandoVisitaNaoForRealizada()
      throws Exception {

    var criado = criar(
        "/api/admin/v1/coletas",
        coleta());

    chamar(
        HttpMethod.POST,
        "/api/admin/v1/coletas/"
            + criado.get("id").asInt() + "/visitas",
        dados(
            "data", dataVisita(),
            "realizada", false,
            "confirmada", false,
            "observacao", null))
        .andExpect(status().isConflict());

    assertEquals(
        0,
        contar("SELECT COUNT(*) FROM tb_visitas_coletas"));
  }

  @Test
  void deveConsultarDashboardComDadosReais()
      throws Exception {

    criar(
        "/api/admin/v1/usuarios",
        usuario());

    var dashboard = json(
        chamar(
            HttpMethod.GET,
            "/api/admin/v1/dashboard?meses=6",
            null).andExpect(status().isOk()));

    assertEquals(
        contar("SELECT COUNT(*) FROM tb_usuarios"),
        dashboard.get("usuarios").asLong());

    assertEquals(
        contar("SELECT COUNT(*) FROM tb_cooperativas"),
        dashboard.get("cooperativas").asLong());

    assertEquals(
        6,
        dashboard.get("evolucao").size());

    assertEquals(
        0,
        dashboard.get("evolucao")
            .get(0).get("usuariosNovos").asLong());

    assertEquals(
        3,
        dashboard.get("usuariosCadastradosHoje").asLong());

    assertEquals(
        "Pessoa Teste",
        dashboard.get("usuariosRecentes")
            .get(0).get("nome").asText());
  }

  @Test
  void deveConsultarCatalogosComIdsReais()
      throws Exception {

    var catalogos = json(
        chamar(
            HttpMethod.GET,
            "/api/admin/v1/catalogos",
            null).andExpect(status().isOk()));

    assertEquals(
        5,
        catalogos.get("tiposUsuarios").size());

    assertEquals(
        2,
        catalogos.get("categoriasResiduos").size());

    assertEquals(
        7,
        catalogos.get("diasSemana").size());

    for (int numero = 0; numero < 7; numero++) {
      var dia = catalogos.get("diasSemana")
          .get(numero);

      assertEquals(
          numero,
          dia.get("numero").asInt());

      assertEquals(
          diasIds[numero],
          dia.get("id").asInt());
    }
  }

  @Test
  void deveRetornar404ParaRecursosInexistentes()
      throws Exception {

    for (String recurso : List.of(
        "usuarios",
        "cursos",
        "quizzes",
        "condominios",
        "cooperativas",
        "coletas")) {
      chamar(
          HttpMethod.GET,
          "/api/admin/v1/"
              + recurso + "/2147483647",
          null).andExpect(status().isNotFound());
    }
  }
}