package dad.core.service;

import dad.core.domain.Curso;
import dad.core.domain.Pagina;
import dad.core.exception.RecursoNaoEncontradoException;
import dad.core.gateway.CursoGateway;
import dad.core.regra.RegrasCurso;
import dad.core.regra.RegrasPaginacao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CursoService {

  private final CursoGateway gateway;

  public CursoService(CursoGateway gateway) {
    this.gateway = gateway;
  }

  @Transactional(readOnly = true)
  public Pagina<Curso> listar(String busca, int pagina, int tamanho) {
    validarPaginacao(pagina, tamanho);

    return gateway.listar(busca, pagina, tamanho);
  }

  @Transactional(readOnly = true)
  public Curso buscar(Integer id) {
    return gateway
        .buscarPorId(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException("Curso não encontrado"));
  }

  public Curso criar(Curso curso) {
    RegrasCurso.validar(curso);

    return gateway.criar(curso);
  }

  public Curso atualizar(Curso curso) {
    RegrasCurso.validar(curso);

    return gateway.atualizar(curso);
  }

  private void validarPaginacao(int pagina, int tamanho) {
    RegrasPaginacao.validar(pagina, tamanho);
  }
}
