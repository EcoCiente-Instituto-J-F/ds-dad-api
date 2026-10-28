package dad.core.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dad.core.domain.Curso;
import dad.core.domain.Pagina;
import dad.core.exception.RecursoNaoEncontradoException;
import dad.core.exception.RegraNegocioException;
import dad.core.gateway.CursoGateway;

@Service
@Transactional
public class CursoService {

  private final CursoGateway gateway;

  public CursoService(CursoGateway gateway) {
    this.gateway = gateway;
  }

  @Transactional(readOnly = true)
  public Pagina<Curso> listar(
      String busca,
      int pagina,
      int tamanho) {
    validarPaginacao(pagina, tamanho);

    return gateway.listar(
        busca,
        pagina,
        tamanho);
  }

  @Transactional(readOnly = true)
  public Curso buscar(Integer id) {
    return gateway.buscarPorId(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException(
            "Curso não encontrado"));
  }

  public Curso criar(Curso curso) {
    validar(curso);

    return gateway.criar(curso);
  }

  public Curso atualizar(Curso curso) {
    validar(curso);

    return gateway.atualizar(curso);
  }

  private void validar(Curso curso) {
    if (curso.titulo() == null
        || curso.titulo().isBlank()) {
      throw new RegraNegocioException(
          "Informe o título do curso");
    }

    if (curso.descricao() == null
        || curso.descricao().isBlank()) {
      throw new RegraNegocioException(
          "Informe a descrição do curso");
    }

    if (curso.aulas() == null) {
      throw new RegraNegocioException(
          "Informe a lista de aulas");
    }

    for (var aula : curso.aulas()) {
      if (aula == null
          || aula.titulo() == null
          || aula.titulo().isBlank()
          || aula.conteudo() == null
          || aula.conteudo().isBlank()) {
        throw new RegraNegocioException(
            "Cada aula precisa de título e conteúdo");
      }
    }
  }

  private void validarPaginacao(
      int pagina,
      int tamanho) {
    if (pagina < 0 || tamanho < 1 || tamanho > 100) {
      throw new RegraNegocioException(
          "Página deve ser maior ou igual a zero "
              + "e tamanho deve estar entre 1 e 100");
    }
  }
}