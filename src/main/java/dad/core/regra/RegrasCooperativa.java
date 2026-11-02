package dad.core.regra;

import dad.core.domain.Cooperativa;
import dad.core.domain.PontoColeta;
import dad.core.exception.RegraNegocioException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class RegrasCooperativa {
  private RegrasCooperativa() {}

  public static void validar(Cooperativa cooperativa) {
    if (cooperativa.nome() == null || cooperativa.nome().isBlank()) {
      throw new RegraNegocioException("Informe o nome da cooperativa");
    }

    if (cooperativa.cnpj() == null || !cooperativa.cnpj().matches("[A-Z0-9]{12}[0-9]{2}")) {
      throw new RegraNegocioException("Informe o CNPJ com 14 posições");
    }

    validarCategorias(cooperativa.categoriaIds());

    if (cooperativa.pontos() == null) {
      throw new RegraNegocioException("Informe a lista de pontos");
    }

    for (var ponto : cooperativa.pontos()) {
      if (ponto == null || ponto.nome() == null || ponto.nome().isBlank()) {
        throw new RegraNegocioException("Informe o nome de cada ponto");
      }

      validarCategorias(ponto.categoriaIds());
    }
  }

  private static void validarCategorias(List<Integer> categorias) {
    if (categorias == null || categorias.stream().anyMatch(id -> id == null || id <= 0)) {
      throw new RegraNegocioException("Informe uma lista válida de categorias");
    }

    if (new HashSet<>(categorias).size() != categorias.size()) {
      throw new RegraNegocioException("Não informe categorias repetidas");
    }
  }

  public static void validarIdNovo(boolean possuiId) {
    if (possuiId)
      throw new RegraNegocioException("Pontos de uma nova cooperativa não devem possuir ID");
  }

  public static Set<Integer> validarIds(List<PontoColeta> itens, Set<Integer> existentes) {
    Set<Integer> recebidos = new HashSet<>();
    for (var item : itens) {
      if (item.id() == null) continue;
      if (!recebidos.add(item.id()))
        throw new RegraNegocioException("Ponto informado mais de uma vez");
      if (!existentes.contains(item.id()))
        throw new RegraNegocioException("Ponto não pertence à cooperativa");
    }
    return recebidos;
  }

  public static void validarUsuario(boolean valido) {
    if (!valido)
      throw new RegraNegocioException("O usuário deve estar ativo e possuir tipo Cooperativa");
  }

  public static void validarCnpj(boolean duplicado) {
    if (duplicado) throw new RegraNegocioException("CNPJ já cadastrado");
  }

  public static void validarVinculoUsuario(boolean duplicado) {
    if (duplicado) throw new RegraNegocioException("Usuário já vinculado a outra cooperativa");
  }

  public static void validarCategoria(Integer id, boolean existe) {
    if (!existe) throw new RegraNegocioException("Categoria de resíduo não encontrada: " + id);
  }
}
