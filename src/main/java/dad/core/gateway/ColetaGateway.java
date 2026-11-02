package dad.core.gateway;

import dad.core.domain.Coleta;
import dad.core.domain.Pagina;
import java.time.OffsetDateTime;
import java.util.Optional;

public interface ColetaGateway {

  Pagina<Coleta> listar(
      String busca,
      Integer statusId,
      OffsetDateTime inicio,
      OffsetDateTime fim,
      int pagina,
      int tamanho);

  Optional<Coleta> buscarPorId(Integer id);

  Coleta criar(Coleta.Criacao criacao);

  Coleta salvar(Integer id, Coleta.Dados dados);

  Coleta cancelar(Integer id);

  Coleta registrarVisita(Integer id, Coleta.RegistroVisita registro);
}
