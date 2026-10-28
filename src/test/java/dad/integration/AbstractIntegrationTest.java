package dad.integration;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import org.testcontainers.containers.PostgreSQLContainer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

  protected static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");

  protected static final String JWT_SECRET = "chave-de-teste-ecociente-dad-"
      + "0123456789012345678901234567890123456789";

  protected static final String ADMIN_EMAIL = "admin@teste.com";

  protected static final String SENHA = "Teste@123";

  private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("ecociente_test")
      .withUsername("teste")
      .withPassword("teste");

 
  static {
    POSTGRES.start();

    try {
      String schema = new ClassPathResource(
          "sql/ecociente-schema.sql").getContentAsString(StandardCharsets.UTF_8);

      try (
          var connection = DriverManager.getConnection(
              POSTGRES.getJdbcUrl(),
              POSTGRES.getUsername(),
              POSTGRES.getPassword());
          var statement = connection.createStatement()) {
   
        statement.execute(schema);
      }
    } catch (Exception exception) {
      POSTGRES.stop();

      throw new IllegalStateException(
          "Não foi possível preparar o schema dos testes",
          exception);
    }
  }

  @DynamicPropertySource
  static void configurar(DynamicPropertyRegistry registry) {
    registry.add(
        "spring.datasource.url",
        POSTGRES::getJdbcUrl);

    registry.add(
        "spring.datasource.username",
        POSTGRES::getUsername);

    registry.add(
        "spring.datasource.password",
        POSTGRES::getPassword);

    registry.add(
        "spring.jpa.hibernate.ddl-auto",
        () -> "validate");

    registry.add(
        "spring.sql.init.mode",
        () -> "never");

    registry.add(
        "app.security.jwt.secret",
        () -> JWT_SECRET);

    registry.add(
        "app.cors.origens",
        () -> "http://localhost:5173");
  }

  @Autowired
  protected MockMvc mvc;

  @Autowired
  protected ObjectMapper objectMapper;

  @Autowired
  protected JdbcTemplate jdbc;

  protected Integer tipoAdmin;
  protected Integer tipoComum;
  protected Integer tipoMorador;
  protected Integer tipoSindico;
  protected Integer tipoCooperativa;
  protected Integer tipoCondominio;

  protected Integer categoria1;
  protected Integer categoria2;

  protected Integer statusAgendado;
  protected Integer statusCancelado;
  protected Integer statusRealizado;

  protected Integer adminId;
  protected Integer comumId;
  protected Integer condominioId;
  protected Integer cooperativaId;

  protected final int[] diasIds = new int[7];

  protected String tokenAdmin;

  @BeforeEach
  void prepararBanco() {
    jdbc.execute("""
        TRUNCATE TABLE
            tb_enderecos,
            tb_lkp_tipos_usuarios,
            tb_lkp_tipos_condominios,
            tb_lkp_status_agendamentos,
            tb_lkp_dias_semanas,
            tb_lkp_categorias_residuos,
            tb_lkp_niveis_confianca,
            tb_cursos
        RESTART IDENTITY CASCADE
        """);

    tipoAdmin = inserirTipo("Administrador");
    tipoComum = inserirTipo("Comum");
    tipoMorador = inserirTipo("Morador");
    tipoSindico = inserirTipo("Síndico");
    tipoCooperativa = inserirTipo("Cooperativa");

    tipoCondominio = id("""
        INSERT INTO tb_lkp_tipos_condominios (
            nome_tipo
        )
        VALUES ('Residencial')
        RETURNING id_tipo_condominio
        """);

    categoria1 = inserirCategoria("Papel", 10);
    categoria2 = inserirCategoria("Plástico", 15);

    statusAgendado = inserirStatus("Agendado");
    inserirStatus("Confirmado");
    statusCancelado = inserirStatus("Cancelado");
    statusRealizado = inserirStatus("Realizado");
    inserirStatus("Recusado");

    jdbc.update("""
        INSERT INTO tb_lkp_niveis_confianca (
            nome_nivel,
            peso_voto
        )
        VALUES ('morador_comum', 1)
        """);


    String[] nomes = {
        "Quarta-feira",
        "Domingo",
        "Sexta-feira",
        "Segunda-feira",
        "Sábado",
        "Terça-feira",
        "Quinta-feira"
    };

    int[] numeros = { 3, 0, 5, 1, 6, 2, 4 };

    for (int indice = 0; indice < nomes.length; indice++) {
      diasIds[numeros[indice]] = id("""
          INSERT INTO tb_lkp_dias_semanas (
              nome_dia
          )
          VALUES (?)
          RETURNING id_dia_semana
          """,
          nomes[indice]);
    }

    adminId = inserirUsuario(
        "Administrador",
        ADMIN_EMAIL,
        tipoAdmin);

    comumId = inserirUsuario(
        "Usuário comum",
        "comum@teste.com",
        tipoComum);

    condominioId = id("""
        INSERT INTO tb_condominios (
            nome_condominio,
            cnpj,
            codigo_acesso,
            tipo_condominio_id,
            endereco_id
        )
        VALUES (?, ?, ?, ?, ?)
        RETURNING id_condominio
        """,
        "Condomínio Base",
        "11444777000161",
        "BASE-01",
        tipoCondominio,
        inserirEndereco());

    cooperativaId = id("""
        INSERT INTO tb_cooperativas (
            nome_cooperativa,
            cnpj_cooperativa,
            email_cooperativa,
            endereco_id
        )
        VALUES (?, ?, ?, ?)
        RETURNING id_cooperativa
        """,
        "Cooperativa Base",
        "11444777000161",
        "base@cooperativa.com",
        inserirEndereco());

    tokenAdmin = token(
        adminId,
        ADMIN_EMAIL,
        "ADMIN");
  }

  protected Integer inserirTipo(String nome) {
    return id("""
        INSERT INTO tb_lkp_tipos_usuarios (
            nome_tipo
        )
        VALUES (?)
        RETURNING id_tipo_usuario
        """,
        nome);
  }

  protected Integer inserirCategoria(
      String nome,
      int pontos) {
    return id("""
        INSERT INTO tb_lkp_categorias_residuos (
            nome_categoria,
            pontos_base
        )
        VALUES (?, ?)
        RETURNING id_categoria
        """,
        nome,
        pontos);
  }

  protected Integer inserirStatus(String nome) {
    return id("""
        INSERT INTO tb_lkp_status_agendamentos (
            nome_status
        )
        VALUES (?)
        RETURNING id_status
        """,
        nome);
  }

  protected Integer inserirEndereco() {
    return id("""
        INSERT INTO tb_enderecos (
            cep,
            estado,
            cidade,
            logradouro,
            numero
        )
        VALUES (
            '01001000',
            'SP',
            'São Paulo',
            'Rua dos Testes',
            '10'
        )
        RETURNING id_endereco
        """);
  }

  protected Integer inserirUsuario(
      String nome,
      String email,
      Integer tipoId) {
    String hash = new BCryptPasswordEncoder(4)
        .encode(SENHA);

    return id("""
        INSERT INTO tb_usuarios (
            nome_usuario,
            email_usuario,
            senha_hash,
            tipo_usuario_id,
            endereco_id
        )
        VALUES (?, ?, ?, ?, ?)
        RETURNING id_usuario
        """,
        nome,
        email,
        hash,
        tipoId,
        inserirEndereco());
  }

  protected Integer id(
      String sql,
      Object... parametros) {
    return jdbc.queryForObject(
        sql,
        Integer.class,
        parametros);
  }

  protected long contar(
      String sql,
      Object... parametros) {
    return jdbc.queryForObject(
        sql,
        Long.class,
        parametros);
  }

  protected String token(
      Integer usuarioId,
      String email,
      String perfil) {
    return token(
        usuarioId,
        email,
        perfil,
        Instant.now().plusSeconds(3600));
  }

  protected String token(
      Integer usuarioId,
      String email,
      String perfil,
      Instant expiracao) {
    return Jwts.builder()
        .subject(email)
        .claim("usuarioId", usuarioId)
        .claim("perfil", perfil)
        .issuedAt(
            Date.from(
                Instant.now().minusSeconds(120)))
        .expiration(Date.from(expiracao))
        .signWith(
            Keys.hmacShaKeyFor(
                JWT_SECRET.getBytes(
                    StandardCharsets.UTF_8)))
        .compact();
  }

  protected ResultActions chamar(
      HttpMethod metodo,
      String caminho,
      Object corpo) throws Exception {
    return chamar(
        metodo,
        caminho,
        corpo,
        tokenAdmin);
  }

  protected ResultActions chamar(
      HttpMethod metodo,
      String caminho,
      Object corpo,
      String token) throws Exception {
    var builder = request(
        metodo,
        URI.create(caminho)).accept(MediaType.APPLICATION_JSON);

    if (token != null) {
      builder.header(
          "Authorization",
          "Bearer " + token);
    }

    if (corpo != null) {
      builder.contentType(
          MediaType.APPLICATION_JSON);

      builder.content(
          objectMapper.writeValueAsBytes(corpo));
    }

    return mvc.perform(builder);
  }

  protected JsonNode json(
      ResultActions resultado) throws Exception {
    return objectMapper.readTree(
        resultado.andReturn()
            .getResponse()
            .getContentAsByteArray());
  }

  protected JsonNode criar(
      String caminho,
      Object corpo) throws Exception {
    return json(
        chamar(
            HttpMethod.POST,
            caminho,
            corpo).andExpect(status().isCreated()));
  }

  protected Map<String, Object> dados(
      Object... valores) {
    Map<String, Object> resultado = new LinkedHashMap<>();

    for (int indice = 0; indice < valores.length; indice += 2) {
      resultado.put(
          (String) valores[indice],
          valores[indice + 1]);
    }

    return resultado;
  }

  protected Map<String, Object> endereco() {
    return dados(
        "cep", "01001000",
        "uf", "SP",
        "cidade", "São Paulo",
        "logradouro", "Rua dos Testes",
        "numero", "10",
        "complemento", null);
  }

  protected Map<String, Object> usuario() {
    return dados(
        "nome", "Pessoa Teste",
        "email", "pessoa@teste.com",
        "senha", SENHA,
        "tipoUsuarioId", tipoComum,
        "dataNascimento", "2000-01-10",
        "cpf", "52998224725",
        "avatar", null,
        "endereco", endereco(),
        "condominioId", null);
  }

  protected Map<String, Object> curso() {
    return dados(
        "titulo", "Curso Integração",
        "descricao", "Conteúdo educativo para testes",
        "ativo", true,
        "aulas", List.of(
            dados(
                "id", null,
                "titulo", "Primeira aula",
                "conteudo", "Conteúdo da primeira aula"),
            dados(
                "id", null,
                "titulo", "Segunda aula",
                "conteudo", "Conteúdo da segunda aula")));
  }

  protected JsonNode criarCurso() throws Exception {
    return criar(
        "/api/admin/v1/cursos",
        curso());
  }

  protected Map<String, Object> quiz(
      int cursoId,
      int aulaId) {
    return dados(
        "cursoId", cursoId,
        "aulaId", aulaId,
        "titulo", "Quiz Integração",
        "notaMinima", 70,
        "pontos", 20,
        "ativo", true,
        "perguntas", List.of(
            dados(
                "enunciado",
                "Qual material pode ser reciclado?",
                "alternativas",
                List.of(
                    dados(
                        "texto", "Papel",
                        "correta", true),
                    dados(
                        "texto", "Material contaminado",
                        "correta", false)))));
  }

  protected Map<String, Object> condominio() {
    return dados(
        "nome", "Edifício Novo",
        "cnpj", "11222333000181",
        "codigoAcesso", "NOVO-01",
        "ativo", true,
        "tipoId", tipoCondominio,
        "sindicoId", null,
        "endereco", endereco(),
        "torres", List.of(
            dados("id", null, "nome", "A"),
            dados("id", null, "nome", "B")));
  }

  protected Map<String, Object> cooperativa() {
    return dados(
        "nome", "Cooperativa Nova",
        "cnpj", "11222333000181",
        "email", "nova@cooperativa.com",
        "telefone", "11999999999",
        "usuarioId", null,
        "endereco", endereco(),
        "categoriaIds",
        List.of(categoria1, categoria2),
        "pontos",
        List.of(
            dados(
                "id", null,
                "nome", "Ponto Central",
                "abertura", "08:00:00",
                "fechamento", "17:00:00",
                "ativo", true,
                "endereco", endereco(),
                "categoriaIds", List.of(categoria1))));
  }

  protected OffsetDateTime dataVisita() {
    return ZonedDateTime.now(ZONA)
        .minusDays(1)
        .withHour(12)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)
        .toOffsetDateTime();
  }

  protected Map<String, Object> coleta() {
    var agora = ZonedDateTime.now(ZONA);

    return dados(
        "condominioId", condominioId,
        "cooperativaId", cooperativaId,
        "statusId", statusAgendado,
        "dataInicio",
        agora.minusDays(3)
            .toLocalDate()
            .atStartOfDay(ZONA)
            .toOffsetDateTime(),
        "dataFim",
        agora.minusDays(1)
            .toLocalDate()
            .atTime(23, 59, 59)
            .atZone(ZONA)
            .toOffsetDateTime(),
        "recorrente", false,
        "diasSemanaIds", List.of());
  }

  protected int idDia(DayOfWeek dia) {
    return diasIds[dia.getValue() % 7];
  }
}