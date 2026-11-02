package dad.entrypoint.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record VisitaColetaRequest(
    @NotNull OffsetDateTime data,
    @NotNull Boolean realizada,
    @NotNull Boolean confirmada,
    String observacao) {}
