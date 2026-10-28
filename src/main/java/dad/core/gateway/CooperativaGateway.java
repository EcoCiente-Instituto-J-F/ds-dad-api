package dad.core.gateway;

import java.util.Optional;

import dad.core.domain.Cooperativa;
import dad.core.domain.Pagina;

public interface CooperativaGateway {

  Pagina<Cooperativa> listar(
      String busca,
      int pagina,
      int tamanho);

  Optional<Cooperativa> buscarPorId(Integer id);

  Cooperativa criar(Cooperativa cooperativa);

  Cooperativa atualizar(Cooperativa cooperativa);
}