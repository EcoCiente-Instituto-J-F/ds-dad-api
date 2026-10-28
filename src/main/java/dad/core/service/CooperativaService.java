package dad.core.service;

import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dad.core.domain.Cooperativa;
import dad.core.domain.Pagina;
import dad.core.exception.RecursoNaoEncontradoException;
import dad.core.exception.RegraNegocioException;
import dad.core.gateway.CooperativaGateway;

@Service
@Transactional
public class CooperativaService {

  private final CooperativaGateway gateway;

  public CooperativaService(
      CooperativaGateway gateway) {
    this.gateway = gateway;
  }

  @Transactional(readOnly = true)
  public Pagina<Cooperativa> listar(
      String busca,
      int pagina,
      int tamanho) {
    if (pagina < 0 || tamanho < 1 || tamanho > 100) {
      throw new RegraNegocioException(
          "Página deve ser maior ou igual a zero "
              + "e tamanho deve estar entre 1 e 100");
    }

    return gateway.listar(busca, pagina, tamanho);
  }

  @Transactional(readOnly = true)
  public Cooperativa buscar(Integer id) {
    return gateway.buscarPorId(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException(
            "Cooperativa não encontrada"));
  }

  public Cooperativa criar(Cooperativa cooperativa) {
    validar(cooperativa);

    return gateway.criar(cooperativa);
  }

  public Cooperativa atualizar(
      Cooperativa cooperativa) {
    validar(cooperativa);

    return gateway.atualizar(cooperativa);
  }

  private void validar(Cooperativa cooperativa) {
    if (cooperativa.nome() == null
        || cooperativa.nome().isBlank()) {
      throw new RegraNegocioException(
          "Informe o nome da cooperativa");
    }

    if (cooperativa.cnpj() == null
        || !cooperativa.cnpj().matches(
            "[A-Z0-9]{12}[0-9]{2}")) {
      throw new RegraNegocioException(
          "Informe o CNPJ com 14 posições");
    }

    validarCategorias(cooperativa.categoriaIds());

    if (cooperativa.pontos() == null) {
      throw new RegraNegocioException(
          "Informe a lista de pontos");
    }

    for (var ponto : cooperativa.pontos()) {
      if (ponto == null
          || ponto.nome() == null
          || ponto.nome().isBlank()) {
        throw new RegraNegocioException(
            "Informe o nome de cada ponto");
      }

      validarCategorias(ponto.categoriaIds());
    }
  }

  private void validarCategorias(
      List<Integer> categorias) {
    if (categorias == null
        || categorias.stream().anyMatch(
            id -> id == null || id <= 0)) {
      throw new RegraNegocioException(
          "Informe uma lista válida de categorias");
    }

    if (new HashSet<>(categorias).size() != categorias.size()) {
      throw new RegraNegocioException(
          "Não informe categorias repetidas");
    }
  }
}