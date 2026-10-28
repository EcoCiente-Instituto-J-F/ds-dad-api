package dad.config.security;

import java.io.IOException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter
    extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UsuarioJwtService usuarioService;
  private final SecurityExceptionHandler exceptionHandler;

  public JwtAuthenticationFilter(
      JwtService jwtService,
      UsuarioJwtService usuarioService,
      SecurityExceptionHandler exceptionHandler) {
    this.jwtService = jwtService;
    this.usuarioService = usuarioService;
    this.exceptionHandler = exceptionHandler;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String authorization = request.getHeader(
        HttpHeaders.AUTHORIZATION);

    if (authorization == null) {
      filterChain.doFilter(request, response);
      return;
    }

    if (!authorization.regionMatches(
        true,
        0,
        "Bearer ",
        0,
        7)) {
      exceptionHandler.naoAutenticado(response);
      return;
    }

    try {
      String token = authorization
          .substring(7)
          .trim();

      var claims = jwtService.validar(token);

      var usuario = usuarioService.buscar(
          jwtService.extrairUsuarioId(claims),
          claims.getSubject());

      String perfil = claims.get(
          "perfil",
          String.class);

      boolean administrador = usuario.administrador()
          && "ADMIN".equals(perfil);

      List<SimpleGrantedAuthority> authorities = administrador
          ? List.of(
              new SimpleGrantedAuthority(
                  "ROLE_ADMIN"))
          : List.of();

      var authentication = new UsernamePasswordAuthenticationToken(
          usuario,
          null,
          authorities);

      authentication.setDetails(
          new WebAuthenticationDetailsSource()
              .buildDetails(request));

      var context = SecurityContextHolder.createEmptyContext();

      context.setAuthentication(authentication);

      SecurityContextHolder.setContext(context);

    } catch (
        JwtException
        | IllegalArgumentException
        | AuthenticationException exception) {
      SecurityContextHolder.clearContext();
      exceptionHandler.naoAutenticado(response);
      return;

    } catch (DataAccessException exception) {
      SecurityContextHolder.clearContext();
      exceptionHandler.indisponivel(response);
      return;
    }

    filterChain.doFilter(request, response);
  }
}