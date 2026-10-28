package dad.config.security;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

  private static final String SECRET = "chave-de-testes-jwt-ecociente-"
      + "0123456789012345678901234567890123456789";

  private final JwtService service = new JwtService(SECRET);

  @Test
  void deveValidarTokenCompativelComAutenticacao() {
    String token = gerar(
        15,
        "admin@teste.com",
        Instant.now().plusSeconds(3600));

    var claims = service.validar(token);

    assertEquals(
        15,
        service.extrairUsuarioId(claims).intValue());

    assertEquals(
        "admin@teste.com",
        claims.getSubject());

    assertEquals(
        "ADMIN",
        claims.get("perfil", String.class));
  }

  @Test
  void deveRecusarTokenExpirado() {
    String token = gerar(
        15,
        "admin@teste.com",
        Instant.now().minusSeconds(60));

    assertThrows(
        JwtException.class,
        () -> service.validar(token));
  }

  @Test
  void deveRecusarTokenSemExpiracao() {
    String token = gerar(
        15,
        "admin@teste.com",
        null);

    assertThrows(
        JwtException.class,
        () -> service.validar(token));
  }

  @Test
  void deveRecusarTokenSemEmail() {
    String token = gerar(
        15,
        null,
        Instant.now().plusSeconds(3600));

    assertThrows(
        JwtException.class,
        () -> service.validar(token));
  }

  @Test
  void deveRecusarIdentificadorFracionario() {
    String token = gerar(
        new BigDecimal("15.5"),
        "admin@teste.com",
        Instant.now().plusSeconds(3600));

    assertThrows(
        JwtException.class,
        () -> service.validar(token));
  }

  @Test
  void deveRecusarIdentificadorAcimaDoLimiteInteger() {
    String token = gerar(
        2147483648L,
        "admin@teste.com",
        Instant.now().plusSeconds(3600));

    assertThrows(
        JwtException.class,
        () -> service.validar(token));
  }

  @Test
  void deveRecusarIdentificadorTextual() {
    String token = gerar(
        "15",
        "admin@teste.com",
        Instant.now().plusSeconds(3600));

    assertThrows(
        JwtException.class,
        () -> service.validar(token));
  }

  @Test
  void deveRecusarAssinaturaDeOutraChave() {
    String outraChave = "outra-chave-jwt-ecociente-"
        + "0123456789012345678901234567890123456789";

    String token = Jwts.builder()
        .subject("admin@teste.com")
        .claim("usuarioId", 15)
        .expiration(
            Date.from(
                Instant.now().plusSeconds(3600)))
        .signWith(
            Keys.hmacShaKeyFor(
                outraChave.getBytes(
                    StandardCharsets.UTF_8)))
        .compact();

    assertThrows(
        JwtException.class,
        () -> service.validar(token));
  }

  private String gerar(
      Object usuarioId,
      String email,
      Instant expiracao) {
    var builder = Jwts.builder()
        .claim("usuarioId", usuarioId)
        .claim("perfil", "ADMIN");

    if (email != null) {
      builder.subject(email);
    }

    if (expiracao != null) {
      builder.expiration(
          Date.from(expiracao));
    }

    return builder
        .signWith(
            Keys.hmacShaKeyFor(
                SECRET.getBytes(
                    StandardCharsets.UTF_8)))
        .compact();
  }
}