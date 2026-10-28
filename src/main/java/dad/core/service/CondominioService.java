package dad.core.service;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dad.core.domain.Condominio;
import dad.core.domain.Pagina;
import dad.core.exception.RecursoNaoEncontradoException;
import dad.core.exception.RegraNegocioException;
import dad.core.gateway.CondominioGateway;

@Service
@Transactional
public class CondominioService {

  private final CondominioGateway gateway;

  public CondominioService(
      CondominioGateway gateway) {
    this.gateway = gateway;
  }

  @Transactional(readOnly = true)
  public Pagina<Condominio> listar(
      String busca,
      Boolean ativo,
      int pagina,
      int tamanho) {
    if (pagina < 0 || tamanho < 1 || tamanho > 100) {
      throw new RegraNegocioException(
          "Página deve ser maior ou igual a zero "
              + "e tamanho deve estar entre 1 e 100");
    }

    return gateway.listar(
        busca,
        ativo,
        pagina,
        tamanho);
  }

  @Transactional(readOnly = true)
  public Condominio buscar(Integer id) {
    return gateway.buscarPorId(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException(
            "Condomínio não encontrado"));
  }

  public Condominio criar(Condominio condominio) {
    validar(condominio);

    return gateway.criar(condominio);
  }

  public Condominio atualizar(Condominio condominio) {
    validar(condominio);

    return gateway.atualizar(condominio);
  }

  public Condominio alterarStatus(
      Integer id,
      boolean ativo) {
    return gateway.alterarStatus(id, ativo);
  }

  private void validar(Condominio condominio) {
    if (condominio.nome() == null
        || condominio.nome().isBlank()) {
      throw new RegraNegocioException(
          "Informe o nome do condomínio");
    }

    if (condominio.tipoId() == null) {
      throw new RegraNegocioException(
          "Informe o tipo de condomínio");
    }

    if (condominio.torres() == null) {
      throw new RegraNegocioException(
          "Informe a lista de torres");
    }

    Set<String> nomes = new HashSet<>();

    for (var torre : condominio.torres()) {
      if (torre == null
          || torre.nome() == null
          || torre.nome().isBlank()) {
        throw new RegraNegocioException(
            "Informe o nome de cada torre");
      }

      String nome = torre.nome()
          .trim()
          .toUpperCase(Locale.ROOT);

      if (!nomes.add(nome)) {
        throw new RegraNegocioException(
            "Não informe torres com nomes repetidos");
      }
    }
  }
}