package dad.core.exception;

import java.util.Map;

public class CamposInvalidosException extends RuntimeException {
  private final Map<String, String> campos;

  public CamposInvalidosException(String mensagem, Map<String, String> campos) {
    super(mensagem);
    this.campos = Map.copyOf(campos);
  }

  public Map<String, String> getCampos() {
    return campos;
  }
}
