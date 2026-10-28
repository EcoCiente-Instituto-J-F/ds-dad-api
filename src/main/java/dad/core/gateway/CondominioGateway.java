package dad.core.gateway;

import java.util.Optional;

import dad.core.domain.Condominio;
import dad.core.domain.Pagina;

public interface CondominioGateway {

  Pagina<Condominio> listar(
      String busca,
      Boolean ativo,
      int pagina,
      int tamanho);

  Optional<Condominio> buscarPorId(Integer id);

  Condominio criar(Condominio condominio);

  Condominio atualizar(Condominio condominio);

  Condominio alterarStatus(
      Integer id,
      boolean ativo);
}