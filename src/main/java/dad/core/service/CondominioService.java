package dad.core.service;

import dad.core.domain.Condominio;
import dad.core.domain.Pagina;
import dad.core.exception.RecursoNaoEncontradoException;
import dad.core.gateway.CondominioGateway;
import dad.core.regra.RegrasCondominio;
import dad.core.regra.RegrasPaginacao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CondominioService {

  private final CondominioGateway gateway;

  public CondominioService(CondominioGateway gateway) {
    this.gateway = gateway;
  }

  @Transactional(readOnly = true)
  public Pagina<Condominio> listar(String busca, Boolean ativo, int pagina, int tamanho) {
    RegrasPaginacao.validar(pagina, tamanho);

    return gateway.listar(busca, ativo, pagina, tamanho);
  }

  @Transactional(readOnly = true)
  public Condominio buscar(Integer id) {
    return gateway
        .buscarPorId(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException("Condomínio não encontrado"));
  }

  public Condominio criar(Condominio condominio) {
    RegrasCondominio.validar(condominio);

    return gateway.criar(condominio);
  }

  public Condominio atualizar(Condominio condominio) {
    RegrasCondominio.validar(condominio);

    return gateway.atualizar(condominio);
  }

  public Condominio alterarStatus(Integer id, boolean ativo) {
    return gateway.alterarStatus(id, ativo);
  }
}
