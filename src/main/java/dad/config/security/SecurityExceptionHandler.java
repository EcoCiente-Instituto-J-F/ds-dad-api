package dad.config.security;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import dad.entrypoint.dto.response.ErroResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SecurityExceptionHandler
    implements AuthenticationEntryPoint, AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  public SecurityExceptionHandler(
      ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException exception) throws IOException {
    naoAutenticado(response);
  }

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException exception) throws IOException {
    escrever(
        response,
        HttpServletResponse.SC_FORBIDDEN,
        "Acesso restrito ao administrador");
  }

  public void naoAutenticado(
      HttpServletResponse response) throws IOException {
    response.setHeader(
        "WWW-Authenticate",
        "Bearer");

    escrever(
        response,
        HttpServletResponse.SC_UNAUTHORIZED,
        "Token ausente, inválido ou expirado");
  }

  public void indisponivel(
      HttpServletResponse response) throws IOException {
    escrever(
        response,
        HttpServletResponse.SC_SERVICE_UNAVAILABLE,
        "Não foi possível verificar o usuário");
  }

  private void escrever(
      HttpServletResponse response,
      int status,
      String mensagem) throws IOException {
    response.setStatus(status);
    response.setContentType(
        MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    objectMapper.writeValue(
        response.getOutputStream(),
        new ErroResponse(
            Instant.now(),
            status,
            mensagem,
            Map.of()));
  }
}