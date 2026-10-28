package dad.config.security;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

  private final JwtParser parser;

  public JwtService(
      @Value("${app.security.jwt.secret}") String secret) {
    this.parser = Jwts.parser()
        .verifyWith(
            Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)))
        .build();
  }

  public Claims validar(String token) {
    Claims claims = parser
        .parseSignedClaims(token)
        .getPayload();

    if (claims.getExpiration() == null
        || !claims.getExpiration().after(new Date())) {
      throw new JwtException(
          "Token sem expiração válida");
    }

    if (claims.getSubject() == null
        || claims.getSubject().isBlank()) {
      throw new JwtException(
          "Token sem identificação de e-mail");
    }

    extrairUsuarioId(claims);

    return claims;
  }

  public Integer extrairUsuarioId(Claims claims) {
    Object valor = claims.get("usuarioId");

    if (!(valor instanceof Number numero)) {
      throw new JwtException(
          "Token sem identificação de usuário");
    }

    try {
      int id = new BigDecimal(
          numero.toString()).intValueExact();

      if (id <= 0) {
        throw new JwtException(
            "Identificação de usuário inválida");
      }

      return id;
    } catch (ArithmeticException | NumberFormatException ex) {
      throw new JwtException(
          "Identificação de usuário inválida",
          ex);
    }
  }
}