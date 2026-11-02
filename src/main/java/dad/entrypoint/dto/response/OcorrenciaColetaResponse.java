package dad.entrypoint.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Ocorrência calculada; o ID pertence ao agendamento e pode se repetir.")
public record OcorrenciaColetaResponse(
    Integer id,
    Integer condominioId,
    Integer cooperativaId,
    Integer statusId,
    String status,
    OffsetDateTime dataInicio,
    OffsetDateTime dataFim,
    boolean recorrente,
    List<Integer> diasSemanaIds,
    List<ColetaResponse.VisitaResponse> visitas,
    @Schema(description = "Instante da ocorrência no calendário.") OffsetDateTime ocorrenciaEm) {}
