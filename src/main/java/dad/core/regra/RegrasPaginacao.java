package dad.core.regra;

import dad.core.exception.RegraNegocioException;

public final class RegrasPaginacao {
  private RegrasPaginacao() {}

  public static void validar(int pagina, int tamanho) {
    if (pagina < 0 || tamanho < 1 || tamanho > 100)
      throw new RegraNegocioException(
          "Página deve ser maior ou igual a zero e tamanho deve estar entre 1 e 100");
  }
}
