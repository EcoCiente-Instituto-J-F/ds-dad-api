package dad.integration;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.http.HttpMethod;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class SegurancaComplementarIntegrationTest
    extends AbstractIntegrationTest {

  @ParameterizedTest(name = "{0} {1} deve exigir ADMIN")
  @CsvSource({
      "GET, /api/admin/v1/usuarios",
      "GET, /api/admin/v1/usuarios/1",
      "POST, /api/admin/v1/usuarios",
      "PATCH, /api/admin/v1/usuarios/1",
      "PATCH, /api/admin/v1/usuarios/1/status",

      "GET, /api/admin/v1/cursos",
      "GET, /api/admin/v1/cursos/1",
      "POST, /api/admin/v1/cursos",
      "PATCH, /api/admin/v1/cursos/1",

      "GET, /api/admin/v1/quizzes",
      "GET, /api/admin/v1/quizzes/1",
      "POST, /api/admin/v1/quizzes",
      "PUT, /api/admin/v1/quizzes/1",
      "DELETE, /api/admin/v1/quizzes/1",

      "GET, /api/admin/v1/condominios",
      "GET, /api/admin/v1/condominios/1",
      "POST, /api/admin/v1/condominios",
      "PATCH, /api/admin/v1/condominios/1",
      "PATCH, /api/admin/v1/condominios/1/status",

      "GET, /api/admin/v1/cooperativas",
      "GET, /api/admin/v1/cooperativas/1",
      "POST, /api/admin/v1/cooperativas",
      "PATCH, /api/admin/v1/cooperativas/1",

      "GET, /api/admin/v1/coletas",
      "GET, /api/admin/v1/coletas/1",
      "POST, /api/admin/v1/coletas",
      "PATCH, /api/admin/v1/coletas/1",
      "POST, /api/admin/v1/coletas/1/cancelamento",
      "POST, /api/admin/v1/coletas/1/visitas",

      "GET, /api/admin/v1/dashboard",
      "GET, /api/admin/v1/catalogos"
  })
  void usuarioComumNaoDeveAcessarRotasAdministrativas(
      String metodo,
      String caminho) throws Exception {

    String tokenComum = token(
        comumId,
        "comum@teste.com",
        "USUARIO_COMUM");

    chamar(
        HttpMethod.valueOf(metodo),
        caminho,
        null,
        tokenComum).andExpect(status().isForbidden());
  }

  @ParameterizedTest
  @ValueSource(longs = {
      0,
      -1,
      2147483648L
  })
  void deveRecusarIdentificadorDeUsuarioInvalido(
      long usuarioId) throws Exception {

    chamar(
        HttpMethod.GET,
        "/api/admin/v1/dashboard",
        null,
        tokenCustomizado(
            usuarioId,
            ADMIN_EMAIL,
            true))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void deveRecusarIdentificadorFracionario()
      throws Exception {

    chamar(
        HttpMethod.GET,
        "/api/admin/v1/dashboard",
        null,
        tokenCustomizado(
            new BigDecimal("1.5"),
            ADMIN_EMAIL,
            true))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void deveRecusarTokenSemUsuarioId()
      throws Exception {

    chamar(
        HttpMethod.GET,
        "/api/admin/v1/dashboard",
        null,
        tokenCustomizado(
            null,
            ADMIN_EMAIL,
            true))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void deveRecusarTokenSemExpiracao()
      throws Exception {

    chamar(
        HttpMethod.GET,
        "/api/admin/v1/dashboard",
        null,
        tokenCustomizado(
            adminId,
            ADMIN_EMAIL,
            false))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void deveRecusarTokenSemSubject()
      throws Exception {

    chamar(
        HttpMethod.GET,
        "/api/admin/v1/dashboard",
        null,
        tokenCustomizado(
            adminId,
            null,
            true))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void deveRecusarAssinaturaComOutraChave()
      throws Exception {

    String outraChave = "outra-chave-de-testes-ecociente-"
        + "0123456789012345678901234567890123456789";

    String token = Jwts.builder()
        .subject(ADMIN_EMAIL)
        .claim("usuarioId", adminId)
        .claim("perfil", "ADMIN")
        .expiration(
            Date.from(
                Instant.now().plusSeconds(3600)))
        .signWith(
            Keys.hmacShaKeyFor(
                outraChave.getBytes(
                    StandardCharsets.UTF_8)))
        .compact();

    chamar(
        HttpMethod.GET,
        "/api/admin/v1/dashboard",
        null,
        token).andExpect(status().isUnauthorized());
  }

  @Test
  void deveRecusarAuthorizationQueNaoSejaBearer()
      throws Exception {

    mvc.perform(
        get("/api/admin/v1/dashboard")
            .header(
                "Authorization",
                "Basic credenciais-invalidas"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void deveBloquearOrigemNaoPermitidaNoCors()
      throws Exception {

    mvc.perform(
        options("/api/admin/v1/usuarios")
            .header(
                "Origin",
                "https://origem-nao-autorizada.example")
            .header(
                "Access-Control-Request-Method",
                "GET"))
        .andExpect(status().isForbidden());
  }

  @Test
  void deveDisponibilizarDocumentacaoSemToken()
      throws Exception {

    mvc.perform(
        get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath(
                "$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
  }

  private String tokenCustomizado(
      Object usuarioId,
      String email,
      boolean incluirExpiracao) {
    var builder = Jwts.builder()
        .claim("perfil", "ADMIN")
        .issuedAt(
            Date.from(Instant.now()));

    if (usuarioId != null) {
      builder.claim("usuarioId", usuarioId);
    }

    if (email != null) {
      builder.subject(email);
    }

    if (incluirExpiracao) {
      builder.expiration(
          Date.from(
              Instant.now().plusSeconds(3600)));
    }

    return builder
        .signWith(
            Keys.hmacShaKeyFor(
                JWT_SECRET.getBytes(
                    StandardCharsets.UTF_8)))
        .compact();
  }
}