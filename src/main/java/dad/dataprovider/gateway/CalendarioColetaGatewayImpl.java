package dad.dataprovider.gateway;

import dad.core.domain.OcorrenciaColeta;
import dad.core.domain.Pagina;
import dad.core.gateway.CalendarioColetaGateway;
import dad.core.gateway.ColetaGateway;
import dad.dataprovider.repository.CalendarioColetaRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;

@Component
public class CalendarioColetaGatewayImpl implements CalendarioColetaGateway {
  private final CalendarioColetaRepository repository;
  private final ColetaGateway coletas;

  public CalendarioColetaGatewayImpl(CalendarioColetaRepository repository, ColetaGateway coletas) {
    this.repository = repository;
    this.coletas = coletas;
  }

  @Override
  public Pagina<OcorrenciaColeta> listar(
      OffsetDateTime inicio,
      OffsetDateTime fim,
      Integer condominioId,
      Integer cooperativaId,
      Integer statusId,
      int pagina,
      int tamanho) {
    long total = repository.contar(inicio, fim, condominioId, cooperativaId, statusId);
    var itens =
        repository
            .listar(inicio, fim, condominioId, cooperativaId, statusId, pagina, tamanho)
            .stream()
            .map(
                linha ->
                    new OcorrenciaColeta(
                        coletas.buscarPorId(((Number) linha[0]).intValue()).orElseThrow(),
                        data(linha[1])))
            .toList();
    return new Pagina<>(
        itens, pagina, tamanho, total, Math.toIntExact((total + tamanho - 1) / tamanho));
  }

  private OffsetDateTime data(Object valor) {
    if (valor instanceof OffsetDateTime data) return data;
    if (valor instanceof Timestamp data) return data.toInstant().atOffset(ZoneOffset.UTC);
    if (valor instanceof Instant data) return data.atOffset(ZoneOffset.UTC);
    throw new IllegalStateException("Tipo de timestamp não suportado pelo driver.");
  }
}
