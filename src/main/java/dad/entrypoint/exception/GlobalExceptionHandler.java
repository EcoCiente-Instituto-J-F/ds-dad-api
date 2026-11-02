package dad.entrypoint.exception;

import dad.core.exception.RecursoNaoEncontradoException;
import dad.core.exception.RegraNegocioException;
import dad.entrypoint.dto.response.ErroResponse;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(dad.core.exception.CamposInvalidosException.class)
  public ResponseEntity<ErroResponse> campos(dad.core.exception.CamposInvalidosException erro) {
    return resposta(HttpStatus.BAD_REQUEST, erro.getMessage(), erro.getCampos());
  }

  @ExceptionHandler({
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
    org.springframework.web.bind.MissingServletRequestParameterException.class,
    org.springframework.http.converter.HttpMessageNotReadableException.class
  })
  public ResponseEntity<ErroResponse> entrada(Exception erro) {
    return resposta(HttpStatus.BAD_REQUEST, "Confira os parâmetros e o JSON informados.", Map.of());
  }

  @ExceptionHandler(RecursoNaoEncontradoException.class)
  public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException exception) {
    return resposta(HttpStatus.NOT_FOUND, exception.getMessage(), Map.of());
  }

  @ExceptionHandler(RegraNegocioException.class)
  public ResponseEntity<ErroResponse> regraNegocio(RegraNegocioException exception) {
    return resposta(HttpStatus.CONFLICT, exception.getMessage(), Map.of());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException exception) {
    Map<String, String> campos = new LinkedHashMap<>();

    for (var erro : exception.getBindingResult().getFieldErrors()) {

      campos.putIfAbsent(
          erro.getField(),
          erro.getDefaultMessage() == null ? "Valor inválido" : erro.getDefaultMessage());
    }

    var nomes = new LinkedHashMap<String, String>();
    campos.forEach(
        (campo, mensagem) -> nomes.put(campo.replaceAll("\\[(\\d+)\\]", ".$1"), mensagem));
    boolean importacao =
        exception
            .getParameter()
            .getContainingClass()
            .equals(dad.entrypoint.controller.ImportacaoUsuariosController.class);
    return resposta(
        HttpStatus.BAD_REQUEST,
        importacao ? "Corrija os dados da importação." : "Confira os campos informados",
        nomes);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErroResponse> integridade(DataIntegrityViolationException exception) {
    return resposta(
        HttpStatus.CONFLICT,
        "A operação viola uma restrição do banco. "
            + "Confira duplicidades e registros vinculados.",
        Map.of());
  }

  private ResponseEntity<ErroResponse> resposta(
      HttpStatus status, String mensagem, Map<String, String> campos) {
    var body = new ErroResponse(Instant.now(), status.value(), mensagem, campos);

    return ResponseEntity.status(status).body(body);
  }
}
