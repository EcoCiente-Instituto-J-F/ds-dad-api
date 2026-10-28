package dad.entrypoint.mapper;

import org.springframework.stereotype.Component;

import dad.core.domain.Coleta;
import dad.core.domain.Pagina;
import dad.entrypoint.dto.request.ColetaRequest;
import dad.entrypoint.dto.request.VisitaColetaRequest;
import dad.entrypoint.dto.response.ColetaResponse;
import dad.entrypoint.dto.response.PaginaResponse;

@Component
public class ColetaMapper {

  public Coleta.Dados toDomain(ColetaRequest request) {
    return new Coleta.Dados(
        request.condominioId(),
        request.cooperativaId(),
        request.statusId(),
        request.dataInicio(),
        request.dataFim(),
        request.recorrente(),
        request.diasSemanaIds());
  }

  public Coleta.RegistroVisita toDomain(
      VisitaColetaRequest request) {
    String observacao = request.observacao();

    if (observacao != null) {
      observacao = observacao.trim();

      if (observacao.isEmpty()) {
        observacao = null;
      }
    }

    return new Coleta.RegistroVisita(
        request.data(),
        request.realizada(),
        request.confirmada(),
        observacao);
  }

  public ColetaResponse toResponse(Coleta coleta) {
    return new ColetaResponse(
        coleta.id(),
        coleta.condominioId(),
        coleta.cooperativaId(),
        coleta.statusId(),
        coleta.status(),
        coleta.dataInicio(),
        coleta.dataFim(),
        coleta.recorrente(),
        coleta.diasSemanaIds(),
        coleta.visitas()
            .stream()
            .map(visita -> new ColetaResponse.VisitaResponse(
                visita.id(),
                visita.data(),
                visita.realizada(),
                visita.confirmada(),
                visita.confirmadoEm(),
                visita.observacao()))
            .toList());
  }

  public PaginaResponse<ColetaResponse> toResponse(
      Pagina<Coleta> pagina) {
    return new PaginaResponse<>(
        pagina.itens()
            .stream()
            .map(this::toResponse)
            .toList(),
        pagina.pagina(),
        pagina.tamanho(),
        pagina.totalItens(),
        pagina.totalPaginas());
  }
}