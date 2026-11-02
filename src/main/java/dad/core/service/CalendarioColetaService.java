package dad.core.service;

import dad.core.domain.OcorrenciaColeta;
import dad.core.domain.Pagina;
import dad.core.exception.CamposInvalidosException;
import dad.core.gateway.CalendarioColetaGateway;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CalendarioColetaService {
  private final CalendarioColetaGateway gateway;

  public CalendarioColetaService(CalendarioColetaGateway gateway) {
    this.gateway = gateway;
  }

  @Transactional(
      readOnly = true,
      isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
  public Pagina<OcorrenciaColeta> listar(
      OffsetDateTime inicio,
      OffsetDateTime fim,
      Integer condominioId,
      Integer cooperativaId,
      Integer statusId,
      int pagina,
      int tamanho) {
    var erros = new LinkedHashMap<String, String>();
    if (inicio == null) erros.put("inicio", "Informe um timestamp com fuso.");
    if (fim == null) erros.put("fim", "Informe um timestamp com fuso.");
    if (inicio != null && fim != null && !inicio.isBefore(fim))
      erros.put("fim", "Deve ser posterior ao início.");
    if (condominioId != null && condominioId <= 0)
      erros.put("condominioId", "Informe um ID positivo.");
    if (cooperativaId != null && cooperativaId <= 0)
      erros.put("cooperativaId", "Informe um ID positivo.");
    if (statusId != null && statusId <= 0) erros.put("statusId", "Informe um ID positivo.");
    if (pagina < 0) erros.put("pagina", "Deve ser maior ou igual a zero.");
    if (tamanho < 1 || tamanho > 100) erros.put("tamanho", "Deve estar entre 1 e 100.");
    if (!erros.isEmpty())
      throw new CamposInvalidosException("Confira os filtros do calendário.", erros);
    return gateway.listar(inicio, fim, condominioId, cooperativaId, statusId, pagina, tamanho);
  }
}
