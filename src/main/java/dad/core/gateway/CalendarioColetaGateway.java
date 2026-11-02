package dad.core.gateway;

import dad.core.domain.OcorrenciaColeta;
import dad.core.domain.Pagina;
import java.time.OffsetDateTime;

public interface CalendarioColetaGateway {
  Pagina<OcorrenciaColeta> listar(
      OffsetDateTime inicio,
      OffsetDateTime fim,
      Integer condominioId,
      Integer cooperativaId,
      Integer statusId,
      int pagina,
      int tamanho);
}
