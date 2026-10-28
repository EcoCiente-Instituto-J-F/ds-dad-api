package dad.entrypoint.dto.request;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotNull;

public record VisitaColetaRequest(

    @NotNull OffsetDateTime data,

    @NotNull Boolean realizada,

    @NotNull Boolean confirmada,

    String observacao

) {
}