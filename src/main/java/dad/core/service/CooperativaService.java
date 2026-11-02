package dad.core.service;

import dad.core.domain.Cooperativa;
import dad.core.domain.Pagina;
import dad.core.exception.RecursoNaoEncontradoException;
import dad.core.gateway.CooperativaGateway;
import dad.core.regra.RegrasCooperativa;
import dad.core.regra.RegrasPaginacao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CooperativaService {

  private final CooperativaGateway gateway;

  public CooperativaService(CooperativaGateway gateway) {
    this.gateway = gateway;
  }

  @Transactional(readOnly = true)
  public Pagina<Cooperativa> listar(String busca, int pagina, int tamanho) {
    RegrasPaginacao.validar(pagina, tamanho);

    return gateway.listar(busca, pagina, tamanho);
  }

  @Transactional(readOnly = true)
  public Cooperativa buscar(Integer id) {
    return gateway
        .buscarPorId(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException("Cooperativa não encontrada"));
  }

  public Cooperativa criar(Cooperativa cooperativa) {
    RegrasCooperativa.validar(cooperativa);

    return gateway.criar(cooperativa);
  }

  public Cooperativa atualizar(Cooperativa cooperativa) {
    RegrasCooperativa.validar(cooperativa);

    return gateway.atualizar(cooperativa);
  }
}
