package dad.core.regra;

import dad.core.domain.Condominio;
import dad.core.domain.Torre;
import dad.core.exception.RegraNegocioException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class RegrasCondominio {
  private RegrasCondominio() {}

  public static void validar(Condominio condominio) {
    if (condominio.nome() == null || condominio.nome().isBlank()) {
      throw new RegraNegocioException("Informe o nome do condomínio");
    }

    if (condominio.tipoId() == null) {
      throw new RegraNegocioException("Informe o tipo de condomínio");
    }

    if (condominio.torres() == null) {
      throw new RegraNegocioException("Informe a lista de torres");
    }

    Set<String> nomes = new HashSet<>();

    for (var torre : condominio.torres()) {
      if (torre == null || torre.nome() == null || torre.nome().isBlank()) {
        throw new RegraNegocioException("Informe o nome de cada torre");
      }

      String nome = torre.nome().trim().toUpperCase(Locale.ROOT);

      if (!nomes.add(nome)) {
        throw new RegraNegocioException("Não informe torres com nomes repetidos");
      }
    }
  }

  public static void validarIdNovo(boolean possuiId) {
    if (possuiId)
      throw new RegraNegocioException("Torres de um novo condomínio não devem possuir ID");
  }

  public static Set<Integer> validarIds(List<Torre> itens, Set<Integer> existentes) {
    Set<Integer> recebidos = new HashSet<>();
    for (var item : itens) {
      if (item.id() == null) continue;
      if (!recebidos.add(item.id()))
        throw new RegraNegocioException("Torre informada mais de uma vez");
      if (!existentes.contains(item.id()))
        throw new RegraNegocioException("Torre não pertence ao condomínio");
    }
    return recebidos;
  }

  public static void validarTipo(boolean existe) {
    if (!existe) throw new RegraNegocioException("Tipo de condomínio não encontrado");
  }

  public static void validarSindico(boolean existe) {
    if (!existe) throw new RegraNegocioException("Síndico não encontrado ou usuário inativo");
  }

  public static void validarCnpj(boolean duplicado) {
    if (duplicado) throw new RegraNegocioException("CNPJ já cadastrado em outro condomínio");
  }

  public static void validarCodigo(boolean duplicado) {
    if (duplicado) throw new RegraNegocioException("Código de acesso já utilizado");
  }

  public static void validarRemocaoTorre(long referencias) {
    if (referencias > 0)
      throw new RegraNegocioException("Não é possível remover uma torre com histórico");
  }
}
