package dad.core.gateway;

import dad.core.domain.Cooperativa;
import dad.core.domain.Pagina;
import java.util.Optional;

public interface CooperativaGateway {

  Pagina<Cooperativa> listar(String busca, int pagina, int tamanho);

  Optional<Cooperativa> buscarPorId(Integer id);

  Cooperativa criar(Cooperativa cooperativa);

  Cooperativa atualizar(Cooperativa cooperativa);
}
